//
//  VaultViewModel.swift
//  ReelLibrary
//

import SwiftUI
import Observation
import GRDB

@Observable
public final class VaultViewModel: @unchecked Sendable {
    // Database
    private let db: AppDatabase

    // State Variables
    public var searchQuery: String = ""
    public var isAiSearchEnabled: Bool = false
    public var orderBy: OrderBy = .date
    public var sortDirection: SortDirection = .descending
    public var selectedStatus: IngestionStatus? = nil
    public var selectedTag: String? = nil
    public var selectedCreator: String? = nil
    public var viewMode: ViewMode = .grid

    // Selection Mode
    public var isSelectionMode: Bool = false
    public var selectedIds: Set<Int64> = []

    // Modals & Navigation
    public var isDrawerOpen: Bool = false
    public var isSortFilterSheetOpen: Bool = false
    public var isSettingsOpen: Bool = false
    public var selectedReelForDetail: ReelRecord? = nil
    public var isAddingUrl: Bool = false
    public var newUrlInput: String = ""
    public var errorMessage: String? = nil

    // Data Storage
    public var reels: [ReelRecord] = []
    public var liveLatencyMs: Int = 165
    public var activeEngineName: String = "Cloud (Groq LPU)"

    public var liveLatencyText: String {
        "⚡ \(liveLatencyMs)ms • \(activeEngineName.contains("Cloud") ? "Cloud" : "Local")"
    }

    public init(database: AppDatabase = .shared) {
        self.db = database
        loadReels()
        checkPendingSharedUrls()
    }

    // MARK: - Filtered Reels Query

    public var filteredReels: [ReelRecord] {
        var result = reels

        // 1. Text / AI Search Query
        let trimmedQuery = searchQuery.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        if !trimmedQuery.isEmpty {
            let cleanQuery = trimmedQuery.trimmingCharacters(in: CharacterSet(charactersIn: "#@"))
            result = result.filter { reel in
                let titleMatch = reel.title?.localizedCaseInsensitiveContains(cleanQuery) == true
                let captionMatch = reel.caption?.localizedCaseInsensitiveContains(cleanQuery) == true
                let summaryMatch = reel.oneLineSummary?.localizedCaseInsensitiveContains(cleanQuery) == true
                let transcriptMatch = reel.transcript?.localizedCaseInsensitiveContains(cleanQuery) == true
                let creatorMatch = reel.creatorHandle?.localizedCaseInsensitiveContains(cleanQuery) == true
                let tagMatch = reel.tags.contains { $0.localizedCaseInsensitiveContains(cleanQuery) }
                return titleMatch || captionMatch || summaryMatch || transcriptMatch || creatorMatch || tagMatch
            }
        }

        // 2. Status Filter
        if let status = selectedStatus {
            result = result.filter { $0.status == status }
        }

        // 3. Tag Filter
        if let tag = selectedTag {
            result = result.filter { $0.tags.contains(tag) }
        }

        // 4. Creator Filter
        if let creator = selectedCreator {
            result = result.filter { $0.displayCreator.localizedCaseInsensitiveContains(creator) }
        }

        // 5. Order By & Direction
        result.sort { a, b in
            let dir = (sortDirection == .ascending)
            switch orderBy {
            case .date:
                return dir ? a.createdAt < b.createdAt : a.createdAt > b.createdAt
            case .title:
                let titleA = a.displayTitle.lowercased()
                let titleB = b.displayTitle.lowercased()
                return dir ? titleA < titleB : titleA > titleB
            case .creator:
                let creatorA = a.displayCreator.lowercased()
                let creatorB = b.displayCreator.lowercased()
                return dir ? creatorA < creatorB : creatorA > creatorB
            }
        }

        return result
    }

    public var hasActiveFilters: Bool {
        selectedStatus != nil ||
        selectedTag != nil ||
        selectedCreator != nil ||
        orderBy != .date ||
        sortDirection != .descending
    }

    // MARK: - Dynamic AI Search Suggestions

    public var aiSearchSuggestions: [String] {
        guard isAiSearchEnabled else { return [] }
        let q = searchQuery.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        var counts: [String: Int] = [:]

        for reel in reels {
            for tag in reel.tags {
                counts[tag, default: 0] += 1
            }
        }

        let sortedTags = counts.sorted(by: { $0.value > $1.value }).map { $0.key }
        if q.isEmpty {
            return Array(sortedTags.prefix(5))
        } else {
            return sortedTags.filter { $0.localizedCaseInsensitiveContains(q) }
        }
    }

    // MARK: - Statistics

    public var tagCounts: [String: Int] {
        var counts: [String: Int] = [:]
        for reel in reels {
            for tag in reel.tags {
                let clean = tag.trimmingCharacters(in: .whitespacesAndNewlines)
                if !clean.isEmpty {
                    counts[clean, default: 0] += 1
                }
            }
        }
        return counts
    }

    public var creatorCounts: [String: Int] {
        var counts: [String: Int] = [:]
        for reel in reels {
            let handle = reel.displayCreator
            counts[handle, default: 0] += 1
        }
        return counts
    }

    public var totalReelsCount: Int {
        reels.count
    }

    public var completedReelsCount: Int {
        reels.filter { $0.status == .completed }.count
    }

    // MARK: - Data Operations

    public func loadReels() {
        do {
            let items = try db.reelDao.getAll()
            if items.isEmpty {
                populateDemoReelsIfNeeded()
            } else {
                self.reels = items
            }
        } catch {
            self.errorMessage = "Failed to load reels: \(error.localizedDescription)"
        }
    }

    public func resetFilters() {
        selectedStatus = nil
        selectedTag = nil
        selectedCreator = nil
        orderBy = .date
        sortDirection = .descending
        searchQuery = ""
    }

    // MARK: - Selection Actions

    public func toggleSelectionMode() {
        isSelectionMode.toggle()
        if !isSelectionMode {
            selectedIds.removeAll()
        }
    }

    public func toggleSelect(id: Int64) {
        if selectedIds.contains(id) {
            selectedIds.remove(id)
        } else {
            selectedIds.insert(id)
        }
    }

    public func selectAll() {
        let currentFilteredIds = filteredReels.compactMap { $0.id }
        if selectedIds.count == currentFilteredIds.count {
            selectedIds.removeAll()
        } else {
            selectedIds = Set(currentFilteredIds)
        }
    }

    public func clearSelection() {
        selectedIds.removeAll()
        isSelectionMode = false
    }

    public func deleteSelected() {
        guard !selectedIds.isEmpty else { return }
        let idsToDelete = Array(selectedIds)
        do {
            try db.reelDao.delete(ids: idsToDelete)
            reels.removeAll { reel in
                guard let id = reel.id else { return false }
                return idsToDelete.contains(id)
            }
            clearSelection()
        } catch {
            self.errorMessage = "Failed to delete selected reels: \(error.localizedDescription)"
        }
    }

    public func deleteReel(id: Int64) {
        do {
            try db.reelDao.delete(id: id)
            reels.removeAll { $0.id == id }
            if selectedReelForDetail?.id == id {
                selectedReelForDetail = nil
            }
        } catch {
            self.errorMessage = "Failed to delete reel: \(error.localizedDescription)"
        }
    }

    public func reprocessReel(id: Int64) {
        guard let index = reels.firstIndex(where: { $0.id == id }) else { return }
        var reel = reels[index]
        reel.status = .extracting
        do {
            try db.reelDao.update(reel)
            reels[index] = reel
            selectedReelForDetail = reel

            // Simulate simulated processing completion
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) { [weak self] in
                guard let self else { return }
                if let idx = self.reels.firstIndex(where: { $0.id == id }) {
                    var updated = self.reels[idx]
                    updated.status = .completed
                    try? self.db.reelDao.update(updated)
                    self.reels[idx] = updated
                    if self.selectedReelForDetail?.id == id {
                        self.selectedReelForDetail = updated
                    }
                }
            }
        } catch {
            self.errorMessage = "Reprocess failed: \(error.localizedDescription)"
        }
    }

    public func saveNotes(id: Int64, notes: String) {
        do {
            try db.reelDao.updateNotes(id: id, notes: notes)
            if let index = reels.firstIndex(where: { $0.id == id }) {
                reels[index].userNotes = notes
                if selectedReelForDetail?.id == id {
                    selectedReelForDetail?.userNotes = notes
                }
            }
        } catch {
            self.errorMessage = "Failed to save notes: \(error.localizedDescription)"
        }
    }

    public func addTag(id: Int64, tag: String) {
        let clean = tag.trimmingCharacters(in: .whitespacesAndNewlines).replacingOccurrences(of: "#", with: "")
        guard !clean.isEmpty else { return }
        guard let index = reels.firstIndex(where: { $0.id == id }) else { return }
        var currentTags = reels[index].tags
        guard !currentTags.contains(clean) else { return }
        currentTags.append(clean)
        reels[index].tags = currentTags
        do {
            try db.reelDao.updateTags(id: id, tags: currentTags)
            if selectedReelForDetail?.id == id {
                selectedReelForDetail?.tags = currentTags
            }
        } catch {
            self.errorMessage = "Failed to add tag: \(error.localizedDescription)"
        }
    }

    public func removeTag(id: Int64, tag: String) {
        guard let index = reels.firstIndex(where: { $0.id == id }) else { return }
        var currentTags = reels[index].tags
        currentTags.removeAll { $0 == tag }
        reels[index].tags = currentTags
        do {
            try db.reelDao.updateTags(id: id, tags: currentTags)
            if selectedReelForDetail?.id == id {
                selectedReelForDetail?.tags = currentTags
            }
        } catch {
            self.errorMessage = "Failed to remove tag: \(error.localizedDescription)"
        }
    }

    public func ingestUrl(_ urlString: String) {
        let trimmed = urlString.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }

        let shortcode = extractShortcode(from: trimmed) ?? UUID().uuidString.prefix(8).lowercased()

        var newReel = ReelRecord(
            canonicalUrl: trimmed,
            shortcode: String(shortcode),
            creatorHandle: "@instagram_user",
            caption: "New Reel added via URL",
            title: "Analyzing Reel #\(shortcode)...",
            oneLineSummary: "Extracting insights and audio...",
            takeawaysJson: "[\"Processing audio transcript with Groq Whisper...\", \"Summarizing actionable concepts with Qwen...\"]",
            tagsJson: "[\"saved\", \"new\"]",
            engineUsed: activeEngineName,
            status: .extracting
        )

        do {
            let id = try db.reelDao.insert(&newReel)
            newReel.id = id
            reels.insert(newReel, at: 0)

            // Simulate completed background ingestion pipeline
            DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) { [weak self] in
                guard let self else { return }
                if let idx = self.reels.firstIndex(where: { $0.id == id }) {
                    var finished = self.reels[idx]
                    finished.status = .completed
                    finished.title = "Mastering Productivity & High-Focus Rituals"
                    finished.oneLineSummary = "A scientific protocol on dopamine regulation, ultradian cycles, and minimizing cognitive friction."
                    finished.takeaways = [
                        "Work in strict 90-minute ultradian rhythm cycles followed by 20-minute active mental rest.",
                        "Delay morning caffeine intake by 90-120 minutes to clear adenosine naturally.",
                        "View natural morning sunlight within 30 minutes of waking to anchor your circadian rhythm."
                    ]
                    finished.tags = ["productivity", "neuroscience", "focus", "habits"]
                    try? self.db.reelDao.update(finished)
                    self.reels[idx] = finished
                    if self.selectedReelForDetail?.id == id {
                        self.selectedReelForDetail = finished
                    }
                }
            }
        } catch {
            self.errorMessage = "Failed to ingest reel: \(error.localizedDescription)"
        }
    }

    public func checkPendingSharedUrls() {
        // 1. Check Shared Keychain Queue (primary cross-sandbox bridge)
        let sharedItems = SharedReelQueue.shared.dequeueAll()
        for item in sharedItems {
            let targetUrl = InstagramUrlParser.parse(item.url)?.canonicalUrl ?? item.url
            if !reels.contains(where: { $0.canonicalUrl == targetUrl }) {
                ingestUrl(targetUrl)
            }
        }

        // 2. Check App Group UserDefaults if provisioned
        let defaults = UserDefaults(suiteName: AppDatabase.appGroupIdentifier)
        if let pending = defaults?.stringArray(forKey: "pending_shared_urls"), !pending.isEmpty {
            for url in pending {
                let targetUrl = InstagramUrlParser.parse(url)?.canonicalUrl ?? url
                if !reels.contains(where: { $0.canonicalUrl == targetUrl }) {
                    ingestUrl(targetUrl)
                }
            }
            defaults?.removeObject(forKey: "pending_shared_urls")
        }
    }

    private func extractShortcode(from url: String) -> String? {
        let pattern = #"(?:reel|reels|p)\/([A-Za-z0-9_-]+)"#
        guard let regex = try? NSRegularExpression(pattern: pattern, options: .caseInsensitive),
              let match = regex.firstMatch(in: url, range: NSRange(url.startIndex..., in: url)),
              let range = Range(match.range(at: 1), in: url) else {
            return nil
        }
        return String(url[range])
    }

    // MARK: - Demo Prepopulation
    public func populateDemoReelsIfNeeded() {
        let demo1 = ReelRecord(
            canonicalUrl: "https://www.instagram.com/reel/C5example1/",
            shortcode: "C5example1",
            creatorHandle: "@hubermanlab",
            caption: "The science of sleep architecture and temperature regulation for restorative deep sleep.",
            transcript: "One of the most overlooked triggers of deep slow-wave sleep is dropping your core body temperature by 1 to 3 degrees Fahrenheit. Taking a warm shower before bed promotes vasodilation, dumping core heat through your hands and feet.",
            title: "Core Temperature & Deep Sleep Architecture",
            oneLineSummary: "How cooling your core body temperature by 2°F accelerates slow-wave restorative sleep.",
            takeawaysJson: "[\"Take a warm bath or shower 60 minutes before bed to dilate extremities and cool core body temperature.\", \"Keep bedroom ambient temperature between 65-68°F (18-20°C).\", \"Avoid intense training or heavy meals within 3 hours of sleep.\"]",
            tagsJson: "[\"sleep\", \"neuroscience\", \"health\", \"huberman\"]",
            engineUsed: "Cloud Free-Tier (Groq LPU)",
            status: .completed,
            createdAt: Int64(Date().timeIntervalSince1970 * 1000) - 3600_000 * 2
        )

        let demo2 = ReelRecord(
            canonicalUrl: "https://www.instagram.com/reel/C5example2/",
            shortcode: "C5example2",
            creatorHandle: "@naval",
            caption: "Specific knowledge cannot be trained. If society can train you, it can train someone else and replace you.",
            transcript: "Specific knowledge is found much more by pursuing your innate talents, your genuine curiosity, and your passion. It’s not by going to school for whatever is the hottest job right now.",
            title: "Naval Ravikant on Specific Knowledge & Leverage",
            oneLineSummary: "Why pursuing authentic curiosity creates irreplaceable career leverage and asymmetric returns.",
            takeawaysJson: "[\"Follow your genuine intellectual obsessions rather than chasing short-term market trends.\", \"Arm yourself with specific knowledge, accountability, and permissionless leverage (code & media).\", \"Play iterated games with long-term people.\"]",
            tagsJson: "[\"wealth\", \"philosophy\", \"naval\", \"career\"]",
            engineUsed: "Cloud Free-Tier (Groq LPU)",
            status: .completed,
            createdAt: Int64(Date().timeIntervalSince1970 * 1000) - 3600_000 * 24
        )

        let demo3 = ReelRecord(
            canonicalUrl: "https://www.instagram.com/reel/C5example3/",
            shortcode: "C5example3",
            creatorHandle: "@timferriss",
            caption: "What would this look like if it were easy? The question that clarifies complexity.",
            transcript: "Whenever I feel stuck or overwhelmed by a monumental project, I pause and ask: What would this look like if it were easy? It removes artificial resistance and unnecessary steps immediately.",
            title: "The 'What If It Were Easy?' Mental Model",
            oneLineSummary: "A heuristic to dismantle artificial friction and solve complex problems simply.",
            takeawaysJson: "[\"Ask yourself 'What would this look like if it were easy?' to strip away non-essential friction.\", \"Identify the single bottleneck holding back progress.\", \"Focus entirely on the 20% of inputs that yield 80% of desired outcomes.\"]",
            tagsJson: "[\"productivity\", \"mental-models\", \"ferriss\"]",
            engineUsed: "Cloud Free-Tier (Groq LPU)",
            status: .completed,
            createdAt: Int64(Date().timeIntervalSince1970 * 1000) - 3600_000 * 48
        )

        var items = [demo1, demo2, demo3]
        for i in 0..<items.count {
            if let id = try? db.reelDao.insert(&items[i]) {
                items[i].id = id
            }
        }
        self.reels = items
    }
}

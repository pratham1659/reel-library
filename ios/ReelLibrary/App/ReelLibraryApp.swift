//
//  ReelLibraryApp.swift
//  ReelLibrary
//

import SwiftUI

@main
struct ReelLibraryApp: App {
    @Environment(\.scenePhase) private var scenePhase
    @State private var vaultViewModel: VaultViewModel

    init() {
        // Initialize AppDatabase and run SQLite migrations
        _ = AppDatabase.shared
        _vaultViewModel = State(initialValue: VaultViewModel())
    }

    var body: some Scene {
        WindowGroup {
            VaultView(viewModel: vaultViewModel)
                .preferredColorScheme(.dark)
                .onOpenURL { url in
                    handleIncomingURL(url)
                }
                .onAppear {
                    checkForPendingSharedReels()
                }
                .onChange(of: scenePhase) { _, newPhase in
                    if newPhase == .active {
                        checkForPendingSharedReels()
                    }
                }
                .onReceive(NotificationCenter.default.publisher(for: UIPasteboard.changedNotification)) { _ in
                    checkForPendingSharedReels()
                }
                .onReceive(NotificationCenter.default.publisher(for: UIApplication.willEnterForegroundNotification)) { _ in
                    checkForPendingSharedReels()
                }
        }
    }

    private func checkForPendingSharedReels() {
        // 1. General system clipboard check (works reliably across isolated iOS sandboxes)
        if let clip = UIPasteboard.general.string?.trimmingCharacters(in: .whitespacesAndNewlines),
           clip.contains("instagram.com") {
            if let parsed = InstagramUrlParser.parse(clip) {
                if !vaultViewModel.reels.contains(where: { $0.canonicalUrl == parsed.canonicalUrl }) {
                    vaultViewModel.ingestUrl(parsed.canonicalUrl)
                }
            }
        }

        // 2. Custom pasteboard type check
        let pasteboardType = "io.github.alokkr016.reellibrary.shared_url"
        if UIPasteboard.general.contains(pasteboardTypes: [pasteboardType]) {
            if let raw = UIPasteboard.general.value(forPasteboardType: pasteboardType) as? String, !raw.isEmpty {
                if !vaultViewModel.reels.contains(where: { $0.canonicalUrl == raw }) {
                    vaultViewModel.ingestUrl(raw)
                }
                UIPasteboard.general.setValue("", forPasteboardType: pasteboardType)
            }
        }

        // 3. App Group pending queue (when provisioned)
        if let defaults = UserDefaults(suiteName: AppDatabase.appGroupIdentifier) {
            let pending = defaults.stringArray(forKey: "pending_shared_urls") ?? []
            if !pending.isEmpty {
                defaults.removeObject(forKey: "pending_shared_urls")
                for urlStr in pending {
                    if !vaultViewModel.reels.contains(where: { $0.canonicalUrl == urlStr }) {
                        vaultViewModel.ingestUrl(urlStr)
                    }
                }
            }
        }
    }

    private func handleIncomingURL(_ url: URL) {
        // Handle custom URL schemes (e.g. reellibrary://ingest?url=https://...)
        if url.scheme == "reellibrary" {
            if let components = URLComponents(url: url, resolvingAgainstBaseURL: true),
               let urlParam = components.queryItems?.first(where: { $0.name == "url" })?.value {
                vaultViewModel.ingestUrl(urlParam)
                return
            }
        }

        // Handle direct web URLs shared to app
        if url.host?.contains("instagram.com") == true {
            vaultViewModel.ingestUrl(url.absoluteString)
        }
    }
}

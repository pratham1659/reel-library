import UIKit
import Social
import UniformTypeIdentifiers
import OSLog

private let logger = Logger(subsystem: "com.reellibrary.app.share", category: "ShareViewController")

final class ShareViewController: UIViewController {
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor(white: 0.1, alpha: 0.8)
        processSharedItem()
    }

    private func processSharedItem() {
        guard let extensionItem = extensionContext?.inputItems.first as? NSExtensionItem,
              let attachments = extensionItem.attachments,
              !attachments.isEmpty else {
            completeRequest()
            return
        }

        // Search for URL or text attachment
        for itemProvider in attachments {
            if itemProvider.hasItemConformingToTypeIdentifier(UTType.url.identifier) {
                itemProvider.loadItem(forTypeIdentifier: UTType.url.identifier, options: nil) { [weak self] (item, _) in
                    let extracted: String?
                    if let url = item as? URL {
                        extracted = url.absoluteString
                    } else if let urlStr = item as? String {
                        extracted = urlStr
                    } else {
                        extracted = nil
                    }

                    Task { @MainActor [weak self] in
                        if let extracted {
                            self?.handleExtractedText(extracted)
                        } else {
                            self?.completeRequest()
                        }
                    }
                }
                return
            } else if itemProvider.hasItemConformingToTypeIdentifier(UTType.plainText.identifier) {
                itemProvider.loadItem(forTypeIdentifier: UTType.plainText.identifier, options: nil) { [weak self] (item, _) in
                    let extracted = item as? String
                    Task { @MainActor [weak self] in
                        if let extracted {
                            self?.handleExtractedText(extracted)
                        } else {
                            self?.completeRequest()
                        }
                    }
                }
                return
            }
        }

        completeRequest()
    }

    private func handleExtractedText(_ text: String) {
        let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else {
            completeRequest()
            return
        }

        // Step 1: Parse canonical Instagram Reel URL & shortcode
        guard let parsed = InstagramUrlParser.parse(trimmed) else {
            logger.warning("Shared text did not contain a valid Instagram Reel URL: \(trimmed)")
            completeRequest()
            return
        }

        // Step 2: Persist into App Group Shared SQLite database
        do {
            let database = AppDatabase.shared
            let dao = database.reelDao

            if let existing = try dao.findByCanonicalUrl(parsed.canonicalUrl) {
                logger.info("Reel already exists in vault: #\(existing.id ?? -1)")
            } else {
                var newRecord = ReelRecord(
                    canonicalUrl: parsed.canonicalUrl,
                    shortcode: parsed.shortcode,
                    status: .queued
                )
                try dao.insert(&newRecord)
                logger.info("Queued shared reel #\(newRecord.id ?? -1) into App Group database.")
            }

            // Also keep secondary pending queue in App Group defaults for instant UI wakeups
            if let defaults = UserDefaults(suiteName: AppDatabase.appGroupIdentifier) {
                var pending = defaults.stringArray(forKey: "pending_shared_urls") ?? []
                if !pending.contains(parsed.canonicalUrl) {
                    pending.append(parsed.canonicalUrl)
                    defaults.set(pending, forKey: "pending_shared_urls")
                }
            }

            // Step 3: Trigger haptic feedback
            let generator = UINotificationFeedbackGenerator()
            generator.prepare()
            generator.notificationOccurred(.success)
        } catch {
            logger.error("Failed to write queued reel to App Group database: \(error.localizedDescription)")
        }

        // Step 4: Dismiss immediately (< 200ms)
        completeRequest()
    }

    private func completeRequest() {
        extensionContext?.completeRequest(returningItems: [], completionHandler: nil)
    }
}

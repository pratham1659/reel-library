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
        guard let inputItems = extensionContext?.inputItems as? [NSExtensionItem], !inputItems.isEmpty else {
            logger.warning("No input items provided to share extension.")
            completeRequest()
            return
        }

        Task {
            // Step 1: Check attributedContentText across all extension items
            for item in inputItems {
                if let text = item.attributedContentText?.string,
                   let parsed = InstagramUrlParser.parse(text) {
                    await handleExtractedReel(parsed)
                    return
                }
            }

            // Step 2: Iterate through all attachments across all input items
            let supportedTypeIdentifiers = [
                UTType.url.identifier,
                UTType.plainText.identifier,
                UTType.text.identifier
            ]

            for item in inputItems {
                guard let attachments = item.attachments else { continue }
                for provider in attachments {
                    for typeId in supportedTypeIdentifiers {
                        if provider.hasItemConformingToTypeIdentifier(typeId) {
                            if let text = await loadItemText(from: provider, forTypeIdentifier: typeId),
                               let parsed = InstagramUrlParser.parse(text) {
                                await handleExtractedReel(parsed)
                                return
                            }
                        }
                    }
                }
            }

            logger.warning("No valid Instagram Reel URL found in any attachments.")
            await MainActor.run {
                completeRequest()
            }
        }
    }

    private func loadItemText(from provider: NSItemProvider, forTypeIdentifier typeIdentifier: String) async -> String? {
        await withCheckedContinuation { continuation in
            provider.loadItem(forTypeIdentifier: typeIdentifier, options: nil) { item, error in
                if let error = error {
                    logger.debug("Failed loading \(typeIdentifier): \(error.localizedDescription)")
                    continuation.resume(returning: nil)
                    return
                }

                if let url = item as? URL {
                    continuation.resume(returning: url.absoluteString)
                } else if let string = item as? String {
                    continuation.resume(returning: string)
                } else if let attrStr = item as? NSAttributedString {
                    continuation.resume(returning: attrStr.string)
                } else if let data = item as? Data, let string = String(data: data, encoding: .utf8) {
                    continuation.resume(returning: string)
                } else {
                    continuation.resume(returning: nil)
                }
            }
        }
    }

    @MainActor
    private func handleExtractedReel(_ parsed: ParsedReel) {
        // Step 1: Persist into App Group Shared SQLite database
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

            // Step 2: Trigger haptic feedback
            let generator = UINotificationFeedbackGenerator()
            generator.prepare()
            generator.notificationOccurred(.success)
        } catch {
            logger.error("Failed to write queued reel to App Group database: \(error.localizedDescription)")
        }

        // Step 3: Dismiss immediately (< 200ms)
        completeRequest()
    }

    private func completeRequest() {
        extensionContext?.completeRequest(returningItems: [], completionHandler: nil)
    }
}

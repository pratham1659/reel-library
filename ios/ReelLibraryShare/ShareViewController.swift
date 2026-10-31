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
        // Step 1: Write canonical URL to system general clipboard and private pasteboard
        UIPasteboard.general.string = parsed.canonicalUrl
        let pasteboardType = "io.github.alokkr016.reellibrary.shared_url"
        UIPasteboard.general.setValue(parsed.canonicalUrl, forPasteboardType: pasteboardType)

        // Step 2: Persist into SQLite database
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
                logger.info("Queued shared reel #\(newRecord.id ?? -1) into database.")
            }

            if let defaults = UserDefaults(suiteName: AppDatabase.appGroupIdentifier) {
                var pending = defaults.stringArray(forKey: "pending_shared_urls") ?? []
                if !pending.contains(parsed.canonicalUrl) {
                    pending.append(parsed.canonicalUrl)
                    defaults.set(pending, forKey: "pending_shared_urls")
                }
            }
        } catch {
            logger.error("Failed to write queued reel to database: \(error.localizedDescription)")
        }

        // Step 3: Trigger haptic feedback
        let generator = UINotificationFeedbackGenerator()
        generator.prepare()
        generator.notificationOccurred(.success)

        // Step 4: Show visual confirmation HUD
        showSuccessHud()

        // Step 5: Also attempt to open main app via custom URL scheme
        openHostApp(with: parsed.canonicalUrl)

        // Step 6: Dismiss after brief visual confirmation
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.45) { [weak self] in
            self?.completeRequest()
        }
    }

    private func showSuccessHud() {
        let container = UIView()
        container.translatesAutoresizingMaskIntoConstraints = false
        container.backgroundColor = UIColor(red: 0.10, green: 0.12, blue: 0.16, alpha: 0.95)
        container.layer.cornerRadius = 18
        container.layer.borderWidth = 1
        container.layer.borderColor = UIColor(red: 0.0, green: 0.85, blue: 0.95, alpha: 0.4).cgColor
        container.layer.shadowColor = UIColor.black.cgColor
        container.layer.shadowOpacity = 0.4
        container.layer.shadowRadius = 12
        container.layer.shadowOffset = CGSize(width: 0, height: 4)

        let stack = UIStackView()
        stack.translatesAutoresizingMaskIntoConstraints = false
        stack.axis = .horizontal
        stack.spacing = 10
        stack.alignment = .center

        let icon = UIImageView(image: UIImage(systemName: "checkmark.circle.fill"))
        icon.tintColor = UIColor(red: 0.0, green: 0.85, blue: 0.95, alpha: 1.0)
        icon.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            icon.widthAnchor.constraint(equalToConstant: 24),
            icon.heightAnchor.constraint(equalToConstant: 24)
        ])

        let label = UILabel()
        label.text = "Saved to Reel Library"
        label.textColor = .white
        label.font = UIFont.systemFont(ofSize: 15, weight: .bold)

        stack.addArrangedSubview(icon)
        stack.addArrangedSubview(label)
        container.addSubview(stack)
        view.addSubview(container)

        NSLayoutConstraint.activate([
            container.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            container.centerYAnchor.constraint(equalTo: view.centerYAnchor),
            stack.topAnchor.constraint(equalTo: container.topAnchor, constant: 14),
            stack.bottomAnchor.constraint(equalTo: container.bottomAnchor, constant: -14),
            stack.leadingAnchor.constraint(equalTo: container.leadingAnchor, constant: 20),
            stack.trailingAnchor.constraint(equalTo: container.trailingAnchor, constant: -20)
        ])

        container.alpha = 0
        container.transform = CGAffineTransform(scaleX: 0.85, y: 0.85)
        UIView.animate(withDuration: 0.25, delay: 0, options: .curveEaseOut) {
            container.alpha = 1
            container.transform = .identity
        }
    }

    private func openHostApp(with canonicalUrl: String) {
        guard let encoded = canonicalUrl.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
              let url = URL(string: "reellibrary://ingest?url=\(encoded)") else {
            return
        }

        // Method 1: Standard NSExtensionContext openURL
        self.extensionContext?.open(url, completionHandler: nil)

        // Method 2: Extension context dynamic selector
        let extSelector = sel_registerName("openURL:completionHandler:")
        if let context = self.extensionContext, (context as AnyObject).responds(to: extSelector) {
            (context as AnyObject).perform(extSelector, with: url, with: nil)
        }

        // Method 3: Traverse UIResponder chain for openURL:
        let selector = sel_registerName("openURL:")
        var responder: UIResponder? = self
        while let current = responder {
            if current.responds(to: selector) {
                current.perform(selector, with: url)
                return
            }
            responder = current.next
        }

        // Method 4: UIApplication sharedApplication via dynamic class lookup
        if let appClass = NSClassFromString("UIApplication") as? NSObject.Type,
           let shared = appClass.perform(sel_registerName("sharedApplication"))?.takeUnretainedValue() as? NSObject {
            shared.perform(selector, with: url)
        }
    }

    private func completeRequest() {
        extensionContext?.completeRequest(returningItems: [], completionHandler: nil)
    }
}

import UIKit
import Social
import UniformTypeIdentifiers
import OSLog

private let logger = Logger(subsystem: "io.github.alokkr016.reellibrary.share", category: "ShareViewController")

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
        // Step 1: Enqueue into Shared Keychain Queue (bridges isolated sandboxes without App Groups)
        SharedReelQueue.shared.enqueue(url: parsed.canonicalUrl)

        // Step 2: Also write canonical URL to system general clipboard and private pasteboard
        UIPasteboard.general.string = parsed.canonicalUrl
        UIPasteboard.general.url = URL(string: parsed.canonicalUrl)
        let pasteboardType = "io.github.alokkr016.reellibrary.shared_url"
        UIPasteboard.general.setValue(parsed.canonicalUrl, forPasteboardType: pasteboardType)

        // Step 3: Persist into local extension SQLite database
        do {
            let database = AppDatabase.shared
            let dao = database.reelDao

            if let existing = try dao.findByCanonicalUrl(parsed.canonicalUrl) {
                logger.info("Reel already exists in extension vault: #\(existing.id ?? -1)")
            } else {
                var newRecord = ReelRecord(
                    canonicalUrl: parsed.canonicalUrl,
                    shortcode: parsed.shortcode,
                    status: .queued
                )
                try dao.insert(&newRecord)
                logger.info("Queued shared reel #\(newRecord.id ?? -1) into extension database.")
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

        // Step 4: Trigger haptic feedback
        let generator = UINotificationFeedbackGenerator()
        generator.prepare()
        generator.notificationOccurred(.success)

        // Step 5: Show visual confirmation HUD
        showSuccessHud()

        // Step 6: Attempt to open main app via custom URL scheme with graceful dismissal
        openHostAppAndDismiss(with: parsed.canonicalUrl)
    }

    private func showSuccessHud() {
        let container = UIView()
        container.translatesAutoresizingMaskIntoConstraints = false
        container.backgroundColor = UIColor(red: 0.10, green: 0.12, blue: 0.16, alpha: 0.96)
        container.layer.cornerRadius = 18
        container.layer.borderWidth = 1.2
        container.layer.borderColor = UIColor(red: 0.0, green: 0.85, blue: 0.95, alpha: 0.5).cgColor
        container.layer.shadowColor = UIColor.black.cgColor
        container.layer.shadowOpacity = 0.5
        container.layer.shadowRadius = 16
        container.layer.shadowOffset = CGSize(width: 0, height: 6)

        let stack = UIStackView()
        stack.translatesAutoresizingMaskIntoConstraints = false
        stack.axis = .horizontal
        stack.spacing = 12
        stack.alignment = .center

        let icon = UIImageView(image: UIImage(systemName: "checkmark.circle.fill"))
        icon.tintColor = UIColor(red: 0.0, green: 0.85, blue: 0.95, alpha: 1.0)
        icon.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            icon.widthAnchor.constraint(equalToConstant: 28),
            icon.heightAnchor.constraint(equalToConstant: 28)
        ])

        let textStack = UIStackView()
        textStack.axis = .vertical
        textStack.spacing = 2

        let titleLabel = UILabel()
        titleLabel.text = "Saved to Reel Library"
        titleLabel.textColor = .white
        titleLabel.font = UIFont.systemFont(ofSize: 15, weight: .bold)

        let subtitleLabel = UILabel()
        subtitleLabel.text = "Queued for AI Ingestion"
        subtitleLabel.textColor = UIColor(red: 0.0, green: 0.85, blue: 0.95, alpha: 0.85)
        subtitleLabel.font = UIFont.systemFont(ofSize: 12, weight: .medium)

        textStack.addArrangedSubview(titleLabel)
        textStack.addArrangedSubview(subtitleLabel)

        stack.addArrangedSubview(icon)
        stack.addArrangedSubview(textStack)
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

    private func openHostAppAndDismiss(with canonicalUrl: String) {
        guard let encoded = canonicalUrl.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
              let url = URL(string: "reellibrary://ingest?url=\(encoded)") else {
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.6) { [weak self] in
                self?.completeRequest()
            }
            return
        }

        // Try standard NSExtensionContext openURL
        var didOpen = false
        if let context = self.extensionContext {
            context.open(url) { [weak self] success in
                didOpen = success
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                    self?.completeRequest()
                }
            }
        }

        // Fallback dismissal timer in case openURL does not fire completion block
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.7) { [weak self] in
            if !didOpen {
                self?.completeRequest()
            }
        }
    }

    private func completeRequest() {
        extensionContext?.completeRequest(returningItems: [], completionHandler: nil)
    }
}

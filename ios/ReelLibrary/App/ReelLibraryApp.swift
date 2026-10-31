//
//  ReelLibraryApp.swift
//  ReelLibrary
//

import SwiftUI

@main
struct ReelLibraryApp: App {
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
                .onReceive(NotificationCenter.default.publisher(for: UIApplication.willEnterForegroundNotification)) { _ in
                    checkForPendingSharedReels()
                }
                .onAppear {
                    checkForPendingSharedReels()
                }
        }
    }

    private func checkForPendingSharedReels() {
        let pasteboardType = "io.github.alokkr016.reellibrary.shared_url"
        if UIPasteboard.general.contains(pasteboardTypes: [pasteboardType]) {
            if let raw = UIPasteboard.general.value(forPasteboardType: pasteboardType) as? String, !raw.isEmpty {
                vaultViewModel.ingestUrl(raw)
                UIPasteboard.general.setValue("", forPasteboardType: pasteboardType)
            }
        }

        // Also check App Group pending queue if available
        if let defaults = UserDefaults(suiteName: AppDatabase.appGroupIdentifier) {
            let pending = defaults.stringArray(forKey: "pending_shared_urls") ?? []
            if !pending.isEmpty {
                defaults.removeObject(forKey: "pending_shared_urls")
                for urlStr in pending {
                    vaultViewModel.ingestUrl(urlStr)
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

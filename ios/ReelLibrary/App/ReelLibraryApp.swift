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

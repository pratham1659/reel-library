//
//  SharedReelQueue.swift
//  ReelLibrary
//

import Foundation
import Security
import OSLog

private let logger = Logger(subsystem: "io.github.alokkr016.reellibrary", category: "SharedReelQueue")

public struct SharedReelItem: Codable, Sendable, Equatable {
    public let url: String
    public let timestamp: Double
    public let title: String?

    public init(url: String, timestamp: Double = Date().timeIntervalSince1970, title: String? = nil) {
        self.url = url
        self.timestamp = timestamp
        self.title = title
    }
}

/// Cross-process shared queue bridging the Share Extension and the Main App.
/// Uses the hardware-backed iOS Keychain with shared Keychain Access Group
/// (`$(AppIdentifierPrefix)io.github.alokkr016.reellibrary`), which is fully supported
/// on free personal developer accounts without requiring App Groups or user prompts.
public final class SharedReelQueue: @unchecked Sendable {
    public static let shared = SharedReelQueue()

    private let service = "io.github.alokkr016.reellibrary.shared_queue"
    private let account = "pending_shared_reels"
    private let accessGroup = "M5Q7N9D29M.io.github.alokkr016.reellibrary"
    private let lock = NSLock()

    // In-memory fallback if Keychain is unavailable (e.g. mock test environments)
    private var inMemoryItems: [SharedReelItem] = []

    public init() {}

    /// Enqueues a canonical reel URL into the shared queue.
    public func enqueue(url: String, title: String? = nil) {
        lock.lock()
        defer { lock.unlock() }

        var items = loadUnlocked()
        if !items.contains(where: { $0.url == url }) {
            items.append(SharedReelItem(url: url, title: title))
            saveUnlocked(items)
            logger.notice("SharedReelQueue: Enqueued URL \(url, privacy: .public). Queue count = \(items.count)")
        }
    }

    /// Dequeues and removes all pending reel items from the shared queue.
    public func dequeueAll() -> [SharedReelItem] {
        lock.lock()
        defer { lock.unlock() }

        let items = loadUnlocked()
        if !items.isEmpty {
            saveUnlocked([])
            logger.notice("SharedReelQueue: Dequeued \(items.count) pending reels.")
        }
        return items
    }

    /// Peeks at the current queue without draining items.
    public func peek() -> [SharedReelItem] {
        lock.lock()
        defer { lock.unlock() }
        return loadUnlocked()
    }

    /// Clears the queue.
    public func clear() {
        lock.lock()
        defer { lock.unlock() }
        saveUnlocked([])
    }

    // MARK: - Internal Storage Implementation

    private func loadUnlocked() -> [SharedReelItem] {
        // Try reading with explicit access group first
        if let data = readKeychainData(accessGroup: accessGroup),
           let items = try? JSONDecoder().decode([SharedReelItem].self, from: data) {
            inMemoryItems = items
            return items
        }

        // Try reading without access group (defaults to first entitlement group)
        if let data = readKeychainData(accessGroup: nil),
           let items = try? JSONDecoder().decode([SharedReelItem].self, from: data) {
            inMemoryItems = items
            return items
        }

        return inMemoryItems
    }

    private func saveUnlocked(_ items: [SharedReelItem]) {
        inMemoryItems = items

        guard let data = try? JSONEncoder().encode(items) else {
            logger.error("Failed to encode SharedReelItem list.")
            return
        }

        // Save with explicit access group
        let successGroup = writeKeychainData(data, accessGroup: accessGroup)
        // Also save without access group as fallback
        let successDefault = writeKeychainData(data, accessGroup: nil)

        if !successGroup && !successDefault {
            logger.warning("Keychain write failed; persisted to in-memory fallback cache.")
        }
    }

    private func readKeychainData(accessGroup: String?) -> Data? {
        var query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]
        if let group = accessGroup, !group.isEmpty {
            query[kSecAttrAccessGroup as String] = group
        }

        var item: CFTypeRef?
        let status = SecItemCopyMatching(query as CFDictionary, &item)
        if status == errSecSuccess, let data = item as? Data {
            return data
        }
        return nil
    }

    private func writeKeychainData(_ data: Data, accessGroup: String?) -> Bool {
        var baseQuery: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account
        ]
        if let group = accessGroup, !group.isEmpty {
            baseQuery[kSecAttrAccessGroup as String] = group
        }

        var addQuery = baseQuery
        addQuery[kSecValueData as String] = data
        addQuery[kSecAttrAccessible as String] = kSecAttrAccessibleAfterFirstUnlock

        let addStatus = SecItemAdd(addQuery as CFDictionary, nil)
        if addStatus == errSecSuccess {
            return true
        } else if addStatus == errSecDuplicateItem {
            let updateAttrs: [String: Any] = [
                kSecValueData as String: data,
                kSecAttrAccessible as String: kSecAttrAccessibleAfterFirstUnlock
            ]
            let updateStatus = SecItemUpdate(baseQuery as CFDictionary, updateAttrs as CFDictionary)
            return updateStatus == errSecSuccess
        }

        return false
    }
}

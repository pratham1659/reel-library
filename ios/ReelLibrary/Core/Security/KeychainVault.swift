//
//  KeychainVault.swift
//  ReelLibrary
//

import Foundation
import Security
import OSLog

private let logger = Logger(subsystem: "com.reellibrary.app", category: "KeychainVault")

/// Hardware-backed Apple Keychain Services wrapper for securely storing sensitive credentials
/// (such as Groq API keys) in the Secure Enclave / Data Protection keychain.
///
/// If Keychain Services fail or return an error (e.g. in headless unit tests or sandboxed environments),
/// an in-memory session cache serves as a thread-safe fallback, preventing plain-text disk writes.
public final class KeychainVault: @unchecked Sendable {
    public static let shared = KeychainVault()

    public static let groqApiKeyName = "vault_groq_api_key"
    public static let openAIApiKeyName = "vault_openai_api_key"
    public static let claudeApiKeyName = "vault_anthropic_api_key"
    public static let customApiKeyName = "vault_custom_api_key"

    private let service: String
    private let accessGroup: String?
    private let lock = NSLock()
    private var inMemoryCache: [String: String] = [:]

    public init(
        service: String = "com.reellibrary.vault",
        accessGroup: String? = nil
    ) {
        self.service = service
        self.accessGroup = accessGroup
    }

    // MARK: - Core Keychain Operations

    /// Persists a string value to the Keychain for a specific key.
    @discardableResult
    public func save(key: String, value: String) -> Bool {
        lock.lock()
        defer { lock.unlock() }

        let trimmed = value.trimmingCharacters(in: .whitespacesAndNewlines)
        if trimmed.isEmpty {
            return deleteUnlocked(key: key)
        }

        // Always update in-memory cache
        inMemoryCache[key] = trimmed

        guard let data = trimmed.data(using: .utf8) else {
            return false
        }

        var query = baseQuery(for: key)
        query[kSecValueData as String] = data
        query[kSecAttrAccessible as String] = kSecAttrAccessibleAfterFirstUnlock

        let status = SecItemAdd(query as CFDictionary, nil)

        if status == errSecSuccess {
            return true
        } else if status == errSecDuplicateItem {
            // Item already exists; update it
            let updateQuery = baseQuery(for: key)
            let attributesToUpdate: [String: Any] = [
                kSecValueData as String: data,
                kSecAttrAccessible as String: kSecAttrAccessibleAfterFirstUnlock
            ]
            let updateStatus = SecItemUpdate(updateQuery as CFDictionary, attributesToUpdate as CFDictionary)
            return updateStatus == errSecSuccess
        } else {
            logger.warning("Keychain SecItemAdd failed with status: \(status). Using in-memory fallback.")
            return false
        }
    }

    /// Reads a string value from the Keychain for a specific key.
    public func read(key: String) -> String? {
        lock.lock()
        defer { lock.unlock() }

        var query = baseQuery(for: key)
        query[kSecReturnData as String] = true
        query[kSecMatchLimit as String] = kSecMatchLimitOne

        var item: CFTypeRef?
        let status = SecItemCopyMatching(query as CFDictionary, &item)

        if status == errSecSuccess,
           let data = item as? Data,
           let string = String(data: data, encoding: .utf8),
           !string.isEmpty {
            inMemoryCache[key] = string
            return string
        }

        // Fall back to in-memory cache
        return inMemoryCache[key]
    }

    /// Deletes a specific key from the Keychain and in-memory cache.
    @discardableResult
    public func delete(key: String) -> Bool {
        lock.lock()
        defer { lock.unlock() }
        return deleteUnlocked(key: key)
    }

    private func deleteUnlocked(key: String) -> Bool {
        inMemoryCache.removeValue(forKey: key)

        let query = baseQuery(for: key)
        let status = SecItemDelete(query as CFDictionary)
        return status == errSecSuccess || status == errSecItemNotFound
    }

    /// Clears all stored keys under this service domain.
    @discardableResult
    public func clearAll() -> Bool {
        lock.lock()
        defer { lock.unlock() }

        inMemoryCache.removeAll()

        var query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service
        ]
        if let accessGroup, !accessGroup.isEmpty {
            query[kSecAttrAccessGroup as String] = accessGroup
        }

        let status = SecItemDelete(query as CFDictionary)
        return status == errSecSuccess || status == errSecItemNotFound
    }

    // MARK: - Convenience Helpers

    /// Retrieves the Groq API key or returns an empty string.
    public func getGroqApiKey() -> String {
        read(key: Self.groqApiKeyName) ?? ""
    }

    /// Saves the Groq API key securely.
    @discardableResult
    public func setGroqApiKey(_ key: String) -> Bool {
        save(key: Self.groqApiKeyName, value: key)
    }

    /// Deletes the Groq API key securely.
    @discardableResult
    public func deleteGroqApiKey() -> Bool {
        delete(key: Self.groqApiKeyName)
    }

    /// Retrieves the OpenAI API key or returns an empty string.
    public func getOpenAIApiKey() -> String {
        read(key: Self.openAIApiKeyName) ?? ""
    }

    /// Saves the OpenAI API key securely.
    @discardableResult
    public func setOpenAIApiKey(_ key: String) -> Bool {
        save(key: Self.openAIApiKeyName, value: key)
    }

    /// Deletes the OpenAI API key securely.
    @discardableResult
    public func deleteOpenAIApiKey() -> Bool {
        delete(key: Self.openAIApiKeyName)
    }

    /// Retrieves the Anthropic Claude API key or returns an empty string.
    public func getClaudeApiKey() -> String {
        read(key: Self.claudeApiKeyName) ?? ""
    }

    /// Saves the Anthropic Claude API key securely.
    @discardableResult
    public func setClaudeApiKey(_ key: String) -> Bool {
        save(key: Self.claudeApiKeyName, value: key)
    }

    /// Deletes the Anthropic Claude API key securely.
    @discardableResult
    public func deleteClaudeApiKey() -> Bool {
        delete(key: Self.claudeApiKeyName)
    }

    /// Retrieves the Custom provider API key or returns an empty string.
    public func getCustomApiKey() -> String {
        read(key: Self.customApiKeyName) ?? ""
    }

    /// Saves the Custom provider API key securely.
    @discardableResult
    public func setCustomApiKey(_ key: String) -> Bool {
        save(key: Self.customApiKeyName, value: key)
    }

    /// Deletes the Custom provider API key securely.
    @discardableResult
    public func deleteCustomApiKey() -> Bool {
        delete(key: Self.customApiKeyName)
    }

    /// Resolves the API key for a specific AIProvider enum.
    public func apiKey(for provider: AIProvider) -> String {
        switch provider {
        case .groq: return getGroqApiKey()
        case .openAI: return getOpenAIApiKey()
        case .claude: return getClaudeApiKey()
        case .custom: return getCustomApiKey()
        }
    }

    /// Saves the API key for a specific AIProvider enum.
    @discardableResult
    public func setApiKey(_ key: String, for provider: AIProvider) -> Bool {
        switch provider {
        case .groq: return setGroqApiKey(key)
        case .openAI: return setOpenAIApiKey(key)
        case .claude: return setClaudeApiKey(key)
        case .custom: return setCustomApiKey(key)
        }
    }

    /// Deletes the API key for a specific AIProvider enum.
    @discardableResult
    public func deleteApiKey(for provider: AIProvider) -> Bool {
        switch provider {
        case .groq: return deleteGroqApiKey()
        case .openAI: return deleteOpenAIApiKey()
        case .claude: return deleteClaudeApiKey()
        case .custom: return deleteCustomApiKey()
        }
    }

    // MARK: - Private Query Builder

    private func baseQuery(for key: String) -> [String: Any] {
        var query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key
        ]
        if let accessGroup, !accessGroup.isEmpty {
            query[kSecAttrAccessGroup as String] = accessGroup
        }
        return query
    }
}

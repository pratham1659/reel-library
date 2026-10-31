//
//  AppPreferences.swift
//  ReelLibrary
//

import Foundation

/// Supported Cloud & Local AI Intelligence Providers.
public enum AIProvider: String, CaseIterable, Codable, Sendable {
    case groq = "GROQ"
    case openAI = "OPENAI"
    case claude = "CLAUDE"
    case custom = "CUSTOM"

    public var displayName: String {
        switch self {
        case .groq:
            return "Groq Cloud (LPU)"
        case .openAI:
            return "OpenAI (GPT-4o & Whisper)"
        case .claude:
            return "Anthropic Claude (3.5)"
        case .custom:
            return "Custom / OpenAI-Compatible"
        }
    }

    public var shortName: String {
        switch self {
        case .groq: return "Groq"
        case .openAI: return "OpenAI"
        case .claude: return "Claude"
        case .custom: return "Custom"
        }
    }

    public var badgeText: String {
        switch self {
        case .groq: return "Free-Tier"
        case .openAI: return "GPT-4o / Whisper"
        case .claude: return "Claude 3.5"
        case .custom: return "OpenRouter / DeepSeek"
        }
    }

    public var subtitle: String {
        switch self {
        case .groq:
            return "Ultra-fast inference (Llama 3.3, Qwen 2.5, Whisper Large v3)"
        case .openAI:
            return "State-of-the-art multimodal AI (GPT-4o, GPT-4o-mini, Whisper-1)"
        case .claude:
            return "Nuanced reasoning & executive briefs (Claude 3.5 Sonnet / Haiku)"
        case .custom:
            return "Connect OpenRouter, DeepSeek, Ollama, Together AI, or local vLLM"
        }
    }

    public var defaultBaseUrl: String {
        switch self {
        case .groq: return "https://api.groq.com/openai/v1"
        case .openAI: return "https://api.openai.com/v1"
        case .claude: return "https://api.anthropic.com/v1"
        case .custom: return "https://openrouter.ai/api/v1"
        }
    }

    public var defaultSummarizerModel: String {
        switch self {
        case .groq: return "llama-3.3-70b-versatile"
        case .openAI: return "gpt-4o-mini"
        case .claude: return "claude-3-5-haiku-20241022"
        case .custom: return "deepseek/deepseek-chat"
        }
    }

    public var defaultTranscriptionModel: String {
        switch self {
        case .groq: return "whisper-large-v3-turbo"
        case .openAI: return "whisper-1"
        case .claude: return "" // Claude has no transcription endpoint; falls back to OpenAI/Groq/WhisperKit
        case .custom: return "whisper-1"
        }
    }

    public var consoleUrl: String {
        switch self {
        case .groq: return "https://console.groq.com/keys"
        case .openAI: return "https://platform.openai.com/api-keys"
        case .claude: return "https://console.anthropic.com/settings/keys"
        case .custom: return "https://openrouter.ai/keys"
        }
    }
}

/// Active processing engine selection.
public enum EngineMode: String, CaseIterable, Codable, Sendable {
    case cloudOnly = "CLOUD_ONLY"
    case localOnly = "LOCAL_ONLY"
    case autoHybrid = "AUTO_HYBRID"

    public var displayName: String {
        switch self {
        case .cloudOnly:
            return "Cloud API (Groq, OpenAI, Claude)"
        case .localOnly:
            return "100% On-Device (WhisperKit)"
        case .autoHybrid:
            return "Auto / Hybrid Fallback"
        }
    }

    public var modeDescription: String {
        switch self {
        case .cloudOnly:
            return "Direct cloud transcription and summarization via your configured AI provider."
        case .localOnly:
            return "Completely offline and private. Uses Apple Neural Engine with local CoreML models."
        case .autoHybrid:
            return "Uses cloud models for maximum speed when online, automatically falling back to on-device models when offline."
        }
    }
}

/// Global user configuration and live telemetry statistics.
public struct AppPreferences: Codable, Sendable, Equatable {
    public var engineMode: EngineMode
    public var selectedProvider: AIProvider
    public var groqApiKey: String
    public var openAIApiKey: String
    public var claudeApiKey: String
    public var customApiKey: String
    public var customBaseUrl: String
    public var customSummarizerModel: String
    public var customTranscriptionModel: String
    public var isWhisperModelDownloaded: Bool
    public var isLlmModelDownloaded: Bool
    public var whisperModelPath: String?
    public var llmModelPath: String?
    public var showTagsOnCards: Bool
    public var cardClickAction: String
    public var autoSummarization: Bool
    public var summaryStyle: String
    public var autoTagging: Bool
    public var themeMode: String
    public var isLoggedIn: Bool
    public var userName: String
    public var userEmail: String
    public var userAvatarInitial: String
    public var isCloudBackupEnabled: Bool
    public var backupOnlyOnWifi: Bool
    public var cloudBackupFolder: String
    public var lastBackupTimeFormatted: String

    // Real-Time Cloud & Local Telemetry
    public var totalTokensUsed: Int64
    public var totalPromptTokens: Int64
    public var totalCompletionTokens: Int64
    public var lastCallTokens: Int64
    public var lastCallPromptTokens: Int64
    public var lastCallCompletionTokens: Int64
    public var lastCallLatencyMs: Int64
    public var lastCallStatusCode: String
    public var lastCallProvider: String

    // Per-Provider Telemetry
    public var groqTotalTokens: Int64
    public var groqPromptTokens: Int64
    public var groqCompletionTokens: Int64
    public var groqCallsCount: Int64

    // Local On-Device Telemetry
    public var localTotalTokens: Int64
    public var localPromptTokens: Int64
    public var localCompletionTokens: Int64
    public var localGemmaCallsCount: Int64
    public var localWhisperCallsCount: Int64
    public var lastDailyResetTime: Int64

    public init(
        engineMode: EngineMode = .cloudOnly,
        selectedProvider: AIProvider = .groq,
        groqApiKey: String = "",
        openAIApiKey: String = "",
        claudeApiKey: String = "",
        customApiKey: String = "",
        customBaseUrl: String = "https://openrouter.ai/api/v1",
        customSummarizerModel: String = "deepseek/deepseek-chat",
        customTranscriptionModel: String = "whisper-1",
        isWhisperModelDownloaded: Bool = false,
        isLlmModelDownloaded: Bool = false,
        whisperModelPath: String? = nil,
        llmModelPath: String? = nil,
        showTagsOnCards: Bool = true,
        cardClickAction: String = "READER",
        autoSummarization: Bool = true,
        summaryStyle: String = "CONCISE",
        autoTagging: Bool = true,
        themeMode: String = "OBSIDIAN_DARK",
        isLoggedIn: Bool = false,
        userName: String = "Not logged in",
        userEmail: String = "",
        userAvatarInitial: String = "?",
        isCloudBackupEnabled: Bool = false,
        backupOnlyOnWifi: Bool = true,
        cloudBackupFolder: String = "ReelLibrary_Vault",
        lastBackupTimeFormatted: String = "Never backed up",
        totalTokensUsed: Int64 = 0,
        totalPromptTokens: Int64 = 0,
        totalCompletionTokens: Int64 = 0,
        lastCallTokens: Int64 = 0,
        lastCallPromptTokens: Int64 = 0,
        lastCallCompletionTokens: Int64 = 0,
        lastCallLatencyMs: Int64 = 0,
        lastCallStatusCode: String = "",
        lastCallProvider: String = "",
        groqTotalTokens: Int64 = 0,
        groqPromptTokens: Int64 = 0,
        groqCompletionTokens: Int64 = 0,
        groqCallsCount: Int64 = 0,
        localTotalTokens: Int64 = 0,
        localPromptTokens: Int64 = 0,
        localCompletionTokens: Int64 = 0,
        localGemmaCallsCount: Int64 = 0,
        localWhisperCallsCount: Int64 = 0,
        lastDailyResetTime: Int64 = 0
    ) {
        self.engineMode = engineMode
        self.selectedProvider = selectedProvider
        self.groqApiKey = groqApiKey
        self.openAIApiKey = openAIApiKey
        self.claudeApiKey = claudeApiKey
        self.customApiKey = customApiKey
        self.customBaseUrl = customBaseUrl
        self.customSummarizerModel = customSummarizerModel
        self.customTranscriptionModel = customTranscriptionModel
        self.isWhisperModelDownloaded = isWhisperModelDownloaded
        self.isLlmModelDownloaded = isLlmModelDownloaded
        self.whisperModelPath = whisperModelPath
        self.llmModelPath = llmModelPath
        self.showTagsOnCards = showTagsOnCards
        self.cardClickAction = cardClickAction
        self.autoSummarization = autoSummarization
        self.summaryStyle = summaryStyle
        self.autoTagging = autoTagging
        self.themeMode = themeMode
        self.isLoggedIn = isLoggedIn
        self.userName = userName
        self.userEmail = userEmail
        self.userAvatarInitial = userAvatarInitial
        self.isCloudBackupEnabled = isCloudBackupEnabled
        self.backupOnlyOnWifi = backupOnlyOnWifi
        self.cloudBackupFolder = cloudBackupFolder
        self.lastBackupTimeFormatted = lastBackupTimeFormatted
        self.totalTokensUsed = totalTokensUsed
        self.totalPromptTokens = totalPromptTokens
        self.totalCompletionTokens = totalCompletionTokens
        self.lastCallTokens = lastCallTokens
        self.lastCallPromptTokens = lastCallPromptTokens
        self.lastCallCompletionTokens = lastCallCompletionTokens
        self.lastCallLatencyMs = lastCallLatencyMs
        self.lastCallStatusCode = lastCallStatusCode
        self.lastCallProvider = lastCallProvider
        self.groqTotalTokens = groqTotalTokens
        self.groqPromptTokens = groqPromptTokens
        self.groqCompletionTokens = groqCompletionTokens
        self.groqCallsCount = groqCallsCount
        self.localTotalTokens = localTotalTokens
        self.localPromptTokens = localPromptTokens
        self.localCompletionTokens = localCompletionTokens
        self.localGemmaCallsCount = localGemmaCallsCount
        self.localWhisperCallsCount = localWhisperCallsCount
        self.lastDailyResetTime = lastDailyResetTime
    }
}

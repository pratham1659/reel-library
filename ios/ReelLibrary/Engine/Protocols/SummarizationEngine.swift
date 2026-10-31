import Foundation

// MARK: - ReelSummary Protocol Extension & Convenience Properties
extension ReelSummary: Equatable {
    public static func == (lhs: ReelSummary, rhs: ReelSummary) -> Bool {
        lhs.title == rhs.title &&
        lhs.oneLineSummary == rhs.oneLineSummary &&
        lhs.actionableTakeaways == rhs.actionableTakeaways &&
        lhs.tags == rhs.tags
    }

    public var one_line_summary: String {
        oneLineSummary
    }

    public var actionable_takeaways: [String] {
        actionableTakeaways
    }

    public init(
        title: String,
        one_line_summary: String,
        actionable_takeaways: [String],
        tags: [String]
    ) {
        self.init(
            title: title,
            oneLineSummary: one_line_summary,
            actionableTakeaways: actionable_takeaways,
            tags: tags
        )
    }
}

public protocol SummarizationEngine: Sendable {
    var name: String { get }
    func summarize(caption: String?, transcript: String) async throws -> ReelSummary
}

public struct FailingSummarizerEngine: SummarizationEngine, Sendable {
    public let name: String = "Summarizer Engine Error"
    public let reason: String

    public init(reason: String) {
        self.reason = reason
    }

    public func summarize(caption: String?, transcript: String) async throws -> ReelSummary {
        throw NSError(
            domain: "SummarizationEngine",
            code: -1,
            userInfo: [NSLocalizedDescriptionKey: reason]
        )
    }
}

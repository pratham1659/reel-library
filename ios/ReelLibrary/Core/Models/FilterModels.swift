//
//  FilterModels.swift
//  ReelLibrary
//

import Foundation

/// Ordering criteria for sorting reels in the vault.
public enum OrderBy: String, CaseIterable, Codable, Sendable {
    case date = "date"
    case title = "title"
    case creator = "creator"

    public var displayName: String {
        switch self {
        case .date:
            return "Date"
        case .title:
            return "Title"
        case .creator:
            return "Creator"
        }
    }

    public var sfSymbolName: String {
        switch self {
        case .date:
            return "calendar"
        case .title:
            return "textformat"
        case .creator:
            return "person"
        }
    }
}

/// Sort direction for query ordering.
public enum SortDirection: String, CaseIterable, Codable, Sendable {
    case ascending = "asc"
    case descending = "desc"

    public var displayName: String {
        switch self {
        case .ascending:
            return "Ascending"
        case .descending:
            return "Descending"
        }
    }

    public var sfSymbolName: String {
        switch self {
        case .ascending:
            return "arrow.up"
        case .descending:
            return "arrow.down"
        }
    }
}

/// Grouping mode for organizing vault sections.
public enum GroupBy: String, CaseIterable, Codable, Sendable {
    case none = "none"
    case date = "date"
    case creator = "creator"

    public var displayName: String {
        switch self {
        case .none:
            return "None"
        case .date:
            return "Date"
        case .creator:
            return "Creator"
        }
    }

    public var sfSymbolName: String {
        switch self {
        case .none:
            return "rectangle.grid.1x2"
        case .date:
            return "calendar"
        case .creator:
            return "person.2"
        }
    }
}

/// Display presentation mode for the vault screen.
public enum ViewMode: String, CaseIterable, Codable, Sendable {
    case grid = "grid"
    case list = "list"

    public var displayName: String {
        switch self {
        case .grid:
            return "Grid"
        case .list:
            return "List"
        }
    }

    public var sfSymbolName: String {
        switch self {
        case .grid:
            return "square.grid.2x2"
        case .list:
            return "list.bullet"
        }
    }
}

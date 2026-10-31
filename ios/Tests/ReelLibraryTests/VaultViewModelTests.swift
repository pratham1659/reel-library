//
//  VaultViewModelTests.swift
//  ReelLibraryTests
//

import XCTest
import GRDB
@testable import ReelLibrary

final class VaultViewModelTests: XCTestCase {
    var inMemoryDatabase: AppDatabase!
    var viewModel: VaultViewModel!

    override func setUpWithError() throws {
        try super.setUpWithError()
        inMemoryDatabase = try AppDatabase.makeInMemory()
        viewModel = VaultViewModel(database: inMemoryDatabase)
    }

    override func tearDownWithError() throws {
        viewModel = nil
        inMemoryDatabase = nil
        try super.tearDownWithError()
    }

    // MARK: - Initial State Tests

    func testInitialStateAndPrepopulatedData() {
        XCTAssertFalse(viewModel.reels.isEmpty, "Prepopulated demo reels should be available on initial load.")
        XCTAssertEqual(viewModel.viewMode, .grid, "Default view mode should be grid.")
        XCTAssertFalse(viewModel.isSelectionMode, "Default selection mode should be disabled.")
        XCTAssertTrue(viewModel.selectedIds.isEmpty, "No items should be selected initially.")
        XCTAssertNil(viewModel.selectedTag)
        XCTAssertNil(viewModel.selectedCreator)
        XCTAssertNil(viewModel.selectedStatus)
    }

    // MARK: - Search Filtering Tests

    func testSearchFilteringByTitle() {
        viewModel.searchQuery = "sleep"
        let filtered = viewModel.filteredReels
        XCTAssertFalse(filtered.isEmpty)
        XCTAssertTrue(filtered.allSatisfy { reel in
            (reel.title?.localizedCaseInsensitiveContains("sleep") == true) ||
            (reel.caption?.localizedCaseInsensitiveContains("sleep") == true) ||
            (reel.oneLineSummary?.localizedCaseInsensitiveContains("sleep") == true) ||
            (reel.transcript?.localizedCaseInsensitiveContains("sleep") == true)
        })

        // Clear query returns full library
        viewModel.searchQuery = ""
        XCTAssertEqual(viewModel.filteredReels.count, viewModel.reels.count)
    }

    func testSearchFilteringNonExistent() {
        viewModel.searchQuery = "non_existent_xyz_query_12345"
        XCTAssertTrue(viewModel.filteredReels.isEmpty)
    }

    // MARK: - Tag Selection Tests

    func testTagSelectionFiltering() {
        viewModel.selectedTag = "neuroscience"
        let filtered = viewModel.filteredReels
        XCTAssertFalse(filtered.isEmpty)
        XCTAssertTrue(filtered.allSatisfy { $0.tags.contains("neuroscience") })

        // Clearing tag resets filter
        viewModel.selectedTag = nil
        XCTAssertEqual(viewModel.filteredReels.count, viewModel.reels.count)
    }

    // MARK: - Creator Filtering Tests

    func testCreatorFiltering() {
        viewModel.selectedCreator = "hubermanlab"
        let filtered = viewModel.filteredReels
        XCTAssertFalse(filtered.isEmpty)
        XCTAssertTrue(filtered.allSatisfy { $0.displayCreator.localizedCaseInsensitiveContains("hubermanlab") })

        viewModel.selectedCreator = nil
        XCTAssertEqual(viewModel.filteredReels.count, viewModel.reels.count)
    }

    // MARK: - Status Filtering Tests

    func testStatusFiltering() {
        viewModel.selectedStatus = .completed
        let filtered = viewModel.filteredReels
        XCTAssertTrue(filtered.allSatisfy { $0.status == .completed })

        viewModel.selectedStatus = .failed
        XCTAssertTrue(viewModel.filteredReels.isEmpty)
    }

    // MARK: - View Mode Switching Tests

    func testViewModeSwitching() {
        XCTAssertEqual(viewModel.viewMode, .grid)
        viewModel.viewMode = .list
        XCTAssertEqual(viewModel.viewMode, .list)
        viewModel.viewMode = .grid
        XCTAssertEqual(viewModel.viewMode, .grid)
    }

    // MARK: - Ordering & Sort Direction Tests

    func testOrderingByTitleAscending() {
        viewModel.orderBy = .title
        viewModel.sortDirection = .ascending
        let filtered = viewModel.filteredReels

        for i in 0..<(filtered.count - 1) {
            let current = filtered[i].displayTitle.lowercased()
            let next = filtered[i + 1].displayTitle.lowercased()
            XCTAssertLessThanOrEqual(current, next)
        }
    }

    func testOrderingByTitleDescending() {
        viewModel.orderBy = .title
        viewModel.sortDirection = .descending
        let filtered = viewModel.filteredReels

        for i in 0..<(filtered.count - 1) {
            let current = filtered[i].displayTitle.lowercased()
            let next = filtered[i + 1].displayTitle.lowercased()
            XCTAssertGreaterThanOrEqual(current, next)
        }
    }

    // MARK: - Multi-Selection & Batch Actions Tests

    func testToggleSelectionMode() {
        XCTAssertFalse(viewModel.isSelectionMode)
        viewModel.toggleSelectionMode()
        XCTAssertTrue(viewModel.isSelectionMode)

        if let firstId = viewModel.reels.first?.id {
            viewModel.toggleSelect(id: firstId)
            XCTAssertTrue(viewModel.selectedIds.contains(firstId))

            viewModel.toggleSelect(id: firstId)
            XCTAssertFalse(viewModel.selectedIds.contains(firstId))
        }

        viewModel.toggleSelectionMode()
        XCTAssertFalse(viewModel.isSelectionMode)
        XCTAssertTrue(viewModel.selectedIds.isEmpty)
    }

    func testSelectAllAndClear() {
        viewModel.isSelectionMode = true
        viewModel.selectAll()
        XCTAssertEqual(viewModel.selectedIds.count, viewModel.reels.count)

        viewModel.clearSelection()
        XCTAssertTrue(viewModel.selectedIds.isEmpty)
        XCTAssertFalse(viewModel.isSelectionMode)
    }

    func testBatchDeletion() {
        let initialCount = viewModel.reels.count
        guard let first = viewModel.reels.first, let id = first.id else {
            XCTFail("Reels should have valid ids")
            return
        }

        viewModel.isSelectionMode = true
        viewModel.toggleSelect(id: id)
        XCTAssertEqual(viewModel.selectedIds.count, 1)

        viewModel.deleteSelected()

        XCTAssertEqual(viewModel.reels.count, initialCount - 1)
        XCTAssertFalse(viewModel.reels.contains { $0.id == id })
        XCTAssertTrue(viewModel.selectedIds.isEmpty)
        XCTAssertFalse(viewModel.isSelectionMode)
    }

    // MARK: - Notes & Tag Mutation Tests

    func testSaveNotesAndTagMutations() {
        guard let first = viewModel.reels.first, let id = first.id else {
            XCTFail("Reel missing id")
            return
        }

        // Test notes update
        let testNotes = "Interesting protocol on circadian alignment."
        viewModel.saveNotes(id: id, notes: testNotes)
        let updatedReel = viewModel.reels.first { $0.id == id }
        XCTAssertEqual(updatedReel?.userNotes, testNotes)

        // Test tag addition
        viewModel.addTag(id: id, tag: "new_custom_tag")
        let reelWithTag = viewModel.reels.first { $0.id == id }
        XCTAssertTrue(reelWithTag?.tags.contains("new_custom_tag") == true)

        // Test tag removal
        viewModel.removeTag(id: id, tag: "new_custom_tag")
        let reelWithoutTag = viewModel.reels.first { $0.id == id }
        XCTAssertFalse(reelWithoutTag?.tags.contains("new_custom_tag") == true)
    }

    // MARK: - AI Search Suggestions Tests

    func testAiSearchSuggestions() {
        viewModel.isAiSearchEnabled = true
        let suggestions = viewModel.aiSearchSuggestions
        XCTAssertFalse(suggestions.isEmpty)

        viewModel.searchQuery = "sleep"
        let filteredSuggestions = viewModel.aiSearchSuggestions
        XCTAssertTrue(filteredSuggestions.contains("sleep"))
    }
}

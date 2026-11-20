import XCTest
@testable import Podkop

@MainActor
private final class ControlledFeedLoader: FeedLoading {
    struct Call {
        let request: FeedRequest
        let query: FeedQuery
        let finish: (Result<FeedPage, Error>) -> Void
    }
    var calls: [Call] = []
    var hitRows: [Resource] = []

    func policy(for query: FeedQuery) -> FeedPolicy {
        query.loggedIn && query.tab != .upcoming
            ? FeedPolicy(kind: "CursorInPage", initial: FeedRequest(kind: "initial"))
            : FeedPolicy(kind: "Numbered", initial: FeedRequest(kind: "number", value: "1"))
    }
    func nextRequest(for query: FeedQuery, next: String?, nextNumber: Int) -> FeedRequest? {
        query.loggedIn && query.tab != .upcoming
            ? next.map { FeedRequest(kind: "pageCursor", value: $0) }
            : FeedRequest(kind: "number", value: next ?? String(nextNumber))
    }
    func load(_ request: FeedRequest, query: FeedQuery) async throws -> FeedPage {
        try await withCheckedThrowingContinuation { continuation in
            calls.append(Call(request: request, query: query) { continuation.resume(with: $0) })
        }
    }
    func hits() async throws -> [Resource] { hitRows }
    func complete(_ index: Int, items: [Resource], next: String? = nil, total: Int? = nil,
                  file: StaticString = #filePath, line: UInt = #line) {
        call(index, file: file, line: line)?(.success(FeedPage(items: items, next: next, total: total)))
    }
    func fail(_ index: Int, file: StaticString = #filePath, line: UInt = #line) {
        call(index, file: file, line: line)?(.failure(NSError(domain: "fixture", code: 1)))
    }
    private func call(_ index: Int, file: StaticString, line: UInt) -> ((Result<FeedPage, Error>) -> Void)? {
        guard calls.indices.contains(index) else {
            XCTFail("Call \(index) was never made; \(calls.count) so far", file: file, line: line)
            return nil
        }
        return calls[index].finish
    }
}

@MainActor
final class FeedTests: XCTestCase {
    private func waitForCalls(_ loader: ControlledFeedLoader, _ count: Int,
                              file: StaticString = #filePath, line: UInt = #line) async throws {
        try await waitUntil("\(count) feed requests", file: file, line: line) { loader.calls.count >= count }
    }
    /// Only for checking that something did not happen, after waiting for what did.
    private func settle() async {
        for _ in 0..<10 { await Task.yield() }
    }
    private func row(_ id: Int) -> Resource {
        Resource(sourceID: id, kind: .link, body: "row \(id)")
    }

    func testNumberedPageRetryAndDeduplication() async throws {
        let loader = ControlledFeedLoader()
        let model = FeedModel(tab: .upcoming, loggedIn: false, loader: loader)
        model.start()
        try await waitForCalls(loader, 1)
        XCTAssertEqual(loader.calls[0].request, FeedRequest(kind: "number", value: "1"))
        loader.complete(0, items: [row(1)], next: "2")
        try await waitUntil("first page") { model.phase == .loaded }
        model.loadNext(); model.loadNext()
        try await waitForCalls(loader, 2)
        XCTAssertEqual(loader.calls.count, 2)
        XCTAssertEqual(loader.calls[1].request.value, "2")
        loader.fail(1)
        try await waitUntil("next page error") { model.nextError }
        XCTAssertTrue(model.nextError)
        XCTAssertEqual(model.items.map(\.sourceID), [1])
        model.retry()
        try await waitForCalls(loader, 3)
        XCTAssertEqual(loader.calls[2].request, loader.calls[1].request)
        loader.complete(2, items: [row(1), row(2)], next: "2")
        try await waitUntil("retried page") { !model.nextLoading }
        XCTAssertEqual(model.items.map(\.sourceID), [1, 2])
        XCTAssertTrue(model.exhausted) // repeated page request
    }

    func testSortChangeDiscardsLatePageAndRefreshFailureKeepsRows() async throws {
        let loader = ControlledFeedLoader()
        let model = FeedModel(tab: .links, loggedIn: true, loader: loader)
        model.start()
        try await waitForCalls(loader, 1)
        XCTAssertEqual(loader.calls[0].request.kind, "initial")
        loader.complete(0, items: [row(1)], next: "opaque-2")
        try await waitUntil("first page") { model.phase == .loaded }
        model.loadNext()
        try await waitForCalls(loader, 2)
        XCTAssertEqual(loader.calls[1].request, FeedRequest(kind: "pageCursor", value: "opaque-2"))
        model.select(sort: "active")
        try await waitForCalls(loader, 3)
        loader.complete(1, items: [row(99)], next: "stale")
        loader.fail(2)
        try await waitUntil("failed sort refresh") { model.refreshError }
        await settle()
        XCTAssertEqual(model.items.map(\.sourceID), [1])
        XCTAssertEqual(model.phase, .loaded)
        XCTAssertTrue(model.refreshError)
        Task { await model.refresh() }
        try await waitForCalls(loader, 4)
        loader.complete(3, items: [row(3)], next: nil)
        try await waitUntil("refreshed page") { model.phase == .loaded }
        XCTAssertEqual(model.items.map(\.sourceID), [3])
        XCTAssertFalse(model.refreshError)
    }

    func testAccountSwitchClearsRowsAndChangesPolicy() async throws {
        let loader = ControlledFeedLoader()
        let model = FeedModel(tab: .entries, loggedIn: true, loader: loader)
        model.start()
        try await waitForCalls(loader, 1)
        loader.complete(0, items: [row(7)], next: "cursor")
        try await waitUntil("first page") { model.phase == .loaded }
        model.setSession(false)
        XCTAssertTrue(model.items.isEmpty)
        try await waitForCalls(loader, 2)
        XCTAssertEqual(loader.calls[1].request, FeedRequest(kind: "number", value: "1"))
        loader.complete(1, items: [], total: 0)
        try await waitUntil("guest page") { model.phase == .loaded }
        XCTAssertTrue(model.exhausted)
    }

    func testPullRefreshDuringPagingKeepsCursorAfterFailure() async throws {
        let loader = ControlledFeedLoader()
        let model = FeedModel(tab: .links, loggedIn: true, loader: loader)
        model.start()
        try await waitForCalls(loader, 1)
        loader.complete(0, items: [row(1)], next: "cursor-2")
        try await waitUntil("first page") { model.phase == .loaded }
        model.loadNext()
        try await waitForCalls(loader, 2)
        let refresh = Task { await model.refresh() }
        try await waitForCalls(loader, 3)
        loader.complete(1, items: [row(99)], next: "stale")
        loader.fail(2)
        await refresh.value
        XCTAssertEqual(model.items.map(\.sourceID), [1])
        model.loadNext()
        try await waitForCalls(loader, 4)
        XCTAssertEqual(loader.calls[3].request, FeedRequest(kind: "pageCursor", value: "cursor-2"))
        loader.complete(3, items: [row(2)])
        try await waitUntil("next page") { !model.nextLoading }
        XCTAssertEqual(model.items.map(\.sourceID), [1, 2])
    }

    func testStaleFeedShowsRefreshPromptUntilItReloads() async throws {
        let loader = ControlledFeedLoader()
        let model = FeedModel(tab: .entries, loggedIn: false, loader: loader)
        let start = Date(timeIntervalSince1970: 1_000_000)
        model.screenOpened(at: start) // before the first load, like Android's hasBeenInitialized
        XCTAssertFalse(model.showsRefreshPrompt)
        model.start()
        try await waitForCalls(loader, 1)
        loader.complete(0, items: [row(1)], next: nil)
        try await waitUntil("first page") { model.phase == .loaded }

        model.screenOpened(at: start.addingTimeInterval(2 * 60 * 60))
        XCTAssertFalse(model.showsRefreshPrompt, "two hours is not stale yet")
        model.screenOpened(at: start.addingTimeInterval(2 * 60 * 60 + FeedModel.staleThreshold + 1))
        XCTAssertTrue(model.showsRefreshPrompt)

        let refresh = Task { await model.refresh() }
        try await waitForCalls(loader, 2)
        XCTAssertFalse(model.showsRefreshPrompt, "any reload hides the prompt")
        loader.complete(1, items: [row(1)], next: nil)
        await refresh.value
    }
}

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
    func complete(_ index: Int, items: [Resource], next: String? = nil, total: Int? = nil) {
        calls[index].finish(.success(FeedPage(items: items, next: next, total: total)))
    }
    func fail(_ index: Int) {
        calls[index].finish(.failure(NSError(domain: "fixture", code: 1)))
    }
}

@MainActor
final class FeedTests: XCTestCase {
    private func waitForCalls(_ loader: ControlledFeedLoader, _ count: Int) async {
        for _ in 0..<100 where loader.calls.count < count { await Task.yield() }
        XCTAssertGreaterThanOrEqual(loader.calls.count, count)
    }
    private func settle() async {
        for _ in 0..<10 { await Task.yield() }
    }
    private func row(_ id: Int) -> Resource {
        Resource(sourceID: id, kind: .link, body: "row \(id)")
    }

    func testNumberedPageRetryAndDeduplication() async {
        let loader = ControlledFeedLoader()
        let model = FeedModel(tab: .upcoming, loggedIn: false, loader: loader)
        model.start()
        await waitForCalls(loader, 1)
        XCTAssertEqual(loader.calls[0].request, FeedRequest(kind: "number", value: "1"))
        loader.complete(0, items: [row(1)], next: "2")
        await settle()
        model.loadNext(); model.loadNext()
        await waitForCalls(loader, 2)
        XCTAssertEqual(loader.calls.count, 2)
        XCTAssertEqual(loader.calls[1].request.value, "2")
        loader.fail(1)
        await settle()
        XCTAssertTrue(model.nextError)
        XCTAssertEqual(model.items.map(\.sourceID), [1])
        model.retry()
        await waitForCalls(loader, 3)
        XCTAssertEqual(loader.calls[2].request, loader.calls[1].request)
        loader.complete(2, items: [row(1), row(2)], next: "2")
        await settle()
        XCTAssertEqual(model.items.map(\.sourceID), [1, 2])
        XCTAssertTrue(model.exhausted) // repeated page request
    }

    func testSortChangeDiscardsLatePageAndRefreshFailureKeepsRows() async {
        let loader = ControlledFeedLoader()
        let model = FeedModel(tab: .links, loggedIn: true, loader: loader)
        model.start()
        await waitForCalls(loader, 1)
        XCTAssertEqual(loader.calls[0].request.kind, "initial")
        loader.complete(0, items: [row(1)], next: "opaque-2")
        await settle()
        model.loadNext()
        await waitForCalls(loader, 2)
        XCTAssertEqual(loader.calls[1].request, FeedRequest(kind: "pageCursor", value: "opaque-2"))
        model.select(sort: "active")
        await waitForCalls(loader, 3)
        loader.complete(1, items: [row(99)], next: "stale")
        loader.fail(2)
        await settle()
        XCTAssertEqual(model.items.map(\.sourceID), [1])
        XCTAssertEqual(model.phase, .loaded)
        XCTAssertTrue(model.refreshError)
        Task { await model.refresh() }
        await waitForCalls(loader, 4)
        loader.complete(3, items: [row(3)], next: nil)
        await settle()
        XCTAssertEqual(model.items.map(\.sourceID), [3])
        XCTAssertFalse(model.refreshError)
    }

    func testAccountSwitchClearsRowsAndChangesPolicy() async {
        let loader = ControlledFeedLoader()
        let model = FeedModel(tab: .entries, loggedIn: true, loader: loader)
        model.start()
        await waitForCalls(loader, 1)
        loader.complete(0, items: [row(7)], next: "cursor")
        await settle()
        model.setSession(false)
        XCTAssertTrue(model.items.isEmpty)
        await waitForCalls(loader, 2)
        XCTAssertEqual(loader.calls[1].request, FeedRequest(kind: "number", value: "1"))
        loader.complete(1, items: [], total: 0)
        await settle()
        XCTAssertTrue(model.exhausted)
    }

    func testPullRefreshDuringPagingKeepsCursorAfterFailure() async {
        let loader = ControlledFeedLoader()
        let model = FeedModel(tab: .links, loggedIn: true, loader: loader)
        model.start()
        await waitForCalls(loader, 1)
        loader.complete(0, items: [row(1)], next: "cursor-2")
        await settle()
        model.loadNext()
        await waitForCalls(loader, 2)
        let refresh = Task { await model.refresh() }
        await waitForCalls(loader, 3)
        loader.complete(1, items: [row(99)], next: "stale")
        loader.fail(2)
        await refresh.value
        XCTAssertEqual(model.items.map(\.sourceID), [1])
        model.loadNext()
        await waitForCalls(loader, 4)
        XCTAssertEqual(loader.calls[3].request, FeedRequest(kind: "pageCursor", value: "cursor-2"))
        loader.complete(3, items: [row(2)])
        await settle()
        XCTAssertEqual(model.items.map(\.sourceID), [1, 2])
    }
}

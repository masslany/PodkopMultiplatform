import Foundation
import Observation
import PodkopShared

struct ListPage<Item> {
    let items: [Item]
    /// Resolved by the shared layer; nil means the list is exhausted.
    let next: FeedRequest?
    let total: Int?
}

/// Paged list state for discovery/account lists. The shared layer decides every next request;
/// this type owns loading phases, generations, deduplication and retry only.
@MainActor @Observable
final class ListPager<Item> {
    enum Phase: Equatable { case idle, loading, loaded, failed }
    typealias Loader = (_ request: FeedRequest, _ loaded: Int) async throws -> ListPage<Item>

    private(set) var phase: Phase = .idle
    private(set) var items: [Item] = []
    private(set) var total: Int?
    private(set) var nextLoading = false
    private(set) var nextError = false
    private(set) var refreshError = false
    private(set) var isRefreshing = false
    var exhausted: Bool { next == nil }

    private let key: (Item) -> String
    private var loader: Loader?
    private var first: FeedRequest?
    private var next: FeedRequest?
    private var seen = Set<FeedRequest>()
    private var generation = 0
    private var active: Task<Void, Never>?

    init(key: @escaping (Item) -> String) { self.key = key }

    /// Starts a new list, discarding rows and any late response from the previous one.
    func load(first request: FeedRequest, loader: @escaping Loader) {
        self.loader = loader
        first = request
        items = []
        total = nil
        run(request, replacing: true)
    }

    /// Reloads the first page while keeping rows visible; a failure keeps the current rows.
    func refresh() async {
        guard let first, loader != nil else { return }
        await run(first, replacing: true).value
    }

    func loadNext() {
        guard phase == .loaded, !nextLoading, !nextError, !isRefreshing,
              active == nil, let next else { return }
        nextLoading = true
        run(next, replacing: false)
    }

    func loadNextIfNeeded(after item: Item) {
        guard let last = items.last, key(last) == key(item) else { return }
        loadNext()
    }

    func retry() {
        if nextError {
            nextError = false
            loadNext()
        } else if phase == .failed, let first, loader != nil {
            run(first, replacing: true)
        }
    }

    /// Restarts a first page interrupted by `stop()`.
    func resume() {
        guard phase == .idle, let first, loader != nil else { return }
        run(first, replacing: true)
    }

    func stop() {
        generation += 1
        active?.cancel()
        active = nil
        nextLoading = false
        isRefreshing = false
        if phase == .loading { phase = .idle }
    }

    /// Applies confirmed changes; returning nil removes the row.
    func update(_ transform: (Item) -> Item?) {
        let before = items.count
        items = items.compactMap(transform)
        if let total { self.total = max(0, total - (before - items.count)) }
    }

    @discardableResult
    private func run(_ request: FeedRequest, replacing: Bool) -> Task<Void, Never> {
        generation += 1
        active?.cancel()
        let token = generation
        nextError = false
        if replacing {
            refreshError = false
            nextLoading = false
            if items.isEmpty { phase = .loading } else { isRefreshing = true }
        }
        let loaded = replacing ? 0 : items.count
        let loader = self.loader
        let task = Task { [weak self] in
            guard let loader else { return }
            do {
                let page = try await loader(request, loaded)
                guard let self, token == self.generation, !Task.isCancelled else { return }
                self.apply(page, request: request, replacing: replacing)
            } catch is CancellationError {
                return
            } catch {
                guard let self, token == self.generation else { return }
                self.fail(replacing: replacing)
            }
            if let self, token == self.generation { self.active = nil }
        }
        active = task
        return task
    }

    private func apply(_ page: ListPage<Item>, request: FeedRequest, replacing: Bool) {
        var keys = Set(replacing ? [] : items.map(key))
        let fresh = page.items.filter { keys.insert(key($0)).inserted }
        if replacing {
            items = fresh
            seen = [request]
        } else {
            items.append(contentsOf: fresh)
            seen.insert(request)
        }
        total = page.total
        let repeated = page.next.map { seen.contains($0) } ?? false
        // A page of only duplicates cannot advance the list; stop instead of looping.
        next = repeated || (!replacing && fresh.isEmpty) ? nil : page.next
        phase = .loaded
        nextLoading = false
        isRefreshing = false
    }

    private func fail(replacing: Bool) {
        if replacing {
            phase = items.isEmpty ? .failed : .loaded
            refreshError = !items.isEmpty
            isRefreshing = false
        } else {
            nextError = true
            nextLoading = false
        }
    }
}

extension ListPager where Item == NativeResource {
    convenience init() {
        self.init(key: { "\($0.id):\($0.parentID ?? 0)" })
    }
}

extension ListPage where Item == NativeResource {
    init(_ value: IOSResourceListPage) {
        items = value.items.map(NativeResource.init)
        next = value.next.map(FeedRequest.init)
        total = value.total?.intValue
    }
}

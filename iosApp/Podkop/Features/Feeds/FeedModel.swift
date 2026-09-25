import Foundation
import Observation
import PodkopShared

@MainActor @Observable
final class FeedModel {
    enum Phase: Equatable { case idle, loading, loaded, refreshing, failed }
    private(set) var phase: Phase = .idle
    private(set) var items: [Resource] = []
    private(set) var hits: [Resource] = []
    private(set) var nextLoading = false
    private(set) var nextError = false
    private(set) var refreshError = false
    private(set) var exhausted = false
    private(set) var query: FeedQuery
    var gallery = false

    private let loader: FeedLoading
    private let updates: ResourceUpdates?
    private var generation = 0
    private var sessionRevision = 0
    private var appliedUpdateRevision = 0
    private var active: Task<Void, Never>?
    private var hitTask: Task<Void, Never>?
    private var next: FeedRequest?
    private var nextNumber = 1
    private var seenRequests = Set<FeedRequest>()
    private struct PageState {
        let next: FeedRequest?
        let nextNumber: Int
        let seen: Set<FeedRequest>
        let exhausted: Bool
    }

    init(tab: AppTab, loggedIn: Bool, loader: FeedLoading, updates: ResourceUpdates? = nil) {
        query = FeedQuery(tab: tab, loggedIn: loggedIn,
                          sort: tab == .entries ? "hot" : tab == .upcoming ? "active" : "newest")
        self.loader = loader
        self.updates = updates
    }

    func start() {
        guard phase == .idle else { return }
        reload()
        if query.tab == .links { loadHits() }
    }

    func setSession(_ loggedIn: Bool, revision: Int = 0) {
        guard query.loggedIn != loggedIn || sessionRevision != revision else { return }
        sessionRevision = revision
        query.loggedIn = loggedIn
        // Rows from another account must never remain visible after a session change.
        items = []
        hits = []
        reload()
        if query.tab == .links { loadHits() }
    }

    func select(sort: String, hotHours: Int? = nil) {
        guard query.sort != sort || (hotHours != nil && query.hotHours != hotHours) else { return }
        let resetHotPeriod = sort == "hot" && query.sort != "hot" && hotHours == nil
        query.sort = sort
        if let hotHours { query.hotHours = hotHours }
        else if resetHotPeriod { query.hotHours = 12 }
        reload()
    }

    func refresh() async { await reload(preservingPagination: true).value }

    @discardableResult private func reload(preservingPagination: Bool = false) -> Task<Void, Never> {
        let backup = preservingPagination
            ? PageState(next: next, nextNumber: nextNumber, seen: seenRequests, exhausted: exhausted)
            : nil
        generation += 1
        active?.cancel()
        next = nil
        nextError = false
        refreshError = false
        nextLoading = false
        exhausted = false
        nextNumber = 1
        seenRequests.removeAll()
        phase = items.isEmpty ? .loading : .refreshing
        let token = generation
        let current = query
        let request = loader.policy(for: current).initial
        let job = Task { [weak self] in
            guard let self else { return }
            await self.perform(request, query: current, generation: token, replacing: true, backup: backup)
        }
        active = job
        return job
    }

    func loadNext() {
        guard phase == .loaded, !nextLoading, !nextError, !exhausted,
              active == nil, let request = next else { return }
        nextLoading = true
        let token = generation
        let current = query
        active = Task { [weak self] in
            guard let self else { return }
            await self.perform(request, query: current, generation: token, replacing: false)
        }
    }

    func retry() {
        if nextError {
            nextError = false
            loadNext()
        } else if phase == .failed {
            reload()
        }
    }

    private func perform(_ request: FeedRequest, query current: FeedQuery,
                         generation token: Int, replacing: Bool, backup: PageState? = nil) async {
        do {
            let page = try await loader.load(request, query: current)
            guard token == generation, !Task.isCancelled else { return }
            let pageItems = updates.map { store in page.items.compactMap(store.reconcile) } ?? page.items
            let fresh = replacing ? pageItems : pageItems.filter { item in
                !items.contains { $0.id == item.id && $0.parentID == item.parentID }
            }
            if replacing {
                var seen = Set<String>()
                items = fresh.filter { seen.insert("\($0.id):\($0.parentID ?? 0)").inserted }
            } else {
                items.append(contentsOf: fresh)
            }
            seenRequests.insert(request)
            let numbered = loader.policy(for: current).kind == "Numbered"
            if numbered {
                nextNumber += 1
            }
            let candidate = loader.nextRequest(for: current, next: page.next, nextNumber: nextNumber)
            let reachedTotal = page.total.map { items.count >= $0 } ?? false
            let repeated = candidate.map { seenRequests.contains($0) } ?? false
            exhausted = page.items.isEmpty || (!replacing && fresh.isEmpty) || reachedTotal || repeated || candidate == nil
            next = exhausted ? nil : candidate
            phase = .loaded
            nextLoading = false
            nextError = false
            refreshError = false
        } catch is CancellationError {
            // A cancelled request belongs to an older generation or a hidden screen.
        } catch {
            guard token == generation else { return }
            if replacing {
                phase = items.isEmpty ? .failed : .loaded
                refreshError = !items.isEmpty
                if let backup, !items.isEmpty {
                    next = backup.next
                    nextNumber = backup.nextNumber
                    seenRequests = backup.seen
                    exhausted = backup.exhausted
                }
            } else {
                nextError = true
                nextLoading = false
            }
        }
        if token == generation { active = nil }
    }

    private func loadHits() {
        hitTask?.cancel()
        let token = generation
        hitTask = Task { [weak self] in
            guard let self else { return }
            if let rows = try? await loader.hits(), token == generation, !Task.isCancelled {
                hits = updates.map { store in rows.compactMap(store.reconcile) } ?? rows
            }
        }
    }

    func stop() {
        generation += 1
        active?.cancel()
        hitTask?.cancel()
        active = nil
        hitTask = nil
        nextLoading = false
        if phase == .loading || phase == .refreshing { phase = .idle }
    }

    func reconcile(_ updates: ResourceUpdates) {
        let mustReload = updates.needsReload(items, since: appliedUpdateRevision)
        appliedUpdateRevision = updates.revision
        items = items.compactMap(updates.reconcile)
        hits = hits.compactMap(updates.reconcile)
        if mustReload { reload() }
    }
}

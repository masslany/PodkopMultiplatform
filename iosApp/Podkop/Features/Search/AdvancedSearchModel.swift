import Foundation
import Observation
import PodkopShared

@MainActor @Observable
final class AdvancedSearchModel {
    var form: AdvancedSearchForm { didSet { if oldValue != form { validation = nil } } }
    private(set) var validation: AdvancedSearchValidation?
    private(set) var hasSearched = false
    let results = ListPager<Resource>()
    private let service: AdvancedSearching
    private let updates: ResourceUpdates?
    private var appliedUpdateRevision = 0

    var canSearch: Bool {
        !form.query.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
            && results.phase != .loading && !results.isRefreshing
    }

    init(initialQuery: String, service: AdvancedSearching, updates: ResourceUpdates? = nil) {
        form = AdvancedSearchForm(query: initialQuery)
        self.service = service
        self.updates = updates
    }

    /// Builds the request once so a relative date window stays fixed while paging.
    func search() {
        switch service.build(form) {
        case .failure(let error):
            validation = error
        case .success(let request):
            validation = nil
            hasSearched = true
            let service = self.service
            results.load(first: service.firstRequest()) { [updates] page, loaded in
                let value = try await service.load(request, request: page, loaded: loaded)
                guard let updates else { return value }
                return ListPage(items: value.items.compactMap(updates.reconcile),
                                next: value.next, total: value.total)
            }
        }
    }

    func refresh() async { await results.refresh() }

    func resume() { results.resume() }

    func stop() { results.stop() }

    func reconcile(_ updates: ResourceUpdates) {
        let reload = updates.needsReload(results.items, since: appliedUpdateRevision)
        appliedUpdateRevision = updates.revision
        results.update(updates.reconcile)
        if reload { Task { await results.refresh() } }
    }
}

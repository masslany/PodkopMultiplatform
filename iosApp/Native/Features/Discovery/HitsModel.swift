import Foundation
import Observation

@MainActor @Observable
final class HitsModel {
    enum Sort: String, CaseIterable { case all, day, week, month, year }
    private(set) var sort: Sort = .day
    private(set) var archive: HitsArchive?
    let pager = ListPager<NativeResource>()
    private let loader: CollectionLoading
    private let updates: ResourceUpdates?
    private var appliedUpdateRevision = 0

    init(loader: CollectionLoading, updates: ResourceUpdates? = nil) {
        self.loader = loader
        self.updates = updates
    }

    func start() { if pager.phase == .idle { reload() } }

    func select(_ sort: Sort) {
        guard sort != self.sort || archive != nil else { return }
        self.sort = sort
        archive = nil
        reload()
    }

    /// Archive months always use the `all` sort, as on Android.
    func select(archive: HitsArchive) {
        guard HitsArchive.isAvailable(year: archive.year, month: archive.month) else { return }
        sort = .all
        self.archive = archive
        reload()
    }

    func refresh() async { await pager.refresh() }
    func stop() { pager.stop() }

    private func reload() {
        let sort = sort.rawValue, archive = archive, loader = loader, updates = updates
        pager.load(first: loader.hitsFirstRequest()) { request, loaded in
            let page = try await loader.hits(sort: sort, archive: archive, request: request, loaded: loaded)
            return reconciled(page, updates)
        }
    }

    func reconcile(_ updates: ResourceUpdates) {
        let reload = updates.needsReload(pager.items, since: appliedUpdateRevision)
        appliedUpdateRevision = updates.revision
        pager.update(updates.reconcile)
        if reload { Task { await pager.refresh() } }
    }
}

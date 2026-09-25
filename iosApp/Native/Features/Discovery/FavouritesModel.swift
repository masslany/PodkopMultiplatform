import Foundation
import Observation

@MainActor @Observable
final class FavouritesModel {
    enum Sort: String, CaseIterable { case newest, oldest }
    enum Kind: String, CaseIterable { case all, link, entry, linkComment, entryComment }
    private(set) var sort: Sort = .newest
    private(set) var kind: Kind = .all
    let pager = ListPager<NativeResource>()
    private let loader: CollectionLoading
    private let updates: ResourceUpdates?
    private var isLoggedIn: Bool
    private var appliedUpdateRevision = 0

    init(loader: CollectionLoading, isLoggedIn: Bool, updates: ResourceUpdates? = nil) {
        self.loader = loader
        self.isLoggedIn = isLoggedIn
        self.updates = updates
    }

    func start() { if pager.phase == .idle { reload() } }

    func select(sort: Sort) {
        guard sort != self.sort else { return }
        self.sort = sort
        reload()
    }

    func select(kind: Kind) {
        guard kind != self.kind else { return }
        self.kind = kind
        reload()
    }

    func setSession(_ loggedIn: Bool) {
        isLoggedIn = loggedIn
        reload()
    }

    func refresh() async { await pager.refresh() }
    func stop() { pager.stop() }

    private func reload() {
        let sort = sort.rawValue, kind = kind.rawValue, loader = loader, updates = updates
        pager.load(first: loader.favouritesFirstRequest(isLoggedIn: isLoggedIn)) { request, loaded in
            let page = try await loader.favourites(sort: sort, type: kind, request: request, loaded: loaded)
            return reconciled(page, updates, keepFavouritesOnly: true)
        }
    }

    /// An unfavourited or deleted resource leaves this list immediately.
    func reconcile(_ updates: ResourceUpdates) {
        let reload = updates.needsReload(pager.items, since: appliedUpdateRevision)
        appliedUpdateRevision = updates.revision
        pager.update { original in
            updates.reconcile(original).flatMap { unfavourited(original, $0) ? nil : $0 }
        }
        if reload { Task { await pager.refresh() } }
    }
}

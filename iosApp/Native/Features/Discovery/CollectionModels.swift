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

@MainActor @Observable
final class RankModel {
    let pager = ListPager<NativeRankUser>(key: \.username)
    private let loader: CollectionLoading

    init(loader: CollectionLoading) { self.loader = loader }

    func start() {
        guard pager.phase == .idle else { return }
        let loader = loader
        pager.load(first: loader.rankFirstRequest()) { try await loader.rank(request: $0, loaded: $1) }
    }

    func refresh() async { await pager.refresh() }
    func stop() { pager.stop() }
}

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

@MainActor @Observable
final class ObservedModel {
    enum Kind: String, CaseIterable { case all, profiles, discussions, tags }
    private(set) var kind: Kind = .all
    let pager = ListPager<NativeObservedItem>(key: \.id)
    private let loader: CollectionLoading
    private let updates: ResourceUpdates?
    private var appliedUpdateRevision = 0

    init(loader: CollectionLoading, updates: ResourceUpdates? = nil) {
        self.loader = loader
        self.updates = updates
    }

    func start() { if pager.phase == .idle { reload() } }

    func select(_ kind: Kind) {
        guard kind != self.kind else { return }
        self.kind = kind
        reload()
    }

    func refresh() async { await pager.refresh() }
    func stop() { pager.stop() }

    private func reload() {
        let kind = kind.rawValue, loader = loader, updates = updates
        pager.load(first: loader.observedFirstRequest()) { request, loaded in
            let page = try await loader.observed(type: kind, request: request, loaded: loaded)
            guard let updates else { return page }
            return ListPage(items: page.items.compactMap { item in
                updates.reconcile(item.resource).map {
                    NativeObservedItem(resource: $0, newContentCount: item.newContentCount)
                }
            }, next: page.next, total: page.total)
        }
    }

    func reconcile(_ updates: ResourceUpdates) {
        let reload = updates.needsReload(pager.items.map(\.resource), since: appliedUpdateRevision)
        appliedUpdateRevision = updates.revision
        pager.update { item in
            updates.reconcile(item.resource).map {
                NativeObservedItem(resource: $0, newContentCount: item.newContentCount)
            }
        }
        if reload { Task { await pager.refresh() } }
    }
}

@MainActor
func reconciled(_ page: ListPage<NativeResource>, _ updates: ResourceUpdates?,
                keepFavouritesOnly: Bool = false) -> ListPage<NativeResource> {
    guard let updates else { return page }
    let items = page.items.compactMap { original in
        updates.reconcile(original).flatMap {
            keepFavouritesOnly && unfavourited(original, $0) ? nil : $0
        }
    }
    return ListPage(items: items, next: page.next, total: page.total)
}

/// Only a confirmed change removes a favourite; server rows are trusted as listed.
private func unfavourited(_ original: NativeResource, _ current: NativeResource) -> Bool {
    original.favourite && !current.favourite
}

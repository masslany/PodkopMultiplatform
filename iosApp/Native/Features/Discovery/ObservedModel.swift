import Foundation
import Observation

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

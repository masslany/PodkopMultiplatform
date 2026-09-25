import Foundation
import Observation
import PodkopShared

@MainActor @Observable
final class TagModel {
    enum Sort: String, CaseIterable { case all, best }
    enum Kind: String, CaseIterable { case all, link, entry }
    enum Action: Hashable { case observe, notifications, blacklist }

    let tag: String
    private(set) var details: TagDetails?
    private(set) var detailsFailed = false
    private(set) var sort: Sort = .all
    private(set) var kind: Kind = .all
    private(set) var isLoggedIn: Bool
    private(set) var pending = Set<Action>()
    private(set) var actionFailed = false
    var gallery = false
    let pager = ListPager<Resource>()

    private let loader: TagLoading
    private let updates: ResourceUpdates?
    private var detailsTask: Task<Void, Never>?
    private var generation = 0
    private var appliedUpdateRevision = 0

    /// Entries with a photo, in stream order, as Android's gallery shows them.
    var galleryItems: [Resource] {
        pager.items.filter { $0.kind == .entry && $0.photo != nil && $0.deletion == nil }
    }

    init(tag: String, isLoggedIn: Bool, loader: TagLoading, updates: ResourceUpdates? = nil) {
        self.tag = tag
        self.isLoggedIn = isLoggedIn
        self.loader = loader
        self.updates = updates
    }

    func start() {
        guard pager.phase == .idle else { return }
        if details == nil { loadDetails() }
        reloadStream()
    }

    func select(sort: Sort) {
        guard sort != self.sort else { return }
        self.sort = sort
        reloadStream()
    }

    func select(kind: Kind) {
        guard kind != self.kind else { return }
        self.kind = kind
        reloadStream()
    }

    func refresh() async {
        loadDetails()
        await pager.refresh()
    }

    /// A new session may change observation state and the stream's pagination policy.
    func setSession(_ loggedIn: Bool) {
        generation += 1
        isLoggedIn = loggedIn
        pending.removeAll()
        details = nil
        loadDetails()
        reloadStream()
    }

    func stop() {
        pager.stop()
        detailsTask?.cancel()
    }

    func toggle(_ action: Action) {
        guard isLoggedIn, let current = details, !pending.contains(action) else { return }
        if action == .notifications && !current.observed { return }
        pending.insert(action)
        actionFailed = false
        let token = generation, loader = loader, tag = tag
        Task { [weak self] in
            do {
                switch action {
                case .observe: try await loader.setObserved(tag, !current.observed)
                case .notifications: try await loader.setNotifications(tag, !current.notificationsEnabled)
                case .blacklist: try await loader.setBlacklisted(tag, !current.blacklisted)
                }
                guard let self, token == self.generation else { return }
                self.pending.remove(action)
                self.applyConfirmed(action)
            } catch {
                guard let self, token == self.generation else { return }
                self.pending.remove(action)
                if !(error is CancellationError) { self.actionFailed = true }
            }
        }
    }

    func dismissActionFailure() { actionFailed = false }

    /// State changes only after the server confirms, as on Android; failures leave it untouched.
    private func applyConfirmed(_ action: Action) {
        guard var value = details else { return }
        switch action {
        case .observe:
            value.observed.toggle()
            if !value.observed { value.notificationsEnabled = false }
        case .notifications:
            value.notificationsEnabled.toggle()
        case .blacklist:
            value.blacklisted.toggle()
        }
        details = value
    }

    private func loadDetails() {
        detailsTask?.cancel()
        detailsFailed = false
        let token = generation, loader = loader, tag = tag
        detailsTask = Task { [weak self] in
            do {
                let value = try await loader.details(tag)
                guard let self, token == self.generation, !Task.isCancelled else { return }
                self.details = value
            } catch is CancellationError {
                return
            } catch {
                guard let self, token == self.generation else { return }
                self.detailsFailed = true
            }
        }
    }

    private func reloadStream() {
        let sort = sort.rawValue, kind = kind.rawValue, loader = loader, updates = updates, tag = tag
        pager.load(first: loader.firstRequest(isLoggedIn: isLoggedIn)) { request, loaded in
            let page = try await loader.stream(tag, sort: sort, type: kind, request: request, loaded: loaded)
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

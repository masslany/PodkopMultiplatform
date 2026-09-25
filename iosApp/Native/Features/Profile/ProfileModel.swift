import Foundation
import Observation
import PodkopShared

@MainActor @Observable
final class ProfileModel {
    enum Phase: Equatable { case loading, loaded, failed }
    enum Action: Hashable { case observe, blacklist }
    struct Note: Equatable {
        var content = ""
        var saved = ""
        var loading = false
        var failed = false
        var saving = false
        var saveFailed = false
        var loaded = false
        var canSave: Bool { loaded && !loading && !saving && content != saved }
    }

    private(set) var phase: Phase = .loading
    private(set) var profile: NativeProfile?
    private(set) var summary: ProfileSummary = .actions
    private(set) var section: ProfileSection = .actions
    private(set) var pending = Set<Action>()
    private(set) var actionFailed = false
    private(set) var badges: [NativeBadge] = []
    private(set) var badgesFailed = false
    private(set) var badgesLoaded = false
    private(set) var noteSaved = false
    var note = Note()
    var detailsExpanded = false

    /// One pager per section, so returning to a section shows its cached rows as on Android.
    private(set) var pagers: [ProfileSection: ListPager<ProfileRow>] = [:]
    var pager: ListPager<ProfileRow> { pager(for: section) }

    private let requestedUsername: String?
    private(set) var username: String?
    private let loader: ProfileLoading
    private let updates: ResourceUpdates?
    private var generation = 0
    private var appliedUpdateRevision = 0

    /// Pass nil to show the signed-in viewer's own profile.
    init(username: String?, loader: ProfileLoading, updates: ResourceUpdates? = nil) {
        requestedUsername = username
        self.username = username
        self.loader = loader
        self.updates = updates
    }

    func start() {
        if profile == nil && phase != .failed { load() }
        else if pager.phase == .idle { loadSection(section) }
    }

    func retry() { load() }

    func stop() { pagers.values.forEach { $0.stop() } }

    /// Session changes can alter permissions, note access and the own-profile identity.
    func setSession() { load() }

    func load() {
        generation += 1
        let token = generation
        phase = .loading
        profile = nil
        pagers.values.forEach { $0.stop() }
        pagers = [:]
        summary = .actions
        section = .actions
        pending = []
        note = Note()
        badges = []
        badgesFailed = false
        badgesLoaded = false
        detailsExpanded = false
        let loader = loader, requested = requestedUsername
        Task { [weak self] in
            do {
                let name: String
                if let requested { name = requested } else { name = try await loader.ownUsername() }
                let value = try await loader.profile(name)
                guard let self, token == self.generation else { return }
                self.username = value.username
                self.profile = value
                self.phase = .loaded
                self.loadSection(.actions)
                self.loadBadges(token: token)
                if value.isLoggedIn && !value.isOwnProfile { self.loadNote(token: token) }
            } catch is CancellationError {
                return
            } catch {
                guard let self, token == self.generation else { return }
                self.phase = .failed
            }
        }
    }

    func select(summary: ProfileSummary) {
        guard summary != self.summary, let first = summary.sections.first else { return }
        self.summary = summary
        select(section: first)
    }

    func select(section: ProfileSection) {
        guard summary.sections.contains(section) else { return }
        self.section = section
        if pager(for: section).phase == .idle { loadSection(section) }
    }

    func refresh() async { await pager.refresh() }

    private func pager(for section: ProfileSection) -> ListPager<ProfileRow> {
        if let existing = pagers[section] { return existing }
        let created = ListPager<ProfileRow>(key: \.id)
        pagers[section] = created
        return created
    }

    private func loadSection(_ section: ProfileSection) {
        guard let username else { return }
        let loader = loader, updates = updates
        pager(for: section).load(first: loader.firstSectionRequest()) { request, loaded in
            let page = try await loader.section(username, section, request: request, loaded: loaded)
            guard let updates else { return page }
            return ListPage(items: page.items.compactMap { reconcileRow($0, updates) }, next: page.next, total: page.total)
        }
    }

    // MARK: Details

    func retryDetails() {
        if badgesFailed { loadBadges(token: generation) }
        if note.failed { loadNote(token: generation) }
    }

    private func loadBadges(token: Int) {
        guard let username else { return }
        badgesFailed = false
        let loader = loader
        Task { [weak self] in
            do {
                let values = try await loader.badges(username)
                guard let self, token == self.generation else { return }
                self.badges = values
                self.badgesLoaded = true
            } catch {
                guard let self, token == self.generation, !(error is CancellationError) else { return }
                self.badgesFailed = true
            }
        }
    }

    private func loadNote(token: Int) {
        guard let username else { return }
        note.loading = true
        note.failed = false
        let loader = loader
        Task { [weak self] in
            do {
                let content = try await loader.note(username)
                guard let self, token == self.generation else { return }
                self.note = Note(content: content, saved: content, loaded: true)
            } catch {
                guard let self, token == self.generation, !(error is CancellationError) else { return }
                self.note.loading = false
                self.note.failed = true
            }
        }
    }

    /// A failed save keeps the edited text so nothing typed is lost.
    func saveNote() {
        guard note.canSave, let username else { return }
        let content = note.content, token = generation, loader = loader
        note.saving = true
        note.saveFailed = false
        noteSaved = false
        Task { [weak self] in
            do {
                try await loader.saveNote(username, content)
                guard let self, token == self.generation else { return }
                self.note.saved = content
                self.note.saving = false
                self.noteSaved = true
            } catch {
                guard let self, token == self.generation else { return }
                self.note.saving = false
                self.note.saveFailed = !(error is CancellationError)
            }
        }
    }

    func dismissNoteSaved() { noteSaved = false }

    // MARK: Actions

    func toggle(_ action: Action) {
        guard let current = profile, let username, !pending.contains(action) else { return }
        switch action {
        case .observe: guard current.canManageObservation else { return }
        case .blacklist: guard current.canBlacklist else { return }
        }
        pending.insert(action)
        actionFailed = false
        let token = generation, loader = loader
        Task { [weak self] in
            do {
                switch action {
                case .observe: try await loader.setObserved(username, !current.observed)
                case .blacklist: try await loader.setBlacklisted(username, !current.blacklisted)
                }
                guard let self, token == self.generation else { return }
                self.pending.remove(action)
                guard var value = self.profile else { return }
                switch action {
                case .observe:
                    value.observed.toggle()
                    value.followers = max(0, value.followers + (value.observed ? 1 : -1))
                case .blacklist:
                    value.blacklisted.toggle()
                }
                self.profile = value
            } catch {
                guard let self, token == self.generation else { return }
                self.pending.remove(action)
                if !(error is CancellationError) { self.actionFailed = true }
            }
        }
    }

    func dismissActionFailure() { actionFailed = false }

    func reconcile(_ updates: ResourceUpdates) {
        let resources = pagers.values.flatMap { $0.items }.compactMap { row -> NativeResource? in
            if case .resource(let value) = row { return value }
            return nil
        }
        let reload = updates.needsReload(resources, since: appliedUpdateRevision)
        appliedUpdateRevision = updates.revision
        pagers.values.forEach { $0.update { reconcileRow($0, updates) } }
        if reload { Task { await pager.refresh() } }
    }
}

@MainActor
private func reconcileRow(_ row: ProfileRow, _ updates: ResourceUpdates) -> ProfileRow? {
    guard case .resource(let value) = row else { return row }
    return updates.reconcile(value).map(ProfileRow.resource)
}

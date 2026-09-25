import Foundation
import Observation
import PodkopShared

struct NativeProfile: Equatable {
    var username: String
    var avatarURL: String? = nil
    var bannerURL: String? = nil
    var rankPosition: Int?
    var color: String
    var gender: String
    var memberSince: String?
    var actions: Int
    var links: Int
    var entries: Int
    var followers: Int
    var following: Int
    var observed: Bool
    var blacklisted: Bool
    var isLoggedIn: Bool
    var isOwnProfile: Bool
    var canManageObservation: Bool
    var canBlacklist: Bool
    var canSendPrivateMessage: Bool
}

struct NativeBadge: Identifiable, Equatable {
    let label: String
    let slug: String
    let description: String
    let colorHex: String
    let level: Int?
    let progress: Int?
    let achievedAt: String?
    var id: String { slug + label }
}

enum ProfileRow: Identifiable {
    case resource(NativeResource)
    case user(username: String, color: String, gender: String, online: Bool, verified: Bool, avatarURL: String? = nil)
    case tag(name: String, pinned: Bool)

    var id: String {
        switch self {
        case .resource(let value): "resource:\(value.id):\(value.parentID ?? 0)"
        case .user(let name, _, _, _, _, _): "user:\(name)"
        case .tag(let name, _): "tag:\(name)"
        }
    }
}

enum ProfileSummary: String, CaseIterable {
    case actions, links, entries, followers, following

    var sections: [ProfileSection] {
        switch self {
        case .actions: [.actions]
        case .links: [.linksAdded, .linksPublished, .linksCommented, .linksRelated, .linksUp, .linksDown]
        case .entries: [.entriesAdded, .entriesCommented, .entriesVoted]
        case .followers: [.followers]
        case .following: [.followingTags, .followingUsers]
        }
    }
}

enum ProfileSection: String, CaseIterable {
    case actions, entriesAdded, entriesVoted, entriesCommented
    case linksAdded, linksPublished, linksUp, linksDown, linksCommented, linksRelated
    case followers, followingTags, followingUsers
}

@MainActor protocol ProfileLoading {
    func ownUsername() async throws -> String
    func profile(_ username: String) async throws -> NativeProfile
    func badges(_ username: String) async throws -> [NativeBadge]
    func note(_ username: String) async throws -> String
    func saveNote(_ username: String, _ content: String) async throws
    func setObserved(_ username: String, _ enabled: Bool) async throws
    func setBlacklisted(_ username: String, _ enabled: Bool) async throws
    func firstSectionRequest() -> FeedRequest
    func section(_ username: String, _ section: ProfileSection, request: FeedRequest, loaded: Int) async throws
        -> ListPage<ProfileRow>
}

@MainActor
final class SharedProfileLoader: ProfileLoading {
    private let client: PodkopClient
    private let adapter: BridgeAdapter

    init(client: PodkopClient, adapter: BridgeAdapter) {
        self.client = client
        self.adapter = adapter
    }

    func ownUsername() async throws -> String {
        try await adapter.call { self.client.profile.ownUsername(completion: $0) }
    }

    func profile(_ username: String) async throws -> NativeProfile {
        let value: IOSProfile = try await adapter.call {
            self.client.profile.load(username: username, completion: $0)
        }
        return NativeProfile(
            username: value.username, avatarURL: value.avatarUrl.nonEmpty,
            bannerURL: value.backgroundUrl.nonEmpty, rankPosition: value.rankPosition?.intValue, color: value.color,
            gender: value.gender, memberSince: value.memberSince, actions: Int(value.actions),
            links: Int(value.links), entries: Int(value.entries), followers: Int(value.followers),
            following: Int(value.following), observed: value.observed, blacklisted: value.blacklisted,
            isLoggedIn: value.isLoggedIn, isOwnProfile: value.isOwnProfile,
            canManageObservation: value.canManageObservation, canBlacklist: value.canBlacklist,
            canSendPrivateMessage: value.canSendPrivateMessage
        )
    }

    func badges(_ username: String) async throws -> [NativeBadge] {
        let values: [IOSProfileBadge] = try await adapter.call {
            self.client.profile.badges(username: username, completion: $0)
        }
        return values.map {
            NativeBadge(label: $0.label, slug: $0.slug, description: $0.description_, colorHex: $0.colorHex,
                        level: $0.level?.intValue, progress: $0.progress?.intValue, achievedAt: $0.achievedAt)
        }
    }

    func note(_ username: String) async throws -> String {
        try await adapter.call { self.client.profile.note(username: username, completion: $0) }
    }

    func saveNote(_ username: String, _ content: String) async throws {
        let _: IOSSuccess = try await adapter.call {
            self.client.profile.saveNote(username: username, content: content, completion: $0)
        }
    }

    func setObserved(_ username: String, _ enabled: Bool) async throws {
        let _: IOSSuccess = try await adapter.call {
            self.client.profile.setObserved(username: username, enabled: enabled, completion: $0)
        }
    }

    func setBlacklisted(_ username: String, _ enabled: Bool) async throws {
        let _: IOSSuccess = try await adapter.call {
            self.client.profile.setBlacklisted(username: username, enabled: enabled, completion: $0)
        }
    }

    func firstSectionRequest() -> FeedRequest { FeedRequest(client.profile.firstSectionRequest()) }

    func section(_ username: String, _ section: ProfileSection, request: FeedRequest, loaded: Int) async throws
        -> ListPage<ProfileRow> {
        let page: IOSProfileSectionPage = try await adapter.call {
            self.client.profile.section(username: username, section: section.rawValue,
                                        request: IOSPageRequest(kind: request.kind, value: request.value),
                                        loaded: Int32(loaded), completion: $0)
        }
        let rows: [ProfileRow] = page.resources.map { .resource(NativeResource($0)) }
            + page.users.map { .user(username: $0.username, color: $0.color, gender: $0.gender,
                                     online: $0.online, verified: $0.verified, avatarURL: $0.avatarUrl.nonEmpty) }
            + page.tags.map { .tag(name: $0.name, pinned: $0.pinned) }
        return ListPage(items: rows, next: page.next.map(FeedRequest.init), total: page.total?.intValue)
    }
}

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

#if DEBUG
@MainActor
final class FixtureProfileLoader: ProfileLoading {
    func ownUsername() async throws -> String { "Ja" }
    func profile(_ username: String) async throws -> NativeProfile {
        NativeProfile(username: username, rankPosition: 12, color: "orange", gender: "female",
                      memberSince: "2012-03-01T10:00", actions: 120, links: 10, entries: 90, followers: 400,
                      following: 7, observed: false, blacklisted: false, isLoggedIn: true,
                      isOwnProfile: username == "Ja", canManageObservation: username != "Ja",
                      canBlacklist: username != "Ja", canSendPrivateMessage: username != "Ja")
    }
    func badges(_ username: String) async throws -> [NativeBadge] {
        [NativeBadge(label: "Weteran", slug: "veteran", description: "10 lat na Wykopie", colorHex: "#ff9900",
                     level: 2, progress: 50, achievedAt: "2022-01-01T10:00")]
    }
    func note(_ username: String) async throws -> String { "" }
    func saveNote(_ username: String, _ content: String) async throws {}
    func setObserved(_ username: String, _ enabled: Bool) async throws {}
    func setBlacklisted(_ username: String, _ enabled: Bool) async throws {}
    func firstSectionRequest() -> FeedRequest { FeedRequest(kind: "number", value: "1") }
    func section(_ username: String, _ section: ProfileSection, request: FeedRequest, loaded: Int) async throws
        -> ListPage<ProfileRow> {
        switch section {
        case .followers, .followingUsers:
            ListPage(items: [.user(username: "Ewa-Żółw", color: "green", gender: "female", online: true, verified: false)],
                     next: nil, total: 1)
        case .followingTags:
            ListPage(items: [.tag(name: "technologia", pinned: true)], next: nil, total: 1)
        default:
            ListPage(items: [.resource(ContentFixtures.entry)], next: nil, total: 1)
        }
    }
}
#endif

extension String {
    /// Treats blank server strings as absent.
    var nonEmpty: String? { trimmingCharacters(in: .whitespaces).isEmpty ? nil : self }
}

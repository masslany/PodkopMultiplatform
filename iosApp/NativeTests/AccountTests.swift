import XCTest
@testable import PodkopNative

@MainActor
private final class ControlledProfile: ProfileLoading {
    let own = Pending<String>()
    let profiles = Pending<NativeProfile>()
    let notes = Pending<String>()
    let saves = Pending<Void>()
    let mutations = Pending<Void>()
    let sections = Pending<ListPage<ProfileRow>>()
    private(set) var badgeRequests: [String] = []

    func ownUsername() async throws -> String { try await own.wait("own") }
    func profile(_ username: String) async throws -> NativeProfile { try await profiles.wait(username) }
    func badges(_ username: String) async throws -> [NativeBadge] {
        badgeRequests.append(username)
        return []
    }
    func note(_ username: String) async throws -> String { try await notes.wait(username) }
    func saveNote(_ username: String, _ content: String) async throws { try await saves.wait(content) }
    func setObserved(_ username: String, _ enabled: Bool) async throws { try await mutations.wait("observe:\(enabled)") }
    func setBlacklisted(_ username: String, _ enabled: Bool) async throws { try await mutations.wait("block:\(enabled)") }
    func firstSectionRequest() -> FeedRequest { FeedRequest(kind: "number", value: "1") }
    func section(_ username: String, _ section: ProfileSection, request: FeedRequest, loaded: Int) async throws
        -> ListPage<ProfileRow> { try await sections.wait(section.rawValue) }
}

@MainActor
private final class ControlledBlacklists: BlacklistsLoading {
    let pages = Pending<ListPage<BlacklistEntry>>()
    let mutations = Pending<Void>()
    func firstRequest() -> FeedRequest { FeedRequest(kind: "number", value: "1") }
    func normalize(_ category: BlacklistCategory, _ value: String) -> String {
        let trimmed = value.trimmingCharacters(in: .whitespaces)
        return category == .tags ? trimmed.replacingOccurrences(of: "#", with: "").lowercased() : trimmed
    }
    func load(_ category: BlacklistCategory, request: FeedRequest, loaded: Int) async throws
        -> ListPage<BlacklistEntry> { try await pages.wait(category.rawValue) }
    func add(_ category: BlacklistCategory, _ value: String) async throws { try await mutations.wait("add:\(value)") }
    func remove(_ entry: BlacklistEntry) async throws { try await mutations.wait("remove:\(entry.value)") }
}

@MainActor
final class AccountTests: XCTestCase {
    private func settle() async { for _ in 0..<20 { await Task.yield() } }

    private func profile(_ name: String, own: Bool = false, observed: Bool = false, followers: Int = 10,
                         canManage: Bool = true, canBlacklist: Bool = true) -> NativeProfile {
        NativeProfile(username: name, rankPosition: nil, color: "orange", gender: "male", memberSince: nil,
                      actions: 1, links: 2, entries: 3, followers: followers, following: 4, observed: observed,
                      blacklisted: false, isLoggedIn: true, isOwnProfile: own, canManageObservation: canManage,
                      canBlacklist: canBlacklist, canSendPrivateMessage: !own)
    }

    // MARK: Profile (P28)

    func testOwnProfileResolvesViewerAndSkipsNote() async {
        let loader = ControlledProfile()
        let model = ProfileModel(username: nil, loader: loader)
        model.start()
        await settle()
        loader.own.succeed(0, "Ja")
        await settle()
        XCTAssertEqual(loader.profiles.calls.map(\.input), ["Ja"])
        loader.profiles.succeed(0, profile("Ja", own: true, canManage: false, canBlacklist: false))
        await settle()
        XCTAssertEqual(model.phase, .loaded)
        XCTAssertEqual(loader.sections.calls.map(\.input), ["actions"])
        XCTAssertEqual(loader.badgeRequests, ["Ja"])
        XCTAssertTrue(loader.notes.calls.isEmpty, "no note about yourself")
        model.toggle(.observe)
        model.toggle(.blacklist)
        await settle()
        XCTAssertTrue(loader.mutations.calls.isEmpty)
    }

    func testSectionsAreCachedWhenSwitchingBack() async {
        let loader = ControlledProfile()
        let model = ProfileModel(username: "ewa", loader: loader)
        model.start()
        await settle()
        loader.profiles.succeed(0, profile("ewa"))
        await settle()
        loader.notes.succeed(0, "")
        loader.sections.succeed(0, ListPage(items: [.resource(NativeResource(sourceID: 1, kind: .entry, body: ""))],
                                            next: nil, total: 1))
        await settle()
        model.select(summary: .following)
        XCTAssertEqual(model.section, .followingTags)
        await settle()
        loader.sections.succeed(1, ListPage(items: [.tag(name: "nauka", pinned: false)], next: nil, total: 1))
        await settle()
        model.select(section: .followingUsers)
        await settle()
        model.select(summary: .actions)
        await settle()
        XCTAssertEqual(loader.sections.calls.map(\.input), ["actions", "followingTags", "followingUsers"])
        XCTAssertEqual(model.pager.items.map(\.id), ["resource:entry:1:0"])
        model.select(section: .linksUp)
        XCTAssertEqual(model.section, .actions, "a section outside the summary is ignored")
    }

    func testNoteSaveFailureKeepsEditsAndSuccessMarksSaved() async {
        let loader = ControlledProfile()
        let model = ProfileModel(username: "ewa", loader: loader)
        model.start()
        await settle()
        loader.profiles.succeed(0, profile("ewa"))
        await settle()
        XCTAssertFalse(model.note.canSave, "cannot save before the note loaded")
        loader.notes.succeed(0, "old")
        await settle()
        model.note.content = "new note ✍️"
        XCTAssertTrue(model.note.canSave)
        model.saveNote()
        model.saveNote()
        await settle()
        XCTAssertEqual(loader.saves.calls.count, 1)
        loader.saves.fail(0)
        await settle()
        XCTAssertEqual(model.note.content, "new note ✍️")
        XCTAssertTrue(model.note.saveFailed)
        XCTAssertTrue(model.note.canSave)
        model.saveNote()
        await settle()
        loader.saves.succeed(1, ())
        await settle()
        XCTAssertEqual(model.note.saved, "new note ✍️")
        XCTAssertFalse(model.note.canSave)
        XCTAssertTrue(model.noteSaved)
    }

    func testObserveUpdatesFollowersOnlyAfterConfirmationAndRollsBackNothingOnFailure() async {
        let loader = ControlledProfile()
        let model = ProfileModel(username: "ewa", loader: loader)
        model.start()
        await settle()
        loader.profiles.succeed(0, profile("ewa", followers: 10))
        await settle()
        model.toggle(.observe)
        await settle()
        XCTAssertEqual(model.profile?.followers, 10)
        loader.mutations.succeed(0, ())
        await settle()
        XCTAssertEqual(model.profile?.observed, true)
        XCTAssertEqual(model.profile?.followers, 11)
        model.toggle(.blacklist)
        await settle()
        loader.mutations.fail(1)
        await settle()
        XCTAssertEqual(model.profile?.blacklisted, false)
        XCTAssertTrue(model.actionFailed)
    }

    func testSessionReloadDiscardsLateProfile() async {
        let loader = ControlledProfile()
        let model = ProfileModel(username: "ewa", loader: loader)
        model.start()
        await settle()
        model.setSession()
        await settle()
        loader.profiles.succeed(1, profile("ewa", followers: 2))
        await settle()
        loader.profiles.succeed(0, profile("ewa", followers: 1))
        await settle()
        XCTAssertEqual(model.profile?.followers, 2)
    }

    // MARK: Blacklists (P31)

    func testBlacklistSubmitNormalizesClearsInputAndRefreshes() async {
        let loader = ControlledBlacklists()
        let model = BlacklistCategoryModel(category: .tags, loader: loader, suggester: ControlledSuggestions(),
                                           debounce: .milliseconds(1))
        model.start()
        await settle()
        loader.pages.succeed(0, ListPage(items: [], next: nil, total: 0))
        await settle()
        model.input = "  #  "
        XCTAssertFalse(model.canSubmit)
        model.input = " #Nauka"
        XCTAssertTrue(model.canSubmit)
        model.submit()
        model.submit()
        await settle()
        XCTAssertEqual(loader.mutations.calls.map(\.input), ["add:nauka"])
        XCTAssertFalse(model.canSubmit, "busy while the add is in flight")
        loader.mutations.succeed(0, ())
        await settle()
        XCTAssertEqual(model.input, "")
        XCTAssertEqual(loader.pages.calls.count, 2, "the list reloads after a confirmed add")
    }

    func testBlacklistRemoveOnlyAfterConfirmationAndFailureKeepsRow() async {
        let loader = ControlledBlacklists()
        let model = BlacklistCategoryModel(category: .domains, loader: loader, suggester: ControlledSuggestions())
        model.start()
        await settle()
        let a = BlacklistEntry(category: .domains, value: "a.pl", color: nil, gender: nil)
        let b = BlacklistEntry(category: .domains, value: "b.pl", color: nil, gender: nil)
        loader.pages.succeed(0, ListPage(items: [a, b], next: nil, total: 2))
        await settle()
        model.remove(a)
        await settle()
        XCTAssertEqual(model.pager.items.count, 2)
        loader.mutations.fail(0)
        await settle()
        XCTAssertEqual(model.pager.items.count, 2)
        XCTAssertTrue(model.failed)
        model.remove(a)
        await settle()
        loader.mutations.succeed(1, ())
        await settle()
        XCTAssertEqual(model.pager.items.map(\.value), ["b.pl"])
        XCTAssertEqual(model.total, 1)
    }

    func testBlacklistSuggestionsSkipListedValuesAndDomains() async throws {
        let loader = ControlledBlacklists()
        let suggester = ControlledSuggestions()
        let model = BlacklistCategoryModel(category: .tags, loader: loader, suggester: suggester,
                                           debounce: .milliseconds(1))
        model.start()
        await settle()
        loader.pages.succeed(0, ListPage(items: [BlacklistEntry(category: .tags, value: "nauka",
                                                                color: nil, gender: nil)], next: nil, total: 1))
        await settle()
        model.input = "na"
        try await Task.sleep(for: .milliseconds(20))
        XCTAssertTrue(suggester.tagCalls.calls.isEmpty)
        model.input = "nau"
        try await Task.sleep(for: .milliseconds(30))
        suggester.tagCalls.succeed(0, [NativeTagSuggestion(name: "Nauka", followers: 1),
                                       NativeTagSuggestion(name: "nauka-polska", followers: 1)])
        await settle()
        XCTAssertEqual(model.suggestions.map(\.value), ["nauka-polska"])

        let domains = BlacklistCategoryModel(category: .domains, loader: loader, suggester: suggester,
                                             debounce: .milliseconds(1))
        domains.input = "example.com"
        try await Task.sleep(for: .milliseconds(20))
        XCTAssertEqual(suggester.tagCalls.calls.count, 1)
        XCTAssertEqual(domains.suggestionStatus, .hidden)
    }
}

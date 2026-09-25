import XCTest
@testable import Podkop

/// Suspends each call until the test completes it, so response order is under test control.
@MainActor
final class Pending<Value> {
    private(set) var calls: [(input: String, finish: (Result<Value, Error>) -> Void)] = []

    func wait(_ input: String) async throws -> Value {
        try await withCheckedThrowingContinuation { continuation in
            calls.append((input, { continuation.resume(with: $0) }))
        }
    }
    func succeed(_ index: Int, _ value: Value) { calls[index].finish(.success(value)) }
    func fail(_ index: Int) { calls[index].finish(.failure(NSError(domain: "fixture", code: 1))) }
}

@MainActor
final class ControlledSuggestions: SearchSuggesting {
    let tagCalls = Pending<[TagSuggestion]>()
    let userCalls = Pending<[UserSuggestion]>()
    func tags(_ query: String) async throws -> [TagSuggestion] { try await tagCalls.wait(query) }
    func users(_ query: String) async throws -> [UserSuggestion] { try await userCalls.wait(query) }
}

@MainActor
private final class ControlledCollections: CollectionLoading {
    let pages = Pending<ListPage<Resource>>()
    private(set) var requests: [(sort: String, archive: HitsArchive?, request: FeedRequest, loaded: Int)] = []
    private(set) var favouriteQueries: [(sort: String, type: String)] = []

    func hitsFirstRequest() -> FeedRequest { FeedRequest(kind: "number", value: "1") }
    func hits(sort: String, archive: HitsArchive?, request: FeedRequest, loaded: Int) async throws
        -> ListPage<Resource> {
        requests.append((sort, archive, request, loaded))
        return try await pages.wait(sort)
    }
    func rankFirstRequest() -> FeedRequest { FeedRequest(kind: "number", value: "1") }
    func rank(request: FeedRequest, loaded: Int) async throws -> ListPage<RankUser> {
        ListPage(items: [], next: nil, total: 0)
    }
    func favouritesFirstRequest(isLoggedIn: Bool) -> FeedRequest { FeedRequest(kind: "initial") }
    func favourites(sort: String, type: String, request: FeedRequest, loaded: Int) async throws
        -> ListPage<Resource> {
        favouriteQueries.append((sort, type))
        return try await pages.wait(type)
    }
    func observedFirstRequest() -> FeedRequest { FeedRequest(kind: "initial") }
    func observed(type: String, request: FeedRequest, loaded: Int) async throws
        -> ListPage<ObservedItem> { ListPage(items: [], next: nil, total: 0) }
}

@MainActor
private final class RecordingAdvancedSearch: AdvancedSearching {
    let pages = Pending<ListPage<Resource>>()
    private(set) var built: [AdvancedSearchForm] = []
    func build(_ form: AdvancedSearchForm) -> Result<AdvancedSearchRequest, AdvancedSearchValidation> {
        built.append(form)
        if form.query.trimmingCharacters(in: .whitespaces).isEmpty { return .failure(.queryRequired) }
        if form.datePreset == .custom && form.customDateFrom == "bad" { return .failure(.invalidCustomDateFormat) }
        return .success(AdvancedSearchRequest(value: nil))
    }
    func firstRequest() -> FeedRequest { FeedRequest(kind: "number", value: "1") }
    func load(_ query: AdvancedSearchRequest, request: FeedRequest, loaded: Int) async throws
        -> ListPage<Resource> { try await pages.wait(request.value ?? "") }
}

@MainActor
final class DiscoveryTests: XCTestCase {
    private func settle() async { for _ in 0..<20 { await Task.yield() } }
    private func row(_ id: Int, favourite: Bool = false) -> Resource {
        Resource(sourceID: id, kind: .entry, body: "row \(id)", favourite: favourite)
    }
    private func number(_ value: Int) -> FeedRequest { FeedRequest(kind: "number", value: String(value)) }

    // MARK: Search suggestions (P25)

    func testSuggestionsIgnoreShortQueriesAndDebounce() async throws {
        let service = ControlledSuggestions()
        let model = SearchModel(service: service, isLoggedIn: true, debounce: .milliseconds(20))
        model.query = "ab"
        try await Task.sleep(for: .milliseconds(60))
        XCTAssertTrue(service.tagCalls.calls.isEmpty)
        model.query = "kot"
        model.query = "kotl"
        try await Task.sleep(for: .milliseconds(80))
        XCTAssertEqual(service.tagCalls.calls.map(\.input), ["kotl"])
        XCTAssertEqual(service.userCalls.calls.map(\.input), ["kotl"])
    }

    func testOutOfOrderSuggestionResponsesKeepTheLatestQuery() async throws {
        let service = ControlledSuggestions()
        let model = SearchModel(service: service, isLoggedIn: false, debounce: .milliseconds(1))
        model.query = "first"
        try await Task.sleep(for: .milliseconds(30))
        model.query = "second"
        try await Task.sleep(for: .milliseconds(30))
        XCTAssertEqual(service.tagCalls.calls.count, 2)
        service.tagCalls.succeed(1, [TagSuggestion(name: "second", followers: 1)])
        await settle()
        service.tagCalls.succeed(0, [TagSuggestion(name: "first", followers: 1)])
        await settle()
        XCTAssertEqual(model.tags.map(\.name), ["second"])
        XCTAssertEqual(model.tagsStatus, .loaded)
        XCTAssertTrue(service.userCalls.calls.isEmpty, "guests never request user suggestions")
    }

    func testSuggestionFailureRetriesOnlyThatSection() async throws {
        let service = ControlledSuggestions()
        let model = SearchModel(service: service, isLoggedIn: true, debounce: .milliseconds(1))
        model.query = "wykop"
        try await Task.sleep(for: .milliseconds(30))
        service.tagCalls.fail(0)
        service.userCalls.succeed(0, [])
        await settle()
        XCTAssertEqual(model.tagsStatus, .failed)
        XCTAssertEqual(model.usersStatus, .loaded)
        model.retryTags()
        await settle()
        XCTAssertEqual(service.tagCalls.calls.count, 2)
        XCTAssertEqual(service.userCalls.calls.count, 1)
    }

    func testLogoutDropsUserSuggestions() async throws {
        let service = ControlledSuggestions()
        let model = SearchModel(service: service, isLoggedIn: true, debounce: .milliseconds(1))
        model.query = "wykop"
        try await Task.sleep(for: .milliseconds(30))
        model.setSession(false)
        service.userCalls.succeed(0, [UserSuggestion(username: "x", avatarURL: "", color: "", gender: "")])
        await settle()
        XCTAssertTrue(model.users.isEmpty)
        XCTAssertFalse(model.isLoggedIn)
    }

    func testStoppedSuggestionsResumeWhenVisibleAgain() async throws {
        let service = ControlledSuggestions()
        let model = SearchModel(service: service, isLoggedIn: false, debounce: .milliseconds(1))
        model.query = "wykop"
        try await Task.sleep(for: .milliseconds(30))
        model.stop()
        service.tagCalls.succeed(0, [TagSuggestion(name: "late", followers: 0)])
        await settle()
        XCTAssertTrue(model.tags.isEmpty)
        model.resume()
        try await Task.sleep(for: .milliseconds(30))
        XCTAssertEqual(service.tagCalls.calls.count, 2)
    }

    // MARK: Pager

    func testPagerDeduplicatesAndStopsOnRepeatedRequest() async {
        let pages = Pending<ListPage<Resource>>()
        let pager = ListPager<Resource>()
        pager.load(first: number(1)) { request, _ in try await pages.wait(request.value ?? "") }
        await settle()
        pages.succeed(0, ListPage(items: [row(1), row(2)], next: number(2), total: nil))
        await settle()
        pager.loadNext()
        await settle()
        pages.succeed(1, ListPage(items: [row(2), row(3)], next: number(1), total: nil))
        await settle()
        XCTAssertEqual(pager.items.map(\.sourceID), [1, 2, 3])
        XCTAssertTrue(pager.exhausted, "a next request already seen must not loop")
    }

    func testPagerNextErrorRetriesSameRequestAndRefreshFailureKeepsRows() async {
        let pages = Pending<ListPage<Resource>>()
        let pager = ListPager<Resource>()
        var loadedCounts: [Int] = []
        pager.load(first: number(1)) { request, loaded in
            loadedCounts.append(loaded)
            return try await pages.wait(request.value ?? "")
        }
        await settle()
        pages.succeed(0, ListPage(items: [row(1)], next: number(2), total: 3))
        await settle()
        pager.loadNext()
        await settle()
        pages.fail(1)
        await settle()
        XCTAssertTrue(pager.nextError)
        pager.retry()
        await settle()
        XCTAssertEqual(pages.calls.map(\.input), ["1", "2", "2"])
        XCTAssertEqual(loadedCounts, [0, 1, 1])
        pages.succeed(2, ListPage(items: [row(2)], next: number(3), total: 3))
        await settle()
        let refresh = Task { await pager.refresh() }
        await settle()
        pages.fail(3)
        await refresh.value
        XCTAssertEqual(pager.items.map(\.sourceID), [1, 2])
        XCTAssertTrue(pager.refreshError)
        XCTAssertEqual(pager.phase, .loaded)
    }

    func testNewListDiscardsLateResponseFromPreviousFilter() async throws {
        let loader = ControlledCollections()
        let model = HitsModel(loader: loader)
        model.start()
        await settle()
        model.select(.week)
        await settle()
        loader.pages.succeed(1, ListPage(items: [row(2)], next: nil, total: 1))
        await settle()
        loader.pages.succeed(0, ListPage(items: [row(1)], next: nil, total: 1))
        await settle()
        XCTAssertEqual(model.pager.items.map(\.sourceID), [2])
        XCTAssertEqual(loader.requests.map(\.sort), ["day", "week"])
    }

    func testFilterChangeDuringPagingRestartsFromFirstPage() async {
        let loader = ControlledCollections()
        let model = FavouritesModel(loader: loader, isLoggedIn: true)
        model.start()
        await settle()
        loader.pages.succeed(0, ListPage(items: [row(1, favourite: true)],
                                         next: FeedRequest(kind: "pageCursor", value: "c2"), total: nil))
        await settle()
        model.pager.loadNext()
        await settle()
        model.select(kind: .entry)
        await settle()
        loader.pages.succeed(1, ListPage(items: [row(9, favourite: true)], next: nil, total: nil))
        loader.pages.succeed(2, ListPage(items: [row(5, favourite: true)], next: nil, total: nil))
        await settle()
        XCTAssertEqual(model.pager.items.map(\.sourceID), [5])
        XCTAssertFalse(model.pager.nextLoading)
        XCTAssertEqual(loader.favouriteQueries.map(\.type), ["all", "all", "entry"])
    }

    // MARK: Favorites consistency (P30)

    func testUnfavouritingOrDeletingRemovesFavouriteRows() async {
        let loader = ControlledCollections()
        let updates = ResourceUpdates()
        let model = FavouritesModel(loader: loader, isLoggedIn: true, updates: updates)
        model.start()
        await settle()
        loader.pages.succeed(0, ListPage(items: [row(1, favourite: true), row(2, favourite: true),
                                                 row(3, favourite: true)], next: nil, total: 3))
        await settle()
        updates.publish(.replacement(row(1, favourite: false)), for: ResourceIdentity(row(1)))
        updates.publish(.deleted, for: ResourceIdentity(row(2)))
        model.reconcile(updates)
        XCTAssertEqual(model.pager.items.map(\.sourceID), [3])
        XCTAssertEqual(model.pager.total, 1)
    }

    // MARK: Advanced search (P26)

    func testAdvancedSearchValidationKeepsResultsUntouched() {
        let service = RecordingAdvancedSearch()
        let model = AdvancedSearchModel(initialQuery: "  ", service: service)
        XCTAssertFalse(model.canSearch)
        model.search()
        XCTAssertEqual(model.validation, .queryRequired)
        XCTAssertFalse(model.hasSearched)
        model.form.query = "kotlin"
        XCTAssertNil(model.validation, "editing clears the validation message")
        model.form.datePreset = .custom
        model.form.customDateFrom = "bad"
        model.search()
        XCTAssertEqual(model.validation, .invalidCustomDateFormat)
        XCTAssertTrue(service.pages.calls.isEmpty)
    }

    func testAdvancedSearchBuildsOnceAndPagesWithTheSameRequest() async {
        let service = RecordingAdvancedSearch()
        let model = AdvancedSearchModel(initialQuery: "kotlin", service: service)
        model.search()
        await settle()
        service.pages.succeed(0, ListPage(items: [row(1)], next: number(2), total: 2))
        await settle()
        model.form.query = "changed but not submitted"
        model.results.loadNext()
        await settle()
        XCTAssertEqual(service.built.count, 1, "paging must not rebuild the relative date window")
        XCTAssertEqual(service.pages.calls.map(\.input), ["1", "2"])
    }

    // MARK: Hits archive (P27)

    func testArchiveAvailabilityMatchesAndroidBounds() {
        let now = DateComponents(calendar: .current, year: 2026, month: 9, day: 25).date!
        XCTAssertFalse(HitsArchive.isAvailable(year: 2007, month: 11, now: now))
        XCTAssertTrue(HitsArchive.isAvailable(year: 2007, month: 12, now: now))
        XCTAssertTrue(HitsArchive.isAvailable(year: 2026, month: 9, now: now))
        XCTAssertFalse(HitsArchive.isAvailable(year: 2026, month: 10, now: now))
        XCTAssertFalse(HitsArchive.isAvailable(year: 2006, month: 5, now: now))
    }

    func testArchiveSelectionForcesAllSortAndSortClearsArchive() async {
        let loader = ControlledCollections()
        let model = HitsModel(loader: loader)
        model.select(archive: HitsArchive(year: 2020, month: 5))
        XCTAssertEqual(model.sort, .all)
        await settle()
        model.select(.day)
        await settle()
        XCTAssertNil(model.archive)
        XCTAssertEqual(loader.requests.map(\.archive), [HitsArchive(year: 2020, month: 5), nil])
    }

    // MARK: Tag (P29)

    func testTagTogglesApplyOnlyAfterConfirmationAndUnobserveClearsNotifications() async {
        let loader = ControlledTag()
        let model = TagModel(tag: "nauka", isLoggedIn: true, loader: loader)
        model.start()
        await settle()
        loader.detailCalls.succeed(0, TagDetails(name: "nauka", description: "", followers: 1,
                                                       bannerURL: nil, observed: true,
                                                       notificationsEnabled: true, blacklisted: false))
        await settle()
        model.toggle(.observe)
        model.toggle(.observe)
        await settle()
        XCTAssertEqual(loader.mutations.calls.map(\.input), ["observe:false"], "duplicate taps are ignored")
        XCTAssertEqual(model.details?.observed, true, "no optimistic change")
        loader.mutations.succeed(0, ())
        await settle()
        XCTAssertEqual(model.details?.observed, false)
        XCTAssertEqual(model.details?.notificationsEnabled, false)
        model.toggle(.notifications)
        await settle()
        XCTAssertEqual(loader.mutations.calls.count, 1, "notifications need an observed tag")
    }

    func testTagActionFailureKeepsStateAndGuestsCannotAct() async {
        let loader = ControlledTag()
        let model = TagModel(tag: "nauka", isLoggedIn: true, loader: loader)
        model.start()
        await settle()
        loader.detailCalls.succeed(0, TagDetails(name: "nauka", description: "", followers: 1,
                                                       bannerURL: nil, observed: false,
                                                       notificationsEnabled: false, blacklisted: false))
        await settle()
        model.toggle(.blacklist)
        await settle()
        loader.mutations.fail(0)
        await settle()
        XCTAssertEqual(model.details?.blacklisted, false)
        XCTAssertTrue(model.actionFailed)
        XCTAssertTrue(model.pending.isEmpty)
        model.setSession(false)
        model.toggle(.observe)
        await settle()
        XCTAssertEqual(loader.mutations.calls.count, 1)
        XCTAssertNil(model.details, "account-specific tag state is reloaded after a session change")
        XCTAssertEqual(loader.firstRequests, [true, false])
    }

    func testTagGalleryShowsOnlyEntriesWithPhotos() async {
        let loader = ControlledTag()
        let model = TagModel(tag: "nauka", isLoggedIn: false, loader: loader)
        model.start()
        await settle()
        let photo = Photo(url: "https://example.com/a.jpg", width: 1, height: 1, mimeType: "image/jpeg", key: "k")
        loader.pages.succeed(0, ListPage(items: [
            Resource(sourceID: 1, kind: .entry, body: "", photo: photo),
            Resource(sourceID: 2, kind: .entry, body: ""),
            Resource(sourceID: 3, kind: .link, body: "", photo: photo),
        ], next: nil, total: nil))
        await settle()
        XCTAssertEqual(model.galleryItems.map(\.sourceID), [1])
    }
}

@MainActor
private final class ControlledTag: TagLoading {
    let detailCalls = Pending<TagDetails>()
    let pages = Pending<ListPage<Resource>>()
    let mutations = Pending<Void>()
    private(set) var firstRequests: [Bool] = []
    func firstRequest(isLoggedIn: Bool) -> FeedRequest {
        firstRequests.append(isLoggedIn)
        return FeedRequest(kind: "initial")
    }
    func details(_ tag: String) async throws -> TagDetails { try await detailCalls.wait(tag) }
    func stream(_ tag: String, sort: String, type: String, request: FeedRequest, loaded: Int) async throws
        -> ListPage<Resource> { try await pages.wait(type) }
    func setObserved(_ tag: String, _ enabled: Bool) async throws { try await mutations.wait("observe:\(enabled)") }
    func setNotifications(_ tag: String, _ enabled: Bool) async throws { try await mutations.wait("notify:\(enabled)") }
    func setBlacklisted(_ tag: String, _ enabled: Bool) async throws { try await mutations.wait("block:\(enabled)") }
}


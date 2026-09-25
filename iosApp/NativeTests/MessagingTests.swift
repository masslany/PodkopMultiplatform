import XCTest
@testable import PodkopNative

@MainActor
private final class ManualClock: PollClock {
    private(set) var waiters: [CheckedContinuation<Void, Error>] = []
    func sleep(seconds: Double) async throws {
        XCTAssertEqual(seconds, 30)
        try await withCheckedThrowingContinuation { waiters.append($0) }
    }
    func tick() {
        let pending = waiters
        waiters = []
        pending.forEach { $0.resume() }
    }
}

@MainActor
private final class ControlledNotifications: NotificationsLoading {
    let pages = Pending<ListPage<NativeNotification>>()
    let marks = Pending<Void>()
    private(set) var statusRefreshes = 0
    func firstRequest(_ group: NotificationGroupKind) -> FeedRequest {
        group == .pm ? FeedRequest(kind: "number", value: "1") : FeedRequest(kind: "initial")
    }
    func load(_ group: NotificationGroupKind, request: FeedRequest, loaded: Int) async throws
        -> ListPage<NativeNotification> { try await pages.wait(group.rawValue) }
    func refreshStatus() async throws { statusRefreshes += 1 }
    func markAsRead(_ group: NotificationGroupKind, id: String) async throws { try await marks.wait("\(group.rawValue):\(id)") }
    func markAllAsRead(_ group: NotificationGroupKind) async throws { try await marks.wait("all:\(group.rawValue)") }
}

@MainActor
private final class ControlledMessages: MessagesLoading {
    let threads = Pending<ListPage<NativeMessage>>()
    let newerCalls = Pending<[NativeMessage]>()
    let sends = Pending<NativeMessage>()
    private(set) var events: [String] = []
    func normalize(_ username: String) -> String {
        let trimmed = username.trimmingCharacters(in: .whitespaces)
        return trimmed.hasPrefix("@") ? String(trimmed.dropFirst()) : trimmed
    }
    func firstRequest() -> FeedRequest { FeedRequest(kind: "number", value: "1") }
    func conversations(request: FeedRequest, loaded: Int) async throws -> ListPage<NativeConversation> {
        ListPage(items: [], next: nil, total: 0)
    }
    func thread(_ username: String, request: FeedRequest, loaded: Int) async throws -> ListPage<NativeMessage> {
        try await threads.wait(request.value ?? "")
    }
    func newer(_ username: String) async throws -> [NativeMessage] { try await newerCalls.wait(username) }
    func send(_ username: String, text: String, adult: Bool, photoKey: String?) async throws -> NativeMessage {
        try await sends.wait(text)
    }
    func readAll() async throws { events.append("readAll") }
    func refreshStatus() async throws { events.append("status") }
}

@MainActor
final class MessagingTests: XCTestCase {
    private func settle() async { for _ in 0..<20 { await Task.yield() } }

    private func notification(_ id: String, group: NotificationGroupKind = .tags, read: Bool = false,
                              groupID: String? = nil, count: Int = 1, entry: Int? = nil, link: Int? = nil,
                              tag: String? = "nauka") -> NativeNotification {
        NativeNotification(id: id, group: group, isRead: read, groupID: groupID, groupCount: count,
                           createdAt: Date(timeIntervalSince1970: 0), actor: "ewa", actorColor: nil, actorGender: nil,
                           message: "m-\(id)", issueTitle: nil, badgeName: nil, tagName: tag, linkID: link,
                           linkTitle: link.map { "L\($0)" }, entryID: entry, entryContent: entry.map { "E\($0)" },
                           target: .none)
    }

    private func message(_ key: String, _ seconds: Double, incoming: Bool = true) -> NativeMessage {
        NativeMessage(key: key, content: key, createdAt: Date(timeIntervalSince1970: seconds), incoming: incoming,
                      adult: false, sender: nil, senderColor: nil, photo: nil, embedURL: nil)
    }

    // MARK: Notifications (P32)

    func testTagNotificationsGroupLikeAndroid() {
        let rows = NotificationRow.rows(for: [
            notification("a", read: true, groupID: "g", count: 2, entry: 1),
            notification("b", groupID: "g", count: 2, entry: 2),
            notification("c", groupID: "g", count: 1, link: 3),
            notification("d", groupID: "h", count: 2, link: 4, tag: nil),
        ], group: .tags)
        XCTAssertEqual(rows.map(\.id), ["group:g", "c", "d"])
        XCTAssertEqual(rows[0].notificationIDs, ["a", "b"])
        XCTAssertFalse(rows[0].isRead, "a group is read only when every member is")
        XCTAssertEqual(rows[0].grouped, .entries)
        XCTAssertEqual(rows[0].target, .tag("nauka"))
        XCTAssertNil(rows[2].grouped, "without a tag name the group falls back to a single row")
        XCTAssertEqual(rows[2].headline, "m-d")
    }

    func testObservedDiscussionsGroupAndPrivateMessagesHaveNoReadIds() {
        let observed = NotificationRow.rows(for: [
            notification("a", group: .observedDiscussions, groupID: "g", count: 3, link: 9),
            notification("b", group: .observedDiscussions, groupID: "g", count: 3, link: 9),
        ], group: .observedDiscussions)
        XCTAssertEqual(observed.count, 1)
        XCTAssertEqual(observed[0].observed, .link)
        XCTAssertEqual(observed[0].observedTitle, "L9")
        let pm = NotificationRow.rows(for: [notification("ewa", group: .pm)], group: .pm)
        XCTAssertEqual(pm[0].notificationIDs, [])
    }

    func testOpeningMarksOnlySingleUnreadNotifications() async {
        let loader = ControlledNotifications()
        let model = NotificationsModel(loader: loader)
        model.start()
        await settle()
        XCTAssertEqual(loader.statusRefreshes, 1)
        loader.pages.succeed(0, ListPage(items: [
            notification("a", group: .entries), notification("b", group: .entries, read: true),
        ], next: nil, total: 2))
        await settle()
        let rows = model.rows
        model.opened(rows[1])
        await settle()
        XCTAssertTrue(loader.marks.calls.isEmpty)
        model.opened(rows[0])
        await settle()
        XCTAssertEqual(loader.marks.calls.map(\.input), ["entries:a"])
        loader.marks.succeed(0, ())
        await settle()
        XCTAssertTrue(model.rows[0].isRead)
    }

    func testMarkAllIsUnavailableForMessagesAndWithoutUnread() async {
        let loader = ControlledNotifications()
        let model = NotificationsModel(loader: loader)
        model.start()
        await settle()
        loader.pages.succeed(0, ListPage(items: [notification("a", group: .entries)], next: nil, total: 1))
        await settle()
        XCTAssertFalse(model.canMarkAll(NotificationCounts(entries: 0)))
        XCTAssertTrue(model.canMarkAll(NotificationCounts(entries: 2)))
        model.markAll(NotificationCounts(entries: 2))
        model.markAll(NotificationCounts(entries: 2))
        await settle()
        XCTAssertEqual(loader.marks.calls.map(\.input), ["all:entries"])
        loader.marks.succeed(0, ())
        await settle()
        XCTAssertTrue(model.rows.allSatisfy(\.isRead))
        model.select(.pm)
        XCTAssertFalse(model.canMarkAll(NotificationCounts(pm: 5)))
    }

    func testSwitchingGroupDiscardsLateResponse() async {
        let loader = ControlledNotifications()
        let model = NotificationsModel(loader: loader)
        model.start()
        await settle()
        model.select(.tags)
        await settle()
        loader.pages.succeed(1, ListPage(items: [notification("t")], next: nil, total: 1))
        loader.pages.succeed(0, ListPage(items: [notification("late", group: .entries)], next: nil, total: 1))
        await settle()
        XCTAssertEqual(model.pager.items.map(\.id), ["t"])
    }

    // MARK: Conversation (P34, P35)

    func testMergeDeduplicatesByKeyAndOrdersByTime() {
        let merged = NativeMessage.merge([message("b", 2), message("a", 1)], [message("b", 2, incoming: false),
                                                                             message("c", 1)])
        XCTAssertEqual(merged.map(\.key), ["a", "c", "b"])
        XCTAssertFalse(merged[2].incoming, "the newer copy of a key wins")
    }

    func testVisibleConversationMarksReadLoadsAndPollsOnlyWhileVisible() async {
        let loader = ControlledMessages()
        let clock = ManualClock()
        let model = ConversationModel(username: "ewa", loader: loader, media: nil, clock: clock)
        model.becameVisible()
        await settle()
        XCTAssertEqual(loader.events, ["readAll", "status"])
        loader.threads.succeed(0, ListPage(items: [message("a", 1)], next: nil, total: 1))
        await settle()
        XCTAssertEqual(model.scrollToLatest, 1)
        XCTAssertEqual(clock.waiters.count, 1, "one poller after loading")
        clock.tick()
        await settle()
        XCTAssertEqual(loader.newerCalls.calls.count, 1)
        loader.newerCalls.succeed(0, [message("b", 2)])
        await settle()
        XCTAssertEqual(model.messages.map(\.key), ["a", "b"])
        XCTAssertEqual(model.scrollToLatest, 1, "polled messages never move the reader")

        model.becameHidden()
        XCTAssertFalse(model.isPolling)
        clock.tick()
        await settle()
        XCTAssertEqual(loader.newerCalls.calls.count, 1, "a stopped poller does not fetch")
        model.becameVisible()
        model.becameVisible()
        await settle()
        XCTAssertEqual(loader.newerCalls.calls.count, 2, "returning catches up once")
        XCTAssertEqual(clock.waiters.count, 1, "reopening does not start a second poller")
    }

    func testOlderPagesPrependKeepAnchorAndStopOnOverlap() async {
        let loader = ControlledMessages()
        let model = ConversationModel(username: "ewa", loader: loader, media: nil, clock: ManualClock())
        model.becameVisible()
        await settle()
        loader.threads.succeed(0, ListPage(items: [message("c", 3), message("d", 4)],
                                           next: FeedRequest(kind: "number", value: "2"), total: nil))
        await settle()
        XCTAssertTrue(model.hasOlder)
        model.loadOlder()
        model.loadOlder()
        await settle()
        XCTAssertEqual(loader.threads.calls.map(\.input), ["1", "2"])
        loader.threads.succeed(1, ListPage(items: [message("a", 1), message("c", 3)],
                                           next: FeedRequest(kind: "number", value: "3"), total: nil))
        await settle()
        XCTAssertEqual(model.messages.map(\.key), ["a", "c", "d"])
        XCTAssertEqual(model.anchorAfterPrepend, "c")
        model.loadOlder()
        await settle()
        loader.threads.succeed(2, ListPage(items: [message("a", 1)], next: FeedRequest(kind: "number", value: "4"),
                                           total: nil))
        await settle()
        XCTAssertFalse(model.hasOlder, "a page with nothing new ends paging")
    }

    func testSendGuardsDuplicatesKeepsDraftOnFailureAndBlocksUnclearOutcome() async {
        let loader = ControlledMessages()
        let model = ConversationModel(username: "ewa", loader: loader, media: nil, clock: ManualClock())
        model.becameVisible()
        await settle()
        loader.threads.succeed(0, ListPage(items: [], next: nil, total: 0))
        await settle()
        XCTAssertFalse(model.canSend)
        model.text = "Cześć 👋"
        model.send()
        model.send()
        await settle()
        XCTAssertEqual(loader.sends.calls.count, 1)
        loader.sends.calls[0].finish(.failure(BridgeFailure(category: "unknown", code: nil)))
        await settle()
        XCTAssertEqual(model.text, "Cześć 👋")
        XCTAssertTrue(model.sendFailed)
        XCTAssertTrue(model.outcomeUnknown, "a transport failure may have been delivered")
        XCTAssertFalse(model.canSend)
        model.acknowledgeUnknownOutcome()
        model.send()
        await settle()
        loader.sends.succeed(1, message("sent", 10, incoming: false))
        await settle()
        XCTAssertEqual(model.text, "")
        XCTAssertEqual(model.messages.map(\.key), ["sent"])
        XCTAssertEqual(model.scrollToLatest, 1)
    }

    func testRejectedSendCanBeRetriedImmediately() async {
        let loader = ControlledMessages()
        let model = ConversationModel(username: "ewa", loader: loader, media: nil, clock: ManualClock())
        model.text = "hej"
        model.send()
        await settle()
        loader.sends.calls[0].finish(.failure(BridgeFailure(category: "validation", code: "400")))
        await settle()
        XCTAssertFalse(model.outcomeUnknown)
        XCTAssertTrue(model.canSend)
    }

    // MARK: New conversation (P33)

    func testNewConversationSuggestionsNeedThreeCharactersAndIgnoreStaleResults() async throws {
        let suggester = ControlledSuggestions()
        let model = NewConversationModel(suggester: suggester, loader: ControlledMessages(),
                                         debounce: .milliseconds(1))
        model.username = "@ew"
        try await Task.sleep(for: .milliseconds(20))
        XCTAssertTrue(suggester.userCalls.calls.isEmpty)
        model.username = "@ewa"
        try await Task.sleep(for: .milliseconds(20))
        model.username = "ewa-z"
        try await Task.sleep(for: .milliseconds(20))
        XCTAssertEqual(suggester.userCalls.calls.map(\.input), ["ewa", "ewa-z"])
        suggester.userCalls.succeed(1, [NativeUserSuggestion(username: "ewa-z", avatarURL: "", color: "", gender: "")])
        suggester.userCalls.succeed(0, [NativeUserSuggestion(username: "stale", avatarURL: "", color: "", gender: "")])
        await settle()
        XCTAssertEqual(model.suggestions.map(\.username), ["ewa-z"])
    }

    func testUnknownUsernameShowsRetryableFailure() async {
        let loader = ControlledMessages()
        let model = ConversationModel(username: "nieistnieje", loader: loader, media: nil, clock: ManualClock())
        model.becameVisible()
        await settle()
        loader.threads.calls[0].finish(.failure(BridgeFailure(category: "notFound", code: "404")))
        await settle()
        XCTAssertEqual(model.phase, .failed)
        XCTAssertFalse(model.isPolling, "no polling for a thread that never loaded")
        model.retry()
        await settle()
        XCTAssertEqual(loader.threads.calls.count, 2)
    }
}


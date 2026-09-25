import Foundation
import Observation
import PodkopShared

enum NotificationGroupKind: String, CaseIterable {
    case entries, pm, tags, observedDiscussions
}

enum NotificationTargetValue: Equatable {
    case link(Int), entry(Int), conversation(String), profile(String), tag(String), external(URL), none

    init(kind: String, value: String?) {
        switch kind {
        case "link": self = value.flatMap(Int.init).map(NotificationTargetValue.link) ?? .none
        case "entry": self = value.flatMap(Int.init).map(NotificationTargetValue.entry) ?? .none
        case "conversation": self = value.map(NotificationTargetValue.conversation) ?? .none
        case "profile": self = value.map(NotificationTargetValue.profile) ?? .none
        case "tag": self = value.map(NotificationTargetValue.tag) ?? .none
        case "external":
            if let value, let url = URL(string: value), ["http", "https"].contains(url.scheme?.lowercased() ?? "") {
                self = .external(url)
            } else {
                self = .none
            }
        default: self = .none
        }
    }
}

struct NativeNotification: Identifiable, Equatable {
    let id: String
    let group: NotificationGroupKind
    var isRead: Bool
    let groupID: String?
    let groupCount: Int
    let createdAt: Date
    let actor: String?
    var actorAvatarURL: String? = nil
    let actorColor: String?
    let actorGender: String?
    let message: String?
    let issueTitle: String?
    let badgeName: String?
    let tagName: String?
    let linkID: Int?
    let linkTitle: String?
    let entryID: Int?
    let entryContent: String?
    let target: NotificationTargetValue
}

/// A displayed row; Android groups tag and observed-discussion notifications that share a group id.
struct NotificationRow: Identifiable, Equatable {
    enum Grouped: Equatable { case entries, links, generic }
    enum Observed: Equatable { case entry, link }

    let id: String
    let isRead: Bool
    let actor: String?
    var actorAvatarURL: String? = nil
    let actorColor: String?
    let actorGender: String?
    let createdAt: Date
    /// Ids to mark read; private-message rows have none, as on Android.
    let notificationIDs: [String]
    let headline: String?
    let groupCount: Int
    let tagName: String?
    let grouped: Grouped?
    let observed: Observed?
    let observedTitle: String?
    let target: NotificationTargetValue

    static func rows(for items: [NativeNotification], group: NotificationGroupKind) -> [NotificationRow] {
        switch group {
        case .tags, .observedDiscussions:
            var order: [String] = []
            var buckets: [String: [NativeNotification]] = [:]
            for item in items {
                let key = item.groupCount > 1 ? (item.groupID ?? item.id) : item.id
                if buckets[key] == nil { order.append(key) }
                buckets[key, default: []].append(item)
            }
            return order.compactMap { key in
                guard let members = buckets[key], let first = members.first else { return nil }
                let groupable = first.groupCount > 1 && !(first.groupID ?? "").isEmpty
                if group == .tags, groupable, !(first.tagName ?? "").isEmpty {
                    return groupedTag(first, members)
                }
                if group == .observedDiscussions, groupable, observedKind(first) != nil {
                    return groupedObserved(first, members)
                }
                return single(first)
            }
        default:
            return items.map(single)
        }
    }

    private static func observedKind(_ item: NativeNotification) -> Observed? {
        if item.linkID != nil { return .link }
        if item.entryID != nil { return .entry }
        return nil
    }

    private static func observedTitle(_ item: NativeNotification) -> String? {
        switch observedKind(item) {
        case .link: item.linkTitle
        case .entry: item.entryContent
        case nil: nil
        }
    }

    private static func single(_ item: NativeNotification) -> NotificationRow {
        NotificationRow(
            id: item.id, isRead: item.isRead, actor: item.actor, actorAvatarURL: item.actorAvatarURL,
            actorColor: item.actorColor,
            actorGender: item.actorGender, createdAt: item.createdAt,
            notificationIDs: item.group == .pm ? [] : [item.id],
            headline: [item.message, item.issueTitle, item.badgeName, item.linkTitle, item.entryContent]
                .compactMap { $0 }.first,
            groupCount: item.groupCount, tagName: item.tagName, grouped: nil,
            observed: observedKind(item), observedTitle: observedTitle(item), target: item.target
        )
    }

    private static func groupedTag(_ first: NativeNotification, _ members: [NativeNotification]) -> NotificationRow {
        let grouped: Grouped
        if members.allSatisfy({ $0.linkID != nil && $0.entryID == nil }) { grouped = .links }
        else if members.allSatisfy({ $0.entryID != nil && $0.linkID == nil }) { grouped = .entries }
        else { grouped = .generic }
        return NotificationRow(
            id: first.groupID.map { "group:\($0)" } ?? first.id, isRead: members.allSatisfy(\.isRead),
            actor: nil, actorColor: nil, actorGender: nil, createdAt: first.createdAt,
            notificationIDs: members.map(\.id), headline: nil, groupCount: first.groupCount,
            tagName: first.tagName, grouped: grouped, observed: nil, observedTitle: nil,
            target: .tag(first.tagName ?? "")
        )
    }

    private static func groupedObserved(_ first: NativeNotification, _ members: [NativeNotification]) -> NotificationRow {
        NotificationRow(
            id: first.groupID.map { "group:\($0)" } ?? first.id, isRead: members.allSatisfy(\.isRead),
            actor: first.actor, actorAvatarURL: first.actorAvatarURL, actorColor: first.actorColor,
            actorGender: first.actorGender,
            createdAt: first.createdAt, notificationIDs: members.map(\.id), headline: nil,
            groupCount: first.groupCount, tagName: nil, grouped: nil, observed: observedKind(first),
            observedTitle: observedTitle(first), target: first.target
        )
    }
}

struct NotificationCounts: Equatable {
    var entries = 0
    var pm = 0
    var tags = 0
    var observedDiscussions = 0
    var total: Int { entries + pm + tags + observedDiscussions }

    subscript(group: NotificationGroupKind) -> Int {
        switch group {
        case .entries: entries
        case .pm: pm
        case .tags: tags
        case .observedDiscussions: observedDiscussions
        }
    }
}

@MainActor protocol NotificationsLoading {
    func firstRequest(_ group: NotificationGroupKind) -> FeedRequest
    func load(_ group: NotificationGroupKind, request: FeedRequest, loaded: Int) async throws
        -> ListPage<NativeNotification>
    func refreshStatus() async throws
    func markAsRead(_ group: NotificationGroupKind, id: String) async throws
    func markAllAsRead(_ group: NotificationGroupKind) async throws
}

@MainActor
final class SharedNotificationsLoader: NotificationsLoading {
    private let client: PodkopClient
    private let adapter: BridgeAdapter

    init(client: PodkopClient, adapter: BridgeAdapter) {
        self.client = client
        self.adapter = adapter
    }

    func firstRequest(_ group: NotificationGroupKind) -> FeedRequest {
        FeedRequest(client.notifications.firstRequest(group: group.rawValue))
    }

    func load(_ group: NotificationGroupKind, request: FeedRequest, loaded: Int) async throws
        -> ListPage<NativeNotification> {
        let page: IOSNotificationPage = try await adapter.call {
            self.client.notifications.load(group: group.rawValue,
                                           request: IOSPageRequest(kind: request.kind, value: request.value),
                                           loaded: Int32(loaded), completion: $0)
        }
        return ListPage(items: page.items.map { value in
            NativeNotification(
                id: value.id, group: NotificationGroupKind(rawValue: value.group) ?? group, isRead: value.isRead,
                groupID: value.groupId, groupCount: Int(value.groupCount),
                createdAt: Date(timeIntervalSince1970: Double(value.createdAtEpochMillis) / 1000),
                actor: value.actorUsername, actorAvatarURL: value.actorAvatarUrl, actorColor: value.actorColor,
                actorGender: value.actorGender,
                message: value.message, issueTitle: value.issueTitle, badgeName: value.badgeName,
                tagName: value.tagName, linkID: value.linkId?.intValue, linkTitle: value.linkTitle,
                entryID: value.entryId?.intValue, entryContent: value.entryContent,
                target: NotificationTargetValue(kind: value.targetKind, value: value.targetValue)
            )
        }, next: page.next.map(FeedRequest.init), total: page.total?.intValue)
    }

    func refreshStatus() async throws {
        let _: IOSNotificationStatus = try await adapter.call { self.client.notifications.refreshStatus(completion: $0) }
    }

    func markAsRead(_ group: NotificationGroupKind, id: String) async throws {
        let _: IOSSuccess = try await adapter.call {
            self.client.notifications.markAsRead(group: group.rawValue, id: id, completion: $0)
        }
    }

    func markAllAsRead(_ group: NotificationGroupKind) async throws {
        let _: IOSSuccess = try await adapter.call {
            self.client.notifications.markAllAsRead(group: group.rawValue, completion: $0)
        }
    }
}

@MainActor @Observable
final class NotificationsModel {
    private(set) var group: NotificationGroupKind = .entries
    private(set) var markingAll = false
    private(set) var markAllFailed = false
    let pager = ListPager<NativeNotification>(key: \.id)
    private let loader: NotificationsLoading

    init(loader: NotificationsLoading) { self.loader = loader }

    var rows: [NotificationRow] { NotificationRow.rows(for: pager.items, group: group) }

    /// Android offers mark-all only outside private messages and only with unread items.
    func canMarkAll(_ counts: NotificationCounts) -> Bool {
        group != .pm && counts[group] > 0 && !markingAll
    }

    func start() {
        guard pager.phase == .idle else { return }
        reload(refreshingStatus: true)
    }

    func select(_ group: NotificationGroupKind) {
        guard group != self.group || pager.phase == .failed else { return }
        self.group = group
        markAllFailed = false
        reload(refreshingStatus: false)
    }

    func refresh() async {
        try? await loader.refreshStatus()
        await pager.refresh()
    }

    func stop() { pager.stop() }

    /// Opens first; a single unread notification is then marked read in the background.
    func opened(_ row: NotificationRow) {
        guard !row.isRead, row.notificationIDs.count == 1, let id = row.notificationIDs.first else { return }
        let group = group, loader = loader
        Task { [weak self] in
            guard (try? await loader.markAsRead(group, id: id)) != nil, let self, self.group == group else { return }
            self.pager.update { item in
                guard item.id == id else { return item }
                var read = item
                read.isRead = true
                return read
            }
        }
    }

    func markAll(_ counts: NotificationCounts) {
        guard canMarkAll(counts) else { return }
        markingAll = true
        markAllFailed = false
        let group = group, loader = loader
        Task { [weak self] in
            do {
                try await loader.markAllAsRead(group)
                guard let self else { return }
                if self.group == group {
                    self.pager.update { item in
                        var read = item
                        read.isRead = true
                        return read
                    }
                }
                self.markingAll = false
            } catch {
                guard let self else { return }
                self.markingAll = false
                self.markAllFailed = !(error is CancellationError)
            }
        }
    }

    func dismissMarkAllFailure() { markAllFailed = false }

    private func reload(refreshingStatus: Bool) {
        let group = group, loader = loader
        if refreshingStatus { Task { try? await loader.refreshStatus() } }
        pager.load(first: loader.firstRequest(group)) { try await loader.load(group, request: $0, loaded: $1) }
    }
}

#if DEBUG
@MainActor
final class FixtureNotificationsLoader: NotificationsLoading {
    private var read = Set<String>()
    func firstRequest(_ group: NotificationGroupKind) -> FeedRequest { FeedRequest(kind: "initial") }
    func load(_ group: NotificationGroupKind, request: FeedRequest, loaded: Int) async throws
        -> ListPage<NativeNotification> {
        let now = Date()
        func item(_ id: String, _ target: NotificationTargetValue, message: String? = nil, tag: String? = nil,
                  groupID: String? = nil, count: Int = 1, entry: Int? = nil, link: Int? = nil,
                  actor: String? = "Ewa-Żółw") -> NativeNotification {
            NativeNotification(id: id, group: group, isRead: read.contains(id), groupID: groupID, groupCount: count,
                               createdAt: now, actor: actor, actorColor: "green", actorGender: "female",
                               message: message, issueTitle: nil, badgeName: nil, tagName: tag, linkID: link,
                               linkTitle: link.map { _ in "Przykładowy link" }, entryID: entry,
                               entryContent: entry.map { _ in "Treść wpisu" }, target: target)
        }
        let items: [NativeNotification] = switch group {
        case .entries: [item("e1", .entry(102), message: "wspomniał Cię we wpisie", entry: 102)]
        case .pm: [item("Ewa-Żółw", .conversation("Ewa-Żółw"), message: "Cześć!")]
        case .tags: [item("t1", .tag("technologia"), tag: "technologia", groupID: "g1", count: 2, entry: 1, actor: nil),
                     item("t2", .tag("technologia"), tag: "technologia", groupID: "g1", count: 2, entry: 2, actor: nil)]
        case .observedDiscussions: [item("o1", .link(101), link: 101)]
        }
        return ListPage(items: items, next: nil, total: items.count)
    }
    func refreshStatus() async throws {}
    func markAsRead(_ group: NotificationGroupKind, id: String) async throws { read.insert(id) }
    func markAllAsRead(_ group: NotificationGroupKind) async throws {}
}
#endif

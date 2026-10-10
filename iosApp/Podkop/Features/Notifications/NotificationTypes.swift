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

struct AppNotification: Identifiable, Equatable {
    let id: String
    let group: NotificationGroupKind
    var isRead: Bool
    let groupID: String?
    let groupCount: Int
    /// Set on a row the API grouped (`show_grouped=1`), whose own notifications `loadGroup` lists.
    var showAsGroup = false
    /// When the group last got a notification; groups show it rather than their first one's time.
    var groupUpdatedAt: Date? = nil
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

    func markedRead() -> AppNotification {
        var read = self
        read.isRead = true
        return read
    }
}

/// A displayed row; like Android, the API's groups of tag and observed-discussion notifications
/// become one row each, which can be expanded into the group's own notifications.
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
    /// Set when the row stands for a group of notifications.
    var groupID: String? = nil
    /// Set on a single notification from an observed tag: the entry or link that used the tag.
    var tagged: Observed? = nil

    static func rows(for items: [AppNotification], group: NotificationGroupKind) -> [NotificationRow] {
        switch group {
        case .tags, .observedDiscussions:
            var order: [String] = []
            var buckets: [String: [AppNotification]] = [:]
            for item in items {
                let key = (item.showAsGroup || item.groupCount > 1) ? (item.groupID ?? item.id) : item.id
                if buckets[key] == nil { order.append(key) }
                buckets[key, default: []].append(item)
            }
            return order.compactMap { key in
                guard let members = buckets[key], let first = members.first else { return nil }
                let groupable = (first.showAsGroup || first.groupCount > 1) && !(first.groupID ?? "").isEmpty
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

    /// The notifications inside an expanded group, each shown on its own.
    static func memberRows(_ items: [AppNotification]) -> [NotificationRow] {
        items.map(single)
    }

    private static func observedKind(_ item: AppNotification) -> Observed? {
        if item.linkID != nil { return .link }
        if item.entryID != nil { return .entry }
        return nil
    }

    private static func observedTitle(_ item: AppNotification) -> String? {
        switch observedKind(item) {
        case .link: item.linkTitle
        case .entry: item.entryContent
        case nil: nil
        }
    }

    private static func single(_ item: AppNotification) -> NotificationRow {
        NotificationRow(
            id: item.id, isRead: item.isRead, actor: item.actor, actorAvatarURL: item.actorAvatarURL,
            actorColor: item.actorColor,
            actorGender: item.actorGender, createdAt: item.createdAt,
            notificationIDs: item.group == .pm ? [] : [item.id],
            headline: [item.message, item.issueTitle, item.badgeName, item.linkTitle, item.entryContent]
                .compactMap { $0 }.first,
            groupCount: item.groupCount, tagName: item.tagName, grouped: nil,
            observed: item.group == .tags ? nil : observedKind(item), observedTitle: observedTitle(item),
            target: item.target, tagged: item.group == .tags ? observedKind(item) : nil
        )
    }

    private static func groupedTag(_ first: AppNotification, _ members: [AppNotification]) -> NotificationRow {
        let grouped: Grouped
        if members.allSatisfy({ $0.linkID != nil && $0.entryID == nil }) { grouped = .links }
        else if members.allSatisfy({ $0.entryID != nil && $0.linkID == nil }) { grouped = .entries }
        else { grouped = .generic }
        return NotificationRow(
            id: first.groupID.map { "group:\($0)" } ?? first.id, isRead: members.allSatisfy(\.isRead),
            actor: nil, actorColor: nil, actorGender: nil, createdAt: first.groupUpdatedAt ?? first.createdAt,
            notificationIDs: members.map(\.id), headline: nil, groupCount: first.groupCount,
            tagName: first.tagName, grouped: grouped, observed: nil, observedTitle: nil,
            target: .tag(first.tagName ?? ""), groupID: first.groupID
        )
    }

    private static func groupedObserved(_ first: AppNotification, _ members: [AppNotification]) -> NotificationRow {
        NotificationRow(
            id: first.groupID.map { "group:\($0)" } ?? first.id, isRead: members.allSatisfy(\.isRead),
            actor: first.actor, actorAvatarURL: first.actorAvatarURL, actorColor: first.actorColor,
            actorGender: first.actorGender,
            createdAt: first.groupUpdatedAt ?? first.createdAt, notificationIDs: members.map(\.id), headline: nil,
            groupCount: first.groupCount, tagName: nil, grouped: nil, observed: observedKind(first),
            observedTitle: observedTitle(first), target: first.target, groupID: first.groupID
        )
    }
}

extension NotificationRow.Grouped {
    /// Like Android and website, a group about new entries or links opens the tag on just those.
    var tagKind: TagModel.Kind {
        switch self {
        case .entries: .entry
        case .links: .link
        case .generic: .all
        }
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

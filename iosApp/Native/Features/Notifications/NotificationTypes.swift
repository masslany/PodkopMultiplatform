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

import Foundation
import Observation

enum AppTab: String, CaseIterable, Codable, Identifiable {
    case links, upcoming, entries, more
    var id: String { rawValue }

    var title: String {
        switch self {
        case .links: String(localized: .commonLinks)
        case .entries: String(localized: .commonEntries)
        case .upcoming: String(localized: .appUpcoming)
        case .more: String(localized: .appMore)
        }
    }

    /// Tab bar icons drawn from the Android navigation vectors (home, shovel, "m", more).
    var image: String {
        switch self {
        case .links: "TabHome"
        case .upcoming: "TabUpcoming"
        case .entries: "TabEntries"
        case .more: "TabMore"
        }
    }

    var symbol: String {
        switch self {
        case .links: "link"
        case .entries: "text.bubble"
        case .upcoming: "clock"
        case .more: "line.3.horizontal"
        }
    }
}

enum AppRoute: Hashable, Codable {
    case link(Int)
    case entry(Int)
    case messages
    case conversation(String)
    case newConversation
    case notifications
    case search
    case advancedSearch(String)
    case tags
    case tag(String)
    /// A tag opened on one kind of content, as notifications about new entries or links open it.
    case tagContent(String, TagModel.Kind)
    case profile
    case user(String)
    case settings
    case blacklists
    case debug
    case favorites
    case observed
    case hits
    case addLink
    case rank
    case about

    var needsAccount: Bool {
        switch self {
        case .messages, .conversation, .newConversation, .notifications, .profile, .favorites, .observed,
             .addLink, .blacklists: true
        default: false
        }
    }
}

enum AppSheet: String, Identifiable {
    case login, composer
    var id: String { rawValue }
}

enum ComposerIntent: Hashable {
    case createEntry
    case createEntryComment(entryID: Int, replyTarget: String?)
    /// A reply nested under `parentCommentID` (threaded entry comments).
    case createEntryThreadReply(entryID: Int, parentCommentID: Int, replyTarget: String?)
    case createLinkComment(linkID: Int, parentCommentID: Int?, replyTarget: String?)
    case editEntry(Int)
    case editEntryComment(entryID: Int, commentID: Int)
    case editLinkComment(linkID: Int, commentID: Int)
}

struct AppAlert: Identifiable {
    let id = UUID()
    let title: String
    let message: String
}

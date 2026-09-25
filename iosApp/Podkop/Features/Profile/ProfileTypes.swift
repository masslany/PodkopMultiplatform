import Foundation
import Observation
import PodkopShared

struct Profile: Equatable {
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

struct Badge: Identifiable, Equatable {
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
    case resource(Resource)
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

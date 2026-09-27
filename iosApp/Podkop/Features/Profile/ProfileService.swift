import Foundation
import Observation
import PodkopShared

@MainActor protocol ProfileLoading {
    func ownUsername() async throws -> String
    func profile(_ username: String) async throws -> Profile
    func badges(_ username: String) async throws -> [Badge]
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

    func profile(_ username: String) async throws -> Profile {
        let value: IOSProfile = try await adapter.call {
            self.client.profile.load(username: username, completion: $0)
        }
        return Profile(
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

    func badges(_ username: String) async throws -> [Badge] {
        let values: [IOSProfileBadge] = try await adapter.call {
            self.client.profile.badges(username: username, completion: $0)
        }
        return values.map {
            Badge(label: $0.label, slug: $0.slug, description: $0.description_, colorHex: $0.colorHex,
                  colorHexDark: $0.colorHexDark, iconURL: $0.iconUrl,
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
        let rows: [ProfileRow] = page.resources.map { .resource(Resource($0)) }
            + page.users.map { .user(username: $0.username, color: $0.color, gender: $0.gender,
                                     online: $0.online, verified: $0.verified, avatarURL: $0.avatarUrl.nonEmpty) }
            + page.tags.map { .tag(name: $0.name, pinned: $0.pinned) }
        return ListPage(items: rows, next: page.next.map(FeedRequest.init), total: page.total?.intValue)
    }
}

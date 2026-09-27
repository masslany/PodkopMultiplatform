import Foundation
import Observation
import PodkopShared

@MainActor protocol SearchSuggesting {
    func tags(_ query: String) async throws -> [TagSuggestion]
    func users(_ query: String) async throws -> [UserSuggestion]
}

@MainActor
final class SharedSearchSuggesting: SearchSuggesting {
    private let client: PodkopClient
    private let adapter: BridgeAdapter

    init(client: PodkopClient, adapter: BridgeAdapter) {
        self.client = client
        self.adapter = adapter
    }

    func tags(_ query: String) async throws -> [TagSuggestion] {
        let values: [IOSTagSuggestion] = try await adapter.call {
            self.client.search.tags(query: query, completion: $0)
        }
        return values.map { TagSuggestion(name: $0.name, followers: Int($0.followers)) }
    }

    func users(_ query: String) async throws -> [UserSuggestion] {
        let values: [IOSUserSuggestion] = try await adapter.call {
            self.client.search.users(query: query, completion: $0)
        }
        return values.map { UserSuggestion(username: $0.username, avatarURL: $0.avatarUrl,
                                                  color: $0.color, gender: $0.gender) }
    }
}

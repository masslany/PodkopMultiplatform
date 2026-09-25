import Foundation
import Observation
import PodkopShared

@MainActor protocol SearchSuggesting {
    func tags(_ query: String) async throws -> [NativeTagSuggestion]
    func users(_ query: String) async throws -> [NativeUserSuggestion]
}

@MainActor
final class SharedSearchSuggesting: SearchSuggesting {
    private let client: PodkopClient
    private let adapter: BridgeAdapter

    init(client: PodkopClient, adapter: BridgeAdapter) {
        self.client = client
        self.adapter = adapter
    }

    func tags(_ query: String) async throws -> [NativeTagSuggestion] {
        let values: [IOSTagSuggestion] = try await adapter.call {
            self.client.search.tags(query: query, completion: $0)
        }
        return values.map { NativeTagSuggestion(name: $0.name, followers: Int($0.followers)) }
    }

    func users(_ query: String) async throws -> [NativeUserSuggestion] {
        let values: [IOSUserSuggestion] = try await adapter.call {
            self.client.search.users(query: query, completion: $0)
        }
        return values.map { NativeUserSuggestion(username: $0.username, avatarURL: $0.avatarUrl,
                                                  color: $0.color, gender: $0.gender) }
    }
}

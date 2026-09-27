import Foundation
import Observation
import PodkopShared

@MainActor protocol BlacklistsLoading {
    func firstRequest() -> FeedRequest
    func normalize(_ category: BlacklistCategory, _ value: String) -> String
    func load(_ category: BlacklistCategory, request: FeedRequest, loaded: Int) async throws -> ListPage<BlacklistEntry>
    func add(_ category: BlacklistCategory, _ value: String) async throws
    func remove(_ entry: BlacklistEntry) async throws
}

@MainActor
final class SharedBlacklistsLoader: BlacklistsLoading {
    private let client: PodkopClient
    private let adapter: BridgeAdapter

    init(client: PodkopClient, adapter: BridgeAdapter) {
        self.client = client
        self.adapter = adapter
    }

    func firstRequest() -> FeedRequest { FeedRequest(client.blacklists.firstRequest()) }

    func normalize(_ category: BlacklistCategory, _ value: String) -> String {
        client.blacklists.normalize(category: category.rawValue, value: value)
    }

    func load(_ category: BlacklistCategory, request: FeedRequest, loaded: Int) async throws
        -> ListPage<BlacklistEntry> {
        let page: IOSBlacklistPage = try await adapter.call {
            self.client.blacklists.load(category: category.rawValue,
                                        request: IOSPageRequest(kind: request.kind, value: request.value),
                                        loaded: Int32(loaded), completion: $0)
        }
        return ListPage(items: page.items.map {
            BlacklistEntry(category: category, value: $0.value, color: $0.color, gender: $0.gender,
                           avatarURL: $0.avatarUrl?.nonEmpty)
        }, next: page.next.map(FeedRequest.init), total: page.total?.intValue)
    }

    func add(_ category: BlacklistCategory, _ value: String) async throws {
        let _: IOSSuccess = try await adapter.call {
            self.client.blacklists.add(category: category.rawValue, value: value, completion: $0)
        }
    }

    func remove(_ entry: BlacklistEntry) async throws {
        let _: IOSSuccess = try await adapter.call {
            self.client.blacklists.remove(category: entry.category.rawValue, value: entry.value, completion: $0)
        }
    }
}

enum BlacklistSuggestion: Identifiable, Equatable {
    case user(UserSuggestion)
    case tag(TagSuggestion)
    var id: String {
        switch self {
        case .user(let value): "user:\(value.username)"
        case .tag(let value): "tag:\(value.name)"
        }
    }
    var value: String {
        switch self {
        case .user(let value): value.username
        case .tag(let value): value.name
        }
    }
}

/// One category's list, add form, and suggestions. Categories load independently, as on Android.

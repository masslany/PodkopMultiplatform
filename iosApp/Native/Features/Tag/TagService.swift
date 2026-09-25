import Foundation
import Observation
import PodkopShared

@MainActor protocol TagLoading {
    func firstRequest(isLoggedIn: Bool) -> FeedRequest
    func details(_ tag: String) async throws -> NativeTagDetails
    func stream(_ tag: String, sort: String, type: String, request: FeedRequest, loaded: Int) async throws
        -> ListPage<NativeResource>
    func setObserved(_ tag: String, _ enabled: Bool) async throws
    func setNotifications(_ tag: String, _ enabled: Bool) async throws
    func setBlacklisted(_ tag: String, _ enabled: Bool) async throws
}

@MainActor
final class SharedTagLoader: TagLoading {
    private let client: PodkopClient
    private let adapter: BridgeAdapter

    init(client: PodkopClient, adapter: BridgeAdapter) {
        self.client = client
        self.adapter = adapter
    }

    func firstRequest(isLoggedIn: Bool) -> FeedRequest {
        FeedRequest(client.tag.firstRequest(isLoggedIn: isLoggedIn))
    }

    func details(_ tag: String) async throws -> NativeTagDetails {
        let value: IOSTagDetails = try await adapter.call { self.client.tag.details(tag: tag, completion: $0) }
        return NativeTagDetails(name: value.name, description: value.description_,
                                followers: Int(value.followers), bannerURL: value.bannerUrl,
                                observed: value.observed, notificationsEnabled: value.notificationsEnabled,
                                blacklisted: value.blacklisted)
    }

    func stream(_ tag: String, sort: String, type: String, request: FeedRequest, loaded: Int) async throws
        -> ListPage<NativeResource> {
        let page: IOSResourceListPage = try await adapter.call {
            self.client.tag.stream(tag: tag, sort: sort, type: type,
                                   request: IOSPageRequest(kind: request.kind, value: request.value),
                                   loaded: Int32(loaded), completion: $0)
        }
        return ListPage(page)
    }

    func setObserved(_ tag: String, _ enabled: Bool) async throws {
        let _: IOSSuccess = try await adapter.call {
            self.client.tag.setObserved(tag: tag, enabled: enabled, completion: $0)
        }
    }

    func setNotifications(_ tag: String, _ enabled: Bool) async throws {
        let _: IOSSuccess = try await adapter.call {
            self.client.tag.setNotifications(tag: tag, enabled: enabled, completion: $0)
        }
    }

    func setBlacklisted(_ tag: String, _ enabled: Bool) async throws {
        let _: IOSSuccess = try await adapter.call {
            self.client.tag.setBlacklisted(tag: tag, enabled: enabled, completion: $0)
        }
    }
}

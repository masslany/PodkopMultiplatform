import Foundation
import Observation
import PodkopShared

@MainActor protocol DetailLoading {
    func resource(kind: NativeResourceKind, id: Int) async throws -> NativeResource
    func comments(kind: NativeResourceKind, id: Int, page: Int, sort: String) async throws -> FeedPage
    func replies(linkID: Int, commentID: Int, page: Int) async throws -> FeedPage
    func related(linkID: Int) async throws -> [NativeResource]
}

@MainActor
final class SharedDetailLoader: DetailLoading {
    private let client: PodkopClient
    private let adapter: BridgeAdapter

    init(client: PodkopClient, adapter: BridgeAdapter) {
        self.client = client
        self.adapter = adapter
    }

    func resource(kind: NativeResourceKind, id: Int) async throws -> NativeResource {
        let value: IOSResource = try await adapter.call { completion in
            if kind == .link {
                self.client.details.link(id: Int32(id), completion: completion)
            } else {
                self.client.details.entry(id: Int32(id), completion: completion)
            }
        }
        return NativeResource(value)
    }

    func comments(kind: NativeResourceKind, id: Int, page: Int, sort: String) async throws -> FeedPage {
        let value: IOSResourcePage = try await adapter.call { completion in
            if kind == .link {
                self.client.details.linkComments(linkId: Int32(id), page: Int32(page),
                                                 sort: sort, completion: completion)
            } else {
                self.client.details.entryComments(entryId: Int32(id), page: Int32(page),
                                                  completion: completion)
            }
        }
        return FeedPage(items: value.items.map(NativeResource.init), next: value.next,
                        total: value.total?.intValue)
    }

    func replies(linkID: Int, commentID: Int, page: Int) async throws -> FeedPage {
        let value: IOSResourcePage = try await adapter.call {
            self.client.details.linkReplies(linkId: Int32(linkID), commentId: Int32(commentID),
                                            page: Int32(page), completion: $0)
        }
        return FeedPage(items: value.items.map(NativeResource.init), next: value.next,
                        total: value.total?.intValue)
    }

    func related(linkID: Int) async throws -> [NativeResource] {
        let value: IOSResourcePage = try await adapter.call {
            self.client.details.relatedLinks(linkId: Int32(linkID), completion: $0)
        }
        return value.items.map(NativeResource.init)
    }
}

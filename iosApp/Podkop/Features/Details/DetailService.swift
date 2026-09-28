import Foundation
import Observation
import PodkopShared

@MainActor protocol DetailLoading {
    func resource(kind: ResourceKind, id: Int) async throws -> Resource
    func comments(kind: ResourceKind, id: Int, page: Int, sort: String) async throws -> FeedPage
    func replies(linkID: Int, commentID: Int, page: Int) async throws -> FeedPage
    func related(linkID: Int) async throws -> [Resource]
    /// The first page of an entry's comments as reply trees.
    func entryThread(entryID: Int) async throws -> ThreadReplies
    /// Replies to `parentID` (top-level comments when nil) after the sibling `afterID`.
    func entryThreadReplies(entryID: Int, parentID: Int?, afterID: Int?) async throws -> ThreadReplies
}

struct ThreadsUnavailable: Error {}

/// Loaders without threads make the entry fall back to its flat comment list.
extension DetailLoading {
    func entryThread(entryID: Int) async throws -> ThreadReplies { throw ThreadsUnavailable() }
    func entryThreadReplies(entryID: Int, parentID: Int?, afterID: Int?) async throws -> ThreadReplies {
        throw ThreadsUnavailable()
    }
}

@MainActor
final class SharedDetailLoader: DetailLoading {
    private let client: PodkopClient
    private let adapter: BridgeAdapter

    init(client: PodkopClient, adapter: BridgeAdapter) {
        self.client = client
        self.adapter = adapter
    }

    func resource(kind: ResourceKind, id: Int) async throws -> Resource {
        let value: IOSResource = try await adapter.call { completion in
            if kind == .link {
                self.client.details.link(id: Int32(id), completion: completion)
            } else {
                self.client.details.entry(id: Int32(id), completion: completion)
            }
        }
        return Resource(value)
    }

    func comments(kind: ResourceKind, id: Int, page: Int, sort: String) async throws -> FeedPage {
        let value: IOSResourcePage = try await adapter.call { completion in
            if kind == .link {
                self.client.details.linkComments(linkId: Int32(id), page: Int32(page),
                                                 sort: sort, completion: completion)
            } else {
                self.client.details.entryComments(entryId: Int32(id), page: Int32(page),
                                                  completion: completion)
            }
        }
        return FeedPage(items: value.items.map(Resource.init), next: value.next,
                        total: value.total?.intValue)
    }

    func replies(linkID: Int, commentID: Int, page: Int) async throws -> FeedPage {
        let value: IOSResourcePage = try await adapter.call {
            self.client.details.linkReplies(linkId: Int32(linkID), commentId: Int32(commentID),
                                            page: Int32(page), completion: $0)
        }
        return FeedPage(items: value.items.map(Resource.init), next: value.next,
                        total: value.total?.intValue)
    }

    func entryThread(entryID: Int) async throws -> ThreadReplies {
        let value: IOSThreadReplies = try await adapter.call {
            self.client.details.entryThread(entryId: Int32(entryID), completion: $0)
        }
        return ThreadReplies(value)
    }

    func entryThreadReplies(entryID: Int, parentID: Int?, afterID: Int?) async throws -> ThreadReplies {
        let value: IOSThreadReplies = try await adapter.call {
            self.client.details.entryThreadReplies(
                entryId: Int32(entryID),
                parentCommentId: parentID.map { KotlinInt(int: Int32($0)) },
                afterId: afterID.map { KotlinInt(int: Int32($0)) },
                completion: $0)
        }
        return ThreadReplies(value)
    }

    func related(linkID: Int) async throws -> [Resource] {
        let value: IOSResourcePage = try await adapter.call {
            self.client.details.relatedLinks(linkId: Int32(linkID), completion: $0)
        }
        return value.items.map(Resource.init)
    }
}

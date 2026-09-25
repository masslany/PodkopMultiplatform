import Foundation
import PodkopShared

enum DetailMutation {
    case voteUp(NativeResource, remove: Bool)
    case voteDown(NativeResource, remove: Bool, reason: String?)
    case favourite(NativeResource, enabled: Bool)
    case survey(entryID: Int, option: Int)
    case relatedVote(linkID: Int, resource: NativeResource, remove: Bool, down: Bool)
    case delete(NativeResource)

    var identity: ResourceIdentity {
        switch self {
        case .voteUp(let value, _), .voteDown(let value, _, _),
             .favourite(let value, _), .delete(let value): ResourceIdentity(value)
        case .relatedVote(_, let value, _, _): ResourceIdentity(value)
        case .survey(let id, _): ResourceIdentity(
            NativeResource(sourceID: id, kind: .entry, body: "")
        )
        }
    }
}

@MainActor protocol DetailMutating {
    func apply(_ mutation: DetailMutation) async throws
}

@MainActor
final class SharedDetailMutator: DetailMutating {
    private let client: PodkopClient
    private let adapter: BridgeAdapter

    init(client: PodkopClient, adapter: BridgeAdapter) {
        self.client = client
        self.adapter = adapter
    }

    func apply(_ mutation: DetailMutation) async throws {
        switch mutation {
        case .voteUp(let value, let remove):
            let _: IOSSuccess = try await adapter.call {
                self.client.mutations.voteUp(kind: value.kind.rawValue, id: Int32(value.sourceID),
                                             parentId: value.parentID.map { KotlinInt(int: Int32($0)) },
                                             remove: remove, completion: $0)
            }
        case .voteDown(let value, let remove, let reason):
            let _: IOSSuccess = try await adapter.call {
                self.client.mutations.voteDown(kind: value.kind.rawValue, id: Int32(value.sourceID),
                                               parentId: value.parentID.map { KotlinInt(int: Int32($0)) },
                                               remove: remove, reason: reason, completion: $0)
            }
        case .favourite(let value, let enabled):
            let _: IOSSuccess = try await adapter.call {
                self.client.mutations.setFavourite(kind: value.kind.rawValue,
                                                   id: Int32(value.sourceID), enabled: enabled,
                                                   completion: $0)
            }
        case .survey(let entryID, let option):
            let _: IOSSuccess = try await adapter.call {
                self.client.mutations.voteSurvey(entryId: Int32(entryID), option: Int32(option),
                                                 completion: $0)
            }
        case .relatedVote(let linkID, let value, let remove, let down):
            let _: IOSSuccess = try await adapter.call {
                self.client.mutations.voteRelated(linkId: Int32(linkID),
                                                  relatedId: Int32(value.sourceID),
                                                  remove: remove, down: down, completion: $0)
            }
        case .delete(let value):
            let _: IOSSuccess = try await adapter.call {
                self.client.mutations.delete(kind: value.kind.rawValue, id: Int32(value.sourceID),
                                             parentId: value.parentID.map { KotlinInt(int: Int32($0)) }, completion: $0)
            }
        }
    }
}

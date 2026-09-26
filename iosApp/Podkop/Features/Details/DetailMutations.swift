import Foundation
import OSLog
import PodkopShared

enum DetailMutation {
    case voteUp(Resource, remove: Bool)
    case voteDown(Resource, remove: Bool, reason: String?)
    case favourite(Resource, enabled: Bool)
    case survey(entryID: Int, option: Int)
    case relatedVote(linkID: Int, resource: Resource, remove: Bool, down: Bool)
    case delete(Resource)

    var identity: ResourceIdentity {
        switch self {
        case .voteUp(let value, _), .voteDown(let value, _, _),
             .favourite(let value, _), .delete(let value): ResourceIdentity(value)
        case .relatedVote(_, let value, _, _): ResourceIdentity(value)
        case .survey(let id, _): ResourceIdentity(
            Resource(sourceID: id, kind: .entry, body: "")
        )
        }
    }
}

@MainActor protocol DetailMutating {
    func apply(_ mutation: DetailMutation) async throws
}

@MainActor
final class SharedDetailMutator: DetailMutating {
    private let logger = Logger(subsystem: "pl.masslany.podkop", category: "CommentVoting")
    private let client: PodkopClient
    private let adapter: BridgeAdapter

    init(client: PodkopClient, adapter: BridgeAdapter) {
        self.client = client
        self.adapter = adapter
    }

    func apply(_ mutation: DetailMutation) async throws {
        if case .voteUp(let value, let remove) = mutation {
            logger.info("Vote up kind=\(value.kind.rawValue, privacy: .public) id=\(value.sourceID) parent=\(value.parentID ?? -1) remove=\(remove)")
        }
        do {
            try await perform(mutation)
            if case .voteUp = mutation { logger.info("Vote up succeeded") }
        } catch {
            if case .voteUp = mutation {
                if let failure = error as? BridgeFailure {
                    logger.error("Vote up failed category=\(failure.category, privacy: .public) code=\(failure.code ?? "none", privacy: .public)")
                } else {
                    logger.error("Vote up failed type=\(String(describing: type(of: error)), privacy: .public)")
                }
            }
            throw error
        }
    }

    private func perform(_ mutation: DetailMutation) async throws {
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

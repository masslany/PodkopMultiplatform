import Foundation
import Observation

/// Votes, favourites and the actions sheet for resources shown in lists (feeds, tags, profiles,
/// search, favourites). Each resource allows one mutation at a time; after the server confirms,
/// the new vote or favourite state is published to `ResourceUpdates`, so every visible list and
/// detail shows it without reloading. A failure leaves the confirmed state unchanged.
@MainActor @Observable
final class ResourceInteractor {
    private(set) var pending = Set<ResourceIdentity>()
    /// The resource whose actions sheet the scene's modal host presents.
    var actionTarget: NativeResource?
    private let mutator: DetailMutating
    private let updates: ResourceUpdates
    private let onFailure: () -> Void

    init(mutator: DetailMutating, updates: ResourceUpdates, onFailure: @escaping () -> Void) {
        self.mutator = mutator
        self.updates = updates
        self.onFailure = onFailure
    }

    func isPending(_ resource: NativeResource) -> Bool { pending.contains(ResourceIdentity(resource)) }

    func voteUp(_ resource: NativeResource) {
        let remove = resource.vote.state == "positive"
        run(.voteUp(resource, remove: remove), resource) { $0.vote = $0.vote.upvoted(remove: remove) }
    }

    /// Link comments only: burying a link needs a reason, which the link detail asks for.
    func voteDown(_ resource: NativeResource) {
        guard resource.kind == .linkComment else { return }
        let remove = resource.vote.state == "negative"
        run(.voteDown(resource, remove: remove, reason: nil), resource) { $0.vote = $0.vote.downvoted(remove: remove) }
    }

    func toggleFavourite(_ resource: NativeResource) {
        let enabled = !resource.favourite
        run(.favourite(resource, enabled: enabled), resource) { $0.favourite = enabled }
    }

    func delete(_ resource: NativeResource) {
        let identity = ResourceIdentity(resource)
        guard !pending.contains(identity) else { return }
        pending.insert(identity)
        let session = updates.sessionRevision
        Task { [weak self] in
            guard let self else { return }
            do {
                try await self.mutator.apply(.delete(resource))
                if session == self.updates.sessionRevision { self.updates.publish(.deleted, for: identity) }
            } catch {
                if !(error is CancellationError) { self.onFailure() }
            }
            self.pending.remove(identity)
        }
    }

    private func run(_ mutation: DetailMutation, _ resource: NativeResource,
                     confirmed change: @escaping (inout NativeResource) -> Void) {
        let identity = ResourceIdentity(resource)
        guard !pending.contains(identity) else { return }
        pending.insert(identity)
        let session = updates.sessionRevision
        Task { [weak self] in
            guard let self else { return }
            do {
                try await self.mutator.apply(mutation)
                guard session == self.updates.sessionRevision else { self.pending.remove(identity); return }
                // Build on the latest confirmed copy, in case another screen changed it meanwhile.
                var updated = self.updates.reconcile(resource) ?? resource
                change(&updated)
                self.updates.publish(.replacement(updated), for: identity)
            } catch {
                if !(error is CancellationError) { self.onFailure() }
            }
            self.pending.remove(identity)
        }
    }
}

extension NativeVote {
    /// The confirmed state after adding or removing an upvote.
    func upvoted(remove: Bool) -> NativeVote {
        if remove {
            return NativeVote(up: max(0, up - 1), down: down, state: "none", canUp: true, canDown: canDown, canUndo: false)
        }
        return NativeVote(up: up + 1, down: state == "negative" ? max(0, down - 1) : down, state: "positive",
                          canUp: canUp, canDown: canDown, canUndo: true)
    }

    /// The confirmed state after adding or removing a downvote.
    func downvoted(remove: Bool) -> NativeVote {
        if remove {
            return NativeVote(up: up, down: max(0, down - 1), state: "none", canUp: canUp, canDown: true, canUndo: false)
        }
        return NativeVote(up: state == "positive" ? max(0, up - 1) : up, down: down + 1, state: "negative",
                          canUp: canUp, canDown: canDown, canUndo: true)
    }
}

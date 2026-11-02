import Foundation
import Observation

struct ResourceIdentity: Hashable {
    let kind: ResourceKind
    let id: Int
    let parentID: Int?

    init(_ resource: Resource) {
        kind = resource.kind
        id = resource.sourceID
        parentID = resource.parentID
    }
}

enum ResourceChange {
    case replacement(Resource)
    case deleted
    case invalidated
}

/// Confirmed updates shared by visible models in one account session.
@MainActor @Observable
final class ResourceUpdates {
    private(set) var revision = 0
    private(set) var feedRevision = 0
    private(set) var sessionRevision = 0
    private var values: [ResourceIdentity: (revision: Int, change: ResourceChange)] = [:]
    private var blockedAuthors = Set<String>()

    func reset(for sessionRevision: Int) {
        guard self.sessionRevision != sessionRevision else { return }
        self.sessionRevision = sessionRevision
        values.removeAll()
        blockedAuthors.removeAll()
        revision += 1
    }

    func publish(_ change: ResourceChange, for identity: ResourceIdentity) {
        revision += 1
        values[identity] = (revision, change)
    }

    func publishNewResource() { feedRevision += 1 }

    /// App Store guideline 1.2: a blocked user's content leaves loaded lists at once, not on their next reload.
    func block(_ author: String) {
        blockedAuthors.insert(author.lowercased())
        revision += 1
    }

    func unblock(_ author: String) {
        guard blockedAuthors.remove(author.lowercased()) != nil else { return }
        revision += 1
    }

    func isBlocked(_ author: String?) -> Bool {
        author.map { blockedAuthors.contains($0.lowercased()) } ?? false
    }

    /// The latest confirmed copy, including embedded comments; nil once deleted or its author blocked.
    func reconcile(_ resource: Resource) -> Resource? {
        var current: Resource
        switch values[ResourceIdentity(resource)]?.change {
        case .replacement(let value): current = value
        case .deleted: return nil
        case .invalidated, .none: current = resource
        }
        if isBlocked(current.author?.name) {
            // Comments fold behind the blacklist notice instead, so the replies around them still read.
            guard current.kind == .entryComment || current.kind == .linkComment else { return nil }
            current.blacklisted = true
        }
        if !current.inlineComments.isEmpty {
            current.inlineComments = current.inlineComments.compactMap(reconcile)
        }
        return current
    }

    func needsReload(_ resources: [Resource], since revision: Int) -> Bool {
        resources.contains { resource in
            guard let update = values[ResourceIdentity(resource)], update.revision > revision else {
                return false
            }
            if case .invalidated = update.change { return true }
            return false
        }
    }
}

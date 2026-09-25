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

    func reset(for sessionRevision: Int) {
        guard self.sessionRevision != sessionRevision else { return }
        self.sessionRevision = sessionRevision
        values.removeAll()
        revision += 1
    }

    func publish(_ change: ResourceChange, for identity: ResourceIdentity) {
        revision += 1
        values[identity] = (revision, change)
    }

    func publishNewResource() { feedRevision += 1 }

    /// The latest confirmed copy, including embedded comments; nil once deleted.
    func reconcile(_ resource: Resource) -> Resource? {
        var current: Resource
        switch values[ResourceIdentity(resource)]?.change {
        case .replacement(let value): current = value
        case .deleted: return nil
        case .invalidated, .none: current = resource
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

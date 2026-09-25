import Foundation
import Observation

struct ResourceIdentity: Hashable {
    let kind: NativeResourceKind
    let id: Int
    let parentID: Int?

    init(_ resource: NativeResource) {
        kind = resource.kind
        id = resource.sourceID
        parentID = resource.parentID
    }
}

enum ResourceChange {
    case replacement(NativeResource)
    case deleted
    case invalidated
}

/// Confirmed updates shared by visible models in one account session.
@MainActor @Observable
final class ResourceUpdates {
    private(set) var revision = 0
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

    func reconcile(_ resource: NativeResource) -> NativeResource? {
        switch values[ResourceIdentity(resource)]?.change {
        case .replacement(let value): value
        case .deleted: nil
        case .invalidated, .none: resource
        }
    }

    func needsReload(_ resources: [NativeResource], since revision: Int) -> Bool {
        resources.contains { resource in
            guard let update = values[ResourceIdentity(resource)], update.revision > revision else {
                return false
            }
            if case .invalidated = update.change { return true }
            return false
        }
    }
}

import Foundation
import Observation
import PodkopShared

#if DEBUG
@MainActor
final class FixtureComposerSubmitter: ComposerSubmitting {
    func submit(intent: ComposerIntent, text: String, adult: Bool,
                photoKey: String?) async throws -> Resource {
        let target = intent.target
        let kind: ResourceKind
        switch target.kind {
        case "createEntry", "editEntry": kind = .entry
        case "createEntryComment", "editEntryComment": kind = .entryComment
        default: kind = .linkComment
        }
        return Resource(sourceID: target.isEdit ? (target.commentID ?? target.rootID ?? 801) : 801,
                              kind: kind, body: text, adult: adult,
                              parentID: kind == .entry ? nil : target.rootID)
    }
}
#endif

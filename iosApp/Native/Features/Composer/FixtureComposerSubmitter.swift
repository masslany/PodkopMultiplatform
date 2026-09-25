import Foundation
import Observation
import PodkopShared

#if DEBUG
@MainActor
final class FixtureComposerSubmitter: ComposerSubmitting {
    func submit(intent: ComposerIntent, text: String, adult: Bool,
                photoKey: String?) async throws -> NativeResource {
        let target = intent.target
        let kind: NativeResourceKind
        switch target.kind {
        case "createEntry", "editEntry": kind = .entry
        case "createEntryComment", "editEntryComment": kind = .entryComment
        default: kind = .linkComment
        }
        return NativeResource(sourceID: target.isEdit ? (target.commentID ?? target.rootID ?? 801) : 801,
                              kind: kind, body: text, adult: adult,
                              parentID: kind == .entry ? nil : target.rootID)
    }
}
#endif

import Foundation
import Observation
import PodkopShared

struct ComposerTarget {
    let kind: String
    let rootID: Int?
    let commentID: Int?
    let replyTarget: String?
    let isEdit: Bool
}

extension ComposerIntent {
    var target: ComposerTarget {
        switch self {
        case .createEntry:
            ComposerTarget(kind: "createEntry", rootID: nil, commentID: nil,
                           replyTarget: nil, isEdit: false)
        case .createEntryComment(let id, let replyTarget):
            ComposerTarget(kind: "createEntryComment", rootID: id, commentID: nil,
                           replyTarget: replyTarget, isEdit: false)
        case .createLinkComment(let id, let parentID, let replyTarget):
            ComposerTarget(kind: "createLinkComment", rootID: id, commentID: parentID,
                           replyTarget: replyTarget, isEdit: false)
        case .editEntry(let id):
            ComposerTarget(kind: "editEntry", rootID: id, commentID: nil,
                           replyTarget: nil, isEdit: true)
        case .editEntryComment(let entryID, let commentID):
            ComposerTarget(kind: "editEntryComment", rootID: entryID, commentID: commentID,
                           replyTarget: nil, isEdit: true)
        case .editLinkComment(let linkID, let commentID):
            ComposerTarget(kind: "editLinkComment", rootID: linkID, commentID: commentID,
                           replyTarget: nil, isEdit: true)
        }
    }
}

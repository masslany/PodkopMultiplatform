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
    var title: LocalizedStringResource {
        switch self {
        case .createEntry: .commonWritePost
        case .createEntryComment, .createEntryThreadReply, .createLinkComment: .composerAddComment
        case .editEntry: .composerEditEntry
        case .editEntryComment, .editLinkComment: .composerEditComment
        }
    }

    /// Android's editor hint: entries ask for a post, every comment for a reply.
    var hint: LocalizedStringResource {
        switch self {
        case .createEntry, .editEntry: .composerEntriesComposerHint
        default: .composerEntryDetailsReplyComposerHint
        }
    }

    var replyPrefix: String {
        guard !target.isEdit, let author = target.replyTarget else { return "" }
        let nickname = author.trimmingCharacters(in: .whitespacesAndNewlines)
            .drop(while: { $0 == "@" })
        return nickname.isEmpty ? "" : "@\(nickname): "
    }

    var target: ComposerTarget {
        switch self {
        case .createEntry:
            ComposerTarget(kind: "createEntry", rootID: nil, commentID: nil,
                           replyTarget: nil, isEdit: false)
        case .createEntryComment(let id, let replyTarget):
            ComposerTarget(kind: "createEntryComment", rootID: id, commentID: nil,
                           replyTarget: replyTarget, isEdit: false)
        case .createEntryThreadReply(let id, let parentID, let replyTarget):
            ComposerTarget(kind: "createEntryThreadReply", rootID: id, commentID: parentID,
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

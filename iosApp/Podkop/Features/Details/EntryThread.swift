import Foundation
import PodkopShared

/// A loaded slice of one node's direct replies; `totalCount` counts all of them on the server.
struct ThreadReplies {
    var totalCount: Int
    var items: [ThreadComment]
}

struct ThreadComment {
    var resource: Resource
    /// 0 for comments directly under the entry, +1 per reply level.
    var depth: Int
    var isByEntryAuthor: Bool
    /// The comment a reply to this one is posted under (its parent past the server's depth limit).
    var replyParentID: Int
    var replies: ThreadReplies
}

extension ThreadReplies {
    init(_ value: IOSThreadReplies) {
        self.init(totalCount: Int(value.totalCount), items: value.items.map(ThreadComment.init))
    }

    /// Every comment in the page, parents before their replies.
    var flattened: [Resource] {
        items.flatMap { [$0.resource] + $0.replies.flattened }
    }
}

extension ThreadComment {
    init(_ value: IOSThreadComment) {
        self.init(resource: Resource(value.resource), depth: Int(value.depth),
                  isByEntryAuthor: value.isByEntryAuthor, replyParentID: Int(value.replyParentId),
                  replies: ThreadReplies(value.replies))
    }
}

/// The shape of an entry's threaded comments (Android's `EntryCommentThreadTree`): which comment
/// sits under which, how deep, and how many replies each branch still has on the server. The
/// comment contents live in the model, keyed by id.
struct EntryThreadTree: Equatable {
    struct Branch: Equatable {
        var childIDs: [Int] = []
        var totalCount = 0
        /// The last child loaded from the server; the next page starts after it.
        var cursorID: Int?
        var remainingCount: Int { max(0, totalCount - childIDs.count) }
    }

    struct Node: Equatable {
        let depth: Int
        let isByEntryAuthor: Bool
        let replyParentID: Int
        var replies: Branch
    }

    enum Row: Equatable {
        case comment(id: Int, depth: Int, isByEntryAuthor: Bool)
        case moreReplies(parentID: Int, depth: Int, remaining: Int)
    }

    private(set) var root = Branch()
    private var nodes: [Int: Node] = [:]

    init(_ comments: ThreadReplies) {
        appendPage(parentID: nil, comments)
    }

    var hasMoreTopLevel: Bool { root.remainingCount > 0 }
    var topLevelCursorID: Int? { root.cursorID }

    func repliesCursorID(_ parentID: Int) -> Int? { nodes[parentID]?.replies.cursorID }

    /// The comment a reply to `commentID` is posted under, or nil when it is not loaded.
    func replyParentID(for commentID: Int) -> Int? { nodes[commentID]?.replyParentID }

    /// Adds a page loaded from the server under `parentID`, or under the entry when it is nil.
    mutating func appendPage(parentID: Int?, _ page: ThreadReplies) {
        let existing = if let parentID { nodes[parentID]?.replies } else { root }
        guard var branch = existing else { return }
        let known = Set(branch.childIDs)
        for comment in page.items where !known.contains(comment.resource.sourceID) {
            register(comment)
            branch.childIDs.append(comment.resource.sourceID)
        }
        branch.totalCount = max(page.totalCount, branch.childIDs.count)
        branch.cursorID = page.items.last?.resource.sourceID ?? branch.cursorID
        if let parentID { nodes[parentID]?.replies = branch } else { root = branch }
    }

    /// Depth-first rows, with a "more replies" row after each branch that has unloaded replies.
    var rows: [Row] {
        var rows: [Row] = []
        func add(_ ids: [Int]) {
            for id in ids {
                guard let node = nodes[id] else { continue }
                rows.append(.comment(id: id, depth: node.depth, isByEntryAuthor: node.isByEntryAuthor))
                add(node.replies.childIDs)
                if node.replies.remainingCount > 0 {
                    rows.append(.moreReplies(parentID: id, depth: node.depth + 1,
                                             remaining: node.replies.remainingCount))
                }
            }
        }
        add(root.childIDs)
        return rows
    }

    private mutating func register(_ comment: ThreadComment) {
        nodes[comment.resource.sourceID] = Node(
            depth: comment.depth,
            isByEntryAuthor: comment.isByEntryAuthor,
            replyParentID: comment.replyParentID,
            replies: Branch(childIDs: comment.replies.items.map(\.resource.sourceID),
                            totalCount: comment.replies.totalCount,
                            cursorID: comment.replies.items.last?.resource.sourceID)
        )
        comment.replies.items.forEach { register($0) }
    }
}

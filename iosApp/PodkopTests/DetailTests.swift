import XCTest
@testable import Podkop

@MainActor
private final class DetailFixtureLoader: DetailLoading {
    var commentCalls: [Int] = []
    var replyCalls: [Int] = []
    var failPageTwo = false
    var failRelated = false

    func resource(kind: ResourceKind, id: Int) async throws -> Resource {
        kind == .link ? ContentFixtures.link : ContentFixtures.entry
    }
    func comments(kind: ResourceKind, id: Int, page: Int, sort: String) async throws -> FeedPage {
        commentCalls.append(page)
        if page == 2 && failPageTwo { throw NSError(domain: "fixture", code: 1) }
        let first = Resource(sourceID: 201, kind: .linkComment, body: "first", parentID: id)
        let second = Resource(sourceID: 202, kind: .linkComment, body: "second", parentID: id)
        return page == 1 ? FeedPage(items: [first], next: nil, total: 2)
            : FeedPage(items: [first, second], next: nil, total: 2)
    }
    func replies(linkID: Int, commentID: Int, page: Int) async throws -> FeedPage {
        replyCalls.append(page)
        let reply = Resource(sourceID: 203, kind: .linkComment, body: "reply", parentID: commentID)
        return FeedPage(items: page == 1 ? [reply] : [], next: nil, total: 1)
    }
    func related(linkID: Int) async throws -> [Resource] {
        if failRelated { throw NSError(domain: "fixture", code: 3) }
        return []
    }
}

@MainActor
private final class ThreadFixtureLoader: DetailLoading {
    var threadCalls = 0
    var flatCalls = 0
    var replyCalls: [(parent: Int?, after: Int?)] = []
    var failThread = false

    func resource(kind: ResourceKind, id: Int) async throws -> Resource { ContentFixtures.entry }
    func comments(kind: ResourceKind, id: Int, page: Int, sort: String) async throws -> FeedPage {
        flatCalls += 1
        return FeedPage(items: [comment(900)], next: nil, total: 1)
    }
    func replies(linkID: Int, commentID: Int, page: Int) async throws -> FeedPage {
        FeedPage(items: [], next: nil, total: 0)
    }
    func related(linkID: Int) async throws -> [Resource] { [] }

    func entryThread(entryID: Int) async throws -> ThreadReplies {
        threadCalls += 1
        if failThread { throw NSError(domain: "fixture", code: 4) }
        return ThreadReplies(totalCount: 3, items: [
            node(10, depth: 0, replies: ThreadReplies(totalCount: 4, items: [node(11, depth: 1, author: true)])),
            node(20, depth: 0),
        ])
    }

    func entryThreadReplies(entryID: Int, parentID: Int?, afterID: Int?) async throws -> ThreadReplies {
        replyCalls.append((parentID, afterID))
        if parentID == 10 {
            return ThreadReplies(totalCount: 4, items: [node(12, depth: 1), node(13, depth: 1)])
        }
        return ThreadReplies(totalCount: 3, items: [node(30, depth: 0)])
    }

    private func comment(_ id: Int) -> Resource {
        Resource(sourceID: id, kind: .entryComment, body: "c\(id)", parentID: 102)
    }

    private func node(_ id: Int, depth: Int, author: Bool = false,
                      replies: ThreadReplies = ThreadReplies(totalCount: 0, items: [])) -> ThreadComment {
        ThreadComment(resource: comment(id), depth: depth, isByEntryAuthor: author,
                      replyParentID: id, replies: replies)
    }
}

@MainActor
private final class ControlledDetailMutator: DetailMutating {
    var calls = 0
    var finish: ((Result<Void, Error>) -> Void)?
    func apply(_ mutation: DetailMutation) async throws {
        calls += 1
        try await withCheckedThrowingContinuation { continuation in
            finish = { continuation.resume(with: $0) }
        }
    }
}

@MainActor
private final class VoterFixtureLoader: VoterLoading {
    var pages: [Int] = []
    var failSecond = true
    func load(target: VoterTarget, page: Int) async throws -> VoterPage {
        pages.append(page)
        if page == 2 && failSecond { throw NSError(domain: "fixture", code: 2) }
        let first = Voter(username: "Ewa", avatarURL: "", verified: true, reason: nil)
        let second = Voter(username: "Ola", avatarURL: "", verified: false, reason: "Spam")
        return VoterPage(items: page == 1 ? [first] : [first, second], total: 2)
    }
}

@MainActor
final class DetailTests: XCTestCase {
    private func settle() async { for _ in 0..<30 { await Task.yield() } }

    func testCommentPageRetryDeduplicationAndNestedReplies() async {
        let loader = DetailFixtureLoader()
        let updates = ResourceUpdates()
        let model = DetailModel(kind: .link, id: 101, loader: loader,
                                mutator: FixtureDetailMutator(), updates: updates)
        model.start()
        await settle()
        XCTAssertEqual(model.comments.map(\.sourceID), [201])
        loader.failPageTwo = true
        model.loadMoreComments()
        await settle()
        XCTAssertTrue(model.nextCommentsError)
        XCTAssertEqual(model.comments.map(\.sourceID), [201])
        loader.failPageTwo = false
        model.retryComments()
        await settle()
        XCTAssertEqual(loader.commentCalls, [1, 2, 2])
        XCTAssertEqual(model.comments.map(\.sourceID), [201, 202])
        XCTAssertTrue(model.commentsExhausted)
        model.loadReplies(for: 201)
        await settle()
        XCTAssertEqual(model.replies[201]?.rows.map(\.sourceID), [203])
        XCTAssertTrue(model.replies[201]?.exhausted == true)
        XCTAssertEqual(loader.replyCalls, [1])
    }

    private func rowSummary(_ rows: [DetailModel.ThreadRow]?) -> [String] {
        (rows ?? []).map { row in
            switch row {
            case .comment(let comment, let depth, let author, _): "\(comment.sourceID)@\(depth)\(author ? "*" : "")"
            case .moreReplies(let parent, let depth, let remaining, _, _, _): "more\(parent)@\(depth):\(remaining)"
            }
        }
    }

    func testThreadedEntryCommentsLoadRepliesAndTopLevelPagesAfterTheirCursor() async {
        let loader = ThreadFixtureLoader()
        let model = DetailModel(kind: .entry, id: 102, loader: loader, mutator: FixtureDetailMutator(),
                                updates: ResourceUpdates(), threadedEntryComments: { true })
        model.start()
        await settle()
        XCTAssertEqual(loader.flatCalls, 0)
        XCTAssertEqual(rowSummary(model.threadRows), ["10@0", "11@1*", "more10@1:3", "20@0"])
        XCTAssertEqual(model.thread?.replyParentID(for: 11), 11)

        model.loadThreadReplies(for: 10)
        await settle()
        XCTAssertEqual(loader.replyCalls.last?.parent, 10)
        XCTAssertEqual(loader.replyCalls.last?.after, 11)
        XCTAssertEqual(rowSummary(model.threadRows), ["10@0", "11@1*", "12@1", "13@1", "more10@1:1", "20@0"])

        model.loadMoreComments()
        await settle()
        XCTAssertNil(loader.replyCalls.last?.parent)
        XCTAssertEqual(loader.replyCalls.last?.after, 20)
        XCTAssertEqual(rowSummary(model.threadRows).last, "30@0")
        XCTAssertTrue(model.commentsExhausted)
    }

    func testThreadConnectorsRunOnOnlyToLaterSiblings() {
        func node(_ id: Int, _ depth: Int, total: Int? = nil, _ replies: [ThreadComment] = []) -> ThreadComment {
            ThreadComment(resource: Resource(sourceID: id, kind: .entryComment, body: "", parentID: 1),
                          depth: depth, isByEntryAuthor: false, replyParentID: id,
                          replies: ThreadReplies(totalCount: total ?? replies.count, items: replies))
        }
        let tree = EntryThreadTree(ThreadReplies(totalCount: 1, items: [
            node(10, 0, [node(11, 1, [node(111, 2)]), node(12, 1, total: 2, [node(121, 2)])]),
        ]))

        let connectors = tree.rows.map { row -> ThreadConnectors in
            switch row {
            case .comment(_, _, _, let connectors), .moreReplies(_, _, _, let connectors): connectors
            }
        }

        XCTAssertEqual(connectors, [
            ThreadConnectors(hasReplies: true),
            ThreadConnectors(continuesBelow: true, hasReplies: true),
            ThreadConnectors(ancestorLines: [true]),
            ThreadConnectors(hasReplies: true),
            ThreadConnectors(ancestorLines: [false], continuesBelow: true),
            ThreadConnectors(ancestorLines: [false]),
        ])
    }

    func testThreadedCommentsFallBackToTheFlatListWhenTheThreadFails() async {
        let loader = ThreadFixtureLoader()
        loader.failThread = true
        let model = DetailModel(kind: .entry, id: 102, loader: loader, mutator: FixtureDetailMutator(),
                                updates: ResourceUpdates(), threadedEntryComments: { true })
        model.start()
        await settle()
        XCTAssertEqual(loader.threadCalls, 1)
        XCTAssertNil(model.threadRows)
        XCTAssertEqual(model.comments.map(\.sourceID), [900])
    }

    func testEntryCommentsStayFlatWithThreadsOff() async {
        let loader = ThreadFixtureLoader()
        let model = DetailModel(kind: .entry, id: 102, loader: loader, mutator: FixtureDetailMutator(),
                                updates: ResourceUpdates())
        model.start()
        await settle()
        XCTAssertEqual(loader.threadCalls, 0)
        XCTAssertNil(model.threadRows)
        XCTAssertEqual(model.comments.map(\.sourceID), [900])
    }

    func testDeletedThreadCommentDropsOutOfTheRows() async {
        let loader = ThreadFixtureLoader()
        let updates = ResourceUpdates()
        let model = DetailModel(kind: .entry, id: 102, loader: loader, mutator: FixtureDetailMutator(),
                                updates: updates, threadedEntryComments: { true })
        model.start()
        await settle()
        updates.publish(.deleted, for: ResourceIdentity(
            Resource(sourceID: 20, kind: .entryComment, body: "", parentID: 102)))
        model.reconcileUpdates()
        XCTAssertEqual(rowSummary(model.threadRows), ["10@0", "11@1*", "more10@1:3"])
    }

    func testRelatedSectionReportsEmptyAndFailedLoads() async {
        let loader = DetailFixtureLoader()
        let model = DetailModel(kind: .link, id: 101, loader: loader,
                                mutator: FixtureDetailMutator(), updates: ResourceUpdates())
        model.start()
        await settle()
        XCTAssertEqual(model.relatedPhase, .loaded, "an empty list shows Android's no-related text")
        loader.failRelated = true
        model.reload()
        await settle()
        XCTAssertEqual(model.relatedPhase, .failed)
    }

    func testConfirmedUpdateAndSessionReset() async throws {
        let loader = DetailFixtureLoader()
        let mutator = ControlledDetailMutator()
        let updates = ResourceUpdates()
        let model = DetailModel(kind: .link, id: 101, loader: loader,
                                mutator: mutator, updates: updates)
        model.start()
        await settle()
        let resource = try XCTUnwrap(model.resource)
        let command = DetailMutation.favourite(resource, enabled: true)
        model.submit(command)
        model.submit(command)
        await settle()
        XCTAssertEqual(mutator.calls, 1)
        XCTAssertEqual(model.resource?.favourite, resource.favourite) // confirmed updates only
        mutator.finish?(.success(()))
        await settle()
        // Like Android, the confirmed state replaces the row; nothing is fetched again.
        XCTAssertFalse(updates.needsReload([resource], since: 0))
        model.reconcileUpdates()
        XCTAssertEqual(model.resource?.favourite, true)
        XCTAssertEqual(loader.commentCalls, [1])
        updates.reset(for: 1)
        XCTAssertEqual(updates.reconcile(resource)?.favourite, resource.favourite)
    }

    func testDeleteReportsItsKindForTheConfirmation() async throws {
        let loader = DetailFixtureLoader()
        let mutator = ControlledDetailMutator()
        let model = DetailModel(kind: .link, id: 101, loader: loader, mutator: mutator, updates: ResourceUpdates())
        model.start()
        await settle()
        let comment = try XCTUnwrap(model.comments.first)
        model.submit(.delete(comment))
        await settle()
        XCTAssertNil(model.deletedKind)
        mutator.finish?(.success(()))
        await settle()
        XCTAssertEqual(model.deletedKind, .linkComment)
        model.acknowledgeDeletion()
        XCTAssertNil(model.deletedKind)
    }

    func testVoteOnALoadedReplyUpdatesThatRowWithoutReloading() async throws {
        let loader = DetailFixtureLoader()
        let mutator = ControlledDetailMutator()
        let updates = ResourceUpdates()
        let model = DetailModel(kind: .link, id: 101, loader: loader, mutator: mutator, updates: updates)
        model.start()
        await settle()
        model.loadReplies(for: 201)
        await settle()
        let reply = try XCTUnwrap(model.replies[201]?.rows.first)

        model.submit(.voteUp(reply, remove: false))
        await settle()
        mutator.finish?(.success(()))
        await settle()
        model.reconcileUpdates()

        XCTAssertEqual(model.replies[201]?.rows.first?.vote.state, "positive")
        XCTAssertEqual(model.replies[201]?.rows.first?.vote.up, reply.vote.up + 1)
        XCTAssertEqual(loader.commentCalls, [1], "a vote must not reload the comments")
        XCTAssertEqual(loader.replyCalls, [1])
        XCTAssertTrue(model.mutating.isEmpty)
    }

    func testResourceLinksUseRootAndNestedParent() {
        let link = Resource(sourceID: 10, kind: .link, body: "", slug: "test-link")
        let reply = Resource(sourceID: 22, kind: .linkComment, body: "reply", parentID: 21)
        XCTAssertEqual(ResourceLinkBuilder.url(for: link, root: link)?.absoluteString,
                       "https://wykop.pl/link/10/test-link")
        XCTAssertEqual(ResourceLinkBuilder.url(for: reply, root: link, parentCommentID: 21)?.absoluteString,
                       "https://wykop.pl/link/10/test-link/komentarz/21#22")
        let entry = Resource(sourceID: 30, kind: .entry, body: "")
        let comment = Resource(sourceID: 31, kind: .entryComment, body: "", parentID: 30)
        XCTAssertEqual(ResourceLinkBuilder.url(for: comment, root: entry)?.absoluteString,
                       "https://wykop.pl/wpis/30/komentarz/31")
    }

    func testResourceLinksWithoutSlugFallBackToRootInsteadOfInvalidCommentRoute() {
        let link = Resource(sourceID: 10, kind: .link, body: "")
        let reply = Resource(sourceID: 22, kind: .linkComment, body: "reply", parentID: 21)
        XCTAssertEqual(ResourceLinkBuilder.url(for: link, root: link)?.absoluteString,
                       "https://wykop.pl/link/10")
        XCTAssertEqual(ResourceLinkBuilder.url(for: reply, root: link, parentCommentID: 21)?.absoluteString,
                       "https://wykop.pl/link/10")
    }

    func testResourceUpdatesReconcileMatchingIdentityOnly() {
        let updates = ResourceUpdates()
        let first = Resource(sourceID: 1, kind: .link, body: "old")
        let other = Resource(sourceID: 1, kind: .entry, body: "entry")
        let replacement = Resource(sourceID: 1, kind: .link, body: "new")
        updates.publish(.replacement(replacement), for: ResourceIdentity(first))
        XCTAssertEqual(updates.reconcile(first)?.body, "new")
        XCTAssertEqual(updates.reconcile(other)?.body, "entry")
        updates.publish(.deleted, for: ResourceIdentity(first))
        XCTAssertNil(updates.reconcile(first))
    }

    func testVoterPageRetryKeepsNumberAndDeduplicates() async {
        let loader = VoterFixtureLoader()
        let target = VoterTarget(kind: "entry", rootID: 1, commentID: nil, side: "up")
        let model = VoterModel(target: target, loader: loader)
        model.load()
        await settle()
        XCTAssertEqual(model.voters.map(\.username), ["Ewa"])
        model.load()
        await settle()
        XCTAssertTrue(model.failed)
        XCTAssertEqual(model.voters.map(\.username), ["Ewa"])
        loader.failSecond = false
        model.load()
        await settle()
        XCTAssertEqual(loader.pages, [1, 2, 2])
        XCTAssertEqual(model.voters.map(\.username), ["Ewa", "Ola"])
        XCTAssertTrue(model.exhausted)
    }
}

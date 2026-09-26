import XCTest
@testable import Podkop

@MainActor
private final class DetailFixtureLoader: DetailLoading {
    var commentCalls: [Int] = []
    var replyCalls: [Int] = []
    var failPageTwo = false

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
    func related(linkID: Int) async throws -> [Resource] { [] }
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
        XCTAssertTrue(updates.needsReload([resource], since: 0))
        updates.reset(for: 1)
        XCTAssertFalse(updates.needsReload([resource], since: 0))
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

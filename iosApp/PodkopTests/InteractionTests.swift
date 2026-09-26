import XCTest
@testable import Podkop

@MainActor
private final class GatedMutator: DetailMutating {
    var mutations: [DetailMutation] = []
    var finish: ((Result<Void, Error>) -> Void)?
    func apply(_ mutation: DetailMutation) async throws {
        mutations.append(mutation)
        try await withCheckedThrowingContinuation { continuation in
            finish = { continuation.resume(with: $0) }
        }
    }
}

@MainActor
final class InteractionTests: XCTestCase {
    private func settle() async { for _ in 0..<30 { await Task.yield() } }

    private func entry(state: String = "none", up: Int = 5, favourite: Bool = false) -> Resource {
        Resource(sourceID: 7, kind: .entry, body: "Treść", favourite: favourite,
                       vote: Vote(up: up, down: 1, state: state, canUp: true, canDown: false,
                                        canUndo: state != "none"))
    }

    func testVoteUpPublishesConfirmedStateAndBlocksDuplicates() async {
        let mutator = GatedMutator()
        let updates = ResourceUpdates()
        var failures = 0
        let interactor = ResourceInteractor(mutator: mutator, updates: updates) { failures += 1 }
        let item = entry()

        interactor.voteUp(item)
        interactor.voteUp(item)
        await settle()
        XCTAssertEqual(mutator.mutations.count, 1, "one mutation per resource at a time")
        XCTAssertTrue(interactor.isPending(item))
        XCTAssertEqual(updates.reconcile(item)?.vote.up, 5, "nothing changes before the server confirms")

        mutator.finish?(.success(()))
        await settle()
        XCTAssertFalse(interactor.isPending(item))
        XCTAssertEqual(updates.reconcile(item)?.vote.up, 6)
        XCTAssertEqual(updates.reconcile(item)?.vote.state, "positive")
        XCTAssertEqual(failures, 0)
    }

    func testFailureKeepsStateAndReports() async {
        let mutator = GatedMutator()
        let updates = ResourceUpdates()
        var failures = 0
        let interactor = ResourceInteractor(mutator: mutator, updates: updates) { failures += 1 }
        let item = entry(favourite: false)

        interactor.toggleFavourite(item)
        await settle()
        mutator.finish?(.failure(NSError(domain: "test", code: 1)))
        await settle()
        XCTAssertEqual(failures, 1)
        XCTAssertEqual(updates.reconcile(item)?.favourite, false)
        XCTAssertFalse(interactor.isPending(item))
    }

    func testSignOutDuringMutationDropsTheResult() async {
        let mutator = GatedMutator()
        let updates = ResourceUpdates()
        let interactor = ResourceInteractor(mutator: mutator, updates: updates) {}
        let item = entry()

        interactor.voteUp(item)
        await settle()
        updates.reset(for: 1)
        mutator.finish?(.success(()))
        await settle()
        XCTAssertEqual(updates.reconcile(item)?.vote.up, 5, "a result from the previous session is ignored")
    }

    func testDownvoteInListsIsOnlyForLinkComments() async {
        let mutator = GatedMutator()
        let interactor = ResourceInteractor(mutator: mutator, updates: ResourceUpdates()) {}
        interactor.voteDown(Resource(sourceID: 1, kind: .link, body: ""))
        interactor.voteDown(Resource(sourceID: 2, kind: .linkComment, body: "", parentID: 1))
        await settle()
        XCTAssertEqual(mutator.mutations.count, 1)
    }

    func testVotesOnEmbeddedCommentsReachTheirParentRow() async {
        let mutator = GatedMutator()
        let updates = ResourceUpdates()
        let interactor = ResourceInteractor(mutator: mutator, updates: updates) {}
        let comment = Resource(sourceID: 3, kind: .entryComment, body: "c", parentID: 7,
                                     vote: Vote(up: 1, down: 0, state: "none", canUp: true,
                                                      canDown: false, canUndo: false))
        let parent = Resource(sourceID: 7, kind: .entry, body: "e", inlineComments: [comment])

        interactor.voteUp(comment)
        await settle()
        mutator.finish?(.success(()))
        await settle()
        XCTAssertEqual(updates.reconcile(parent)?.inlineComments.first?.vote.up, 2)
    }

    func testVoteArithmetic() {
        let negative = Vote(up: 3, down: 2, state: "negative", canUp: true, canDown: true, canUndo: true)
        let up = negative.upvoted(remove: false)
        XCTAssertEqual([up.up, up.down], [4, 1])
        XCTAssertEqual(up.state, "positive")
        let removed = up.upvoted(remove: true)
        XCTAssertEqual([removed.up, removed.down], [3, 1])
        XCTAssertEqual(removed.state, "none")
        let down = up.downvoted(remove: false)
        XCTAssertEqual([down.up, down.down], [3, 2])
        XCTAssertEqual(down.state, "negative")
    }

    func testPublishedTimeFollowsAndroidRules() {
        let now = Date(timeIntervalSince1970: 1_800_000_000)
        XCTAssertEqual(PublishedTime.text(now.addingTimeInterval(-20), now: now), String(localized: .contentJustNow))
        XCTAssertEqual(PublishedTime.text(now.addingTimeInterval(-5 * 60), now: now), String(localized: .contentMinutesAgo(5)))
        XCTAssertEqual(PublishedTime.text(now.addingTimeInterval(-2 * 3600), now: now), String(localized: .contentHoursAgo(2)))
        XCTAssertEqual(PublishedTime.text(now.addingTimeInterval(-(2 * 3600 + 15 * 60)), now: now),
                       String(localized: .contentHoursMinutesAgo(2, 15)))
        XCTAssertEqual(PublishedTime.text(now.addingTimeInterval(-3 * 86400), now: now), String(localized: .contentDaysAgo(3)))
        let old = now.addingTimeInterval(-30 * 86400)
        XCTAssertTrue(PublishedTime.text(old, now: now).range(of: #"^\d{4}\.\d{2}\.\d{2} \d{2}:\d{2}$"#,
                                                               options: .regularExpression) != nil)
        XCTAssertNil(PublishedTime.text(iso: nil, now: now))
    }

    func testLinkCommentAccentFollowsAndroidPriority() {
        XCTAssertEqual(CommentAccent.resolve(author: "Ja", linkAuthor: "Ja", parentAuthor: "Ja", currentUser: "Ja"),
                       .currentUser)
        XCTAssertEqual(CommentAccent.resolve(author: "Ewa", linkAuthor: "Ewa", parentAuthor: "Ewa", currentUser: "Ja"),
                       .linkAuthor)
        XCTAssertEqual(CommentAccent.resolve(author: "Ola", linkAuthor: "Ewa", parentAuthor: "Ola", currentUser: nil),
                       .parentAuthor)
        XCTAssertNil(CommentAccent.resolve(author: "Ola", linkAuthor: "Ewa", parentAuthor: nil, currentUser: "Ja"))
        XCTAssertNil(CommentAccent.resolve(author: "", linkAuthor: "", parentAuthor: "", currentUser: ""))
    }
}

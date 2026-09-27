import XCTest
@testable import Podkop

/// Screen models must be released once their screen stops, including their loading and polling
/// tasks, so repeated navigation cannot keep old screens (and their content) alive.
@MainActor
final class LifetimeTests: XCTestCase {
    private func settle() async { for _ in 0..<50 { await Task.yield() } }

    private func assertReleased(_ make: () -> AnyObject, start: (AnyObject) -> Void, stop: (AnyObject) -> Void,
                                file: StaticString = #filePath, line: UInt = #line) async {
        weak var weakModel: AnyObject?
        do {
            let model = make()
            weakModel = model
            start(model)
            await settle()
            stop(model)
        }
        await settle()
        XCTAssertNil(weakModel, "model is still alive after its screen stopped", file: file, line: line)
    }

    func testFeedModelIsReleasedAfterStop() async {
        await assertReleased({ FeedModel(tab: .links, loggedIn: false, loader: FixtureFeedLoader(),
                                         updates: ResourceUpdates()) },
                             start: { ($0 as! FeedModel).start() }, stop: { ($0 as! FeedModel).stop() })
    }

    func testDetailModelIsReleasedAfterStop() async {
        await assertReleased({ DetailModel(kind: .link, id: 101, loader: FixtureDetailLoader(),
                                           mutator: FixtureDetailMutator(), updates: ResourceUpdates()) },
                             start: { ($0 as! DetailModel).start() }, stop: { ($0 as! DetailModel).stop() })
    }

    func testProfileModelIsReleasedAfterStop() async {
        await assertReleased({ ProfileModel(username: "Ewa", loader: FixtureProfileLoader(),
                                            updates: ResourceUpdates()) },
                             start: { ($0 as! ProfileModel).start() }, stop: { ($0 as! ProfileModel).stop() })
    }

    func testPollingConversationIsReleasedWhenHidden() async {
        await assertReleased({ ConversationModel(username: "Ewa", loader: FixtureMessagesLoader(),
                                                 media: FixtureComposerMedia()) },
                             start: { ($0 as! ConversationModel).becameVisible() },
                             stop: { ($0 as! ConversationModel).becameHidden() })
    }

    func testMoreModelIsReleasedWhileLoading() async {
        await assertReleased({ MoreModel(loader: FixtureProfileLoader()) },
                             start: { ($0 as! MoreModel).update(loggedIn: true, revision: 1) },
                             stop: { _ in })
    }
}

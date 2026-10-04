import XCTest
import PodkopShared
@testable import Podkop

@MainActor
final class NavigationTests: XCTestCase {
    func testEachTabKeepsItsPathAndWideSelection() {
        let router = AppRouter(storage: nil)
        router.navigate(.link(42), in: .links)
        router.navigate(.entry(7), in: .entries)
        XCTAssertEqual(router.detail(for: .links), .link(42))
        XCTAssertEqual(router.detail(for: .entries), .entry(7))
        XCTAssertEqual(router.selectedTab, .entries)
        router.replacePath([], for: .entries)
        XCTAssertNil(router.detail(for: .entries))
        XCTAssertEqual(router.detail(for: .links), .link(42))
    }

    func testRestoreRejectsInvalidDataAndRemovesAccountRoutes() {
        let router = AppRouter(storage: nil)
        router.isLoggedIn = true
        router.navigate(.messages, in: .more)
        router.navigate(.search, in: .more)
        let saved = router.snapshot()
        let restored = AppRouter(storage: nil)
        restored.restore(Data("garbage".utf8))
        XCTAssertEqual(restored.selectedTab, .links)
        restored.restore(saved!)
        XCTAssertEqual(restored.selectedTab, .more)
        XCTAssertEqual(restored.paths[.more], [.search])
    }

    func testSignOutClearsAccountRoutesAndPendingSheet() {
        let router = AppRouter(storage: nil)
        router.isLoggedIn = true
        router.navigate(.link(9), in: .links)
        router.navigate(.favorites, in: .more)
        router.sheet = .composer
        router.isLoggedIn = false
        XCTAssertEqual(router.paths[.links], [.link(9)])
        XCTAssertEqual(router.paths[.more], [])
        XCTAssertNil(router.sheet)
    }

    func testContentWaitsForReadinessWhileLoginIntentDoesNotBlock() {
        let router = AppRouter(storage: nil)
        let ingress = LinkIngress(router: router)
        let url = URL(string: "https://masslany.pl/wykop/link/21")!
        let first = Date(timeIntervalSince1970: 10)
        XCTAssertTrue(ingress.begin(url, now: first))
        XCTAssertFalse(ingress.begin(url, now: first))
        ingress.accept(kind: "link", id: 21)
        ingress.accept(kind: "login", id: nil)
        XCTAssertNil(router.detail(for: .links))
        ingress.end(url)
        ingress.ready = true
        XCTAssertEqual(router.detail(for: .links), .link(21))
        XCTAssertFalse(ingress.begin(url, now: first.addingTimeInterval(1)))
        XCTAssertTrue(ingress.begin(url, now: first.addingTimeInterval(4)))
    }

    func testSessionRevisionClearsAccountSelectionWithoutLoggingOut() {
        let router = AppRouter(storage: nil)
        router.applySession(isLoggedIn: true, revision: 0)
        router.navigate(.favorites, in: .more)
        router.navigate(.link(19), in: .links)
        router.applySession(isLoggedIn: true, revision: 1)
        XCTAssertEqual(router.paths[.more], [])
        XCTAssertEqual(router.paths[.links], [.link(19)])
    }

    func testComposerIntentSurvivesLoginAndClearsOnSignOut() {
        let router = AppRouter(storage: nil)
        let intent = ComposerIntent.createLinkComment(linkID: 31, parentCommentID: 9,
                                                     replyTarget: "author")
        router.presentComposer(intent)
        XCTAssertEqual(router.sheet, .login)
        XCTAssertEqual(router.pendingComposerIntent, intent)

        router.applySession(isLoggedIn: true, revision: 1)
        XCTAssertEqual(router.sheet, .composer)
        XCTAssertEqual(router.composerIntent, intent)
        XCTAssertNil(router.pendingComposerIntent)

        router.applySession(isLoggedIn: false, revision: 2)
        XCTAssertNil(router.sheet)
        XCTAssertNil(router.composerIntent)
    }

    func testRepeatedSceneRegistrationCountsOnceAndStopsAtLastScene() {
        let scenes = SceneActivity()
        let first = UUID()
        let second = UUID()
        scenes.set(first, active: true)
        scenes.set(first, active: true)
        scenes.set(second, active: true)
        XCTAssertEqual(scenes.activeCount, 2)
        scenes.set(first, active: false)
        XCTAssertTrue(scenes.isForeground)
        scenes.set(second, active: false)
        XCTAssertFalse(scenes.isForeground)
    }

    func testSharedParserRejectsUnsupportedURL() async throws {
        let client = PodkopClient.companion.create()
        let adapter = BridgeAdapter()
        defer { adapter.close(); client.close() }
        do {
            let _: IOSLinkIntent = try await adapter.call {
                client.session.acceptUrl(url: "http://masslany.pl/wykop/link/21", completion: $0)
            }
            XCTFail("expected validation failure")
        } catch let failure as BridgeFailure {
            XCTAssertEqual(failure.category, "validation")
        }
    }

    func testSharedParserRoutesContentLinksButNotLoginCallbacks() {
        let client = PodkopClient.companion.create()
        defer { client.close() }
        func route(_ url: String) -> AppRoute? {
            client.session.contentLink(url: url).flatMap {
                LinkIngress.route(kind: $0.kind, id: $0.id?.intValue, name: $0.name)
            }
        }
        XCTAssertEqual(route("https://wykop.pl/wpis/123/some-slug#456"), .entry(123))
        XCTAssertEqual(route("https://www.wykop.pl/link/456/some-slug/komentarz/789"), .link(456))
        XCTAssertEqual(route("https://wykop.pl/ludzie/some_user/wpisy"), .user("some_user"))
        XCTAssertEqual(route("https://wykop.pl/tag/Heheszki/najlepsze"), .tag("heheszki"))
        XCTAssertNil(route("https://wykop.pl/regulamin"))
        XCTAssertNil(route("https://example.com/wpis/1"))
        XCTAssertNil(route("https://masslany.pl/wykop/connect?token=abc&rtoken=def"))
    }

    func testIngressRoutesProfileAndTagIntents() {
        let router = AppRouter(storage: nil)
        let ingress = LinkIngress(router: router)
        ingress.ready = true
        ingress.accept(kind: "profile", id: nil, name: "some_user")
        ingress.accept(kind: "tag", id: nil, name: "heheszki")
        ingress.accept(kind: "profile", id: nil)
        XCTAssertEqual(router.paths[router.selectedTab], [.user("some_user"), .tag("heheszki")])
    }
}

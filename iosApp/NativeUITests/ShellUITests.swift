import XCTest

final class ShellUITests: XCTestCase {
    private func launch(_ fixture: String) -> XCUIApplication {
        let app = XCUIApplication()
        app.launchArguments = ["-nativeFixture", fixture]
        app.launch()
        return app
    }

    func testGuestTabsAndMoreGate() {
        let app = launch("guest")
        XCTAssertTrue(app.tabBars.buttons["Links"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.tabBars.buttons["Upcoming"].exists)
        XCTAssertTrue(app.tabBars.buttons["Entries"].exists)
        app.tabBars.buttons["More"].tap()
        XCTAssertTrue(app.buttons["Sign in"].exists)
        XCTAssertFalse(app.buttons["Sign out"].exists)
    }

    func testThreeFixtureFeedsKeepTheirControlsAndRows() {
        let app = launch("guest")
        app.tabBars.buttons["Links"].tap()
        XCTAssertTrue(app.descendants(matching: .any)["resource-link:101"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.buttons["feedSort"].exists)
        app.buttons["feedGallery"].tap()
        app.tabBars.buttons["Upcoming"].tap()
        XCTAssertTrue(app.descendants(matching: .any)["resource-link:101"].waitForExistence(timeout: 5))
        app.tabBars.buttons["Entries"].tap()
        XCTAssertTrue(app.descendants(matching: .any)["resource-entry:102"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.buttons["hotPeriod"].exists)
        app.tabBars.buttons["Links"].tap()
        XCTAssertEqual(app.buttons["feedGallery"].label, "List view")
    }

    func testRealFeedSmokeWhenRequested() throws {
        guard ProcessInfo.processInfo.environment["PODKOP_REAL_FEED"] == "1" else {
            throw XCTSkip("Run explicitly with a configured network and PODKOP_REAL_FEED=1")
        }
        let app = XCUIApplication()
        app.launch()
        XCTAssertTrue(app.tabBars.buttons["Links"].waitForExistence(timeout: 25))
        app.tabBars.buttons["Links"].tap()
        XCTAssertTrue(app.descendants(matching: .any).matching(
            NSPredicate(format: "identifier BEGINSWITH %@", "resource-link:")
        ).firstMatch.waitForExistence(timeout: 25))
        app.tabBars.buttons["Upcoming"].tap()
        XCTAssertTrue(app.descendants(matching: .any).matching(
            NSPredicate(format: "identifier BEGINSWITH %@", "resource-link:")
        ).firstMatch.waitForExistence(timeout: 25))
        app.tabBars.buttons["Entries"].tap()
        XCTAssertTrue(app.descendants(matching: .any).matching(
            NSPredicate(format: "identifier BEGINSWITH %@", "resource-entry:")
        ).firstMatch.waitForExistence(timeout: 25))
    }

    func testRetryAndQueuedLink() {
        let app = launch("retryLink")
        XCTAssertTrue(app.staticTexts["Could not start Podkop"].waitForExistence(timeout: 5))
        app.buttons["Retry"].tap()
        XCTAssertTrue(app.staticTexts["Link 21"].waitForExistence(timeout: 5))
    }

    func testMissingConfigurationKeepsRetryVisible() {
        let app = launch("missing")
        XCTAssertTrue(app.staticTexts["Configuration required"].waitForExistence(timeout: 5))
        app.buttons["Retry"].tap()
        XCTAssertTrue(app.staticTexts["Configuration required"].exists)
    }

    func testAuthenticatedMoreAndSignOut() {
        let app = launch("authenticated")
        app.tabBars.buttons["More"].tap()
        XCTAssertTrue(app.buttons["Profile"].exists)
        app.buttons["Sign out"].tap()
        XCTAssertTrue(app.buttons["Sign in"].waitForExistence(timeout: 5))
        XCTAssertFalse(app.buttons["Profile"].exists)
    }

    func testLoginSheetCanBeDismissed() {
        let app = launch("guest")
        app.tabBars.buttons["More"].tap()
        app.buttons["Sign in"].tap()
        XCTAssertTrue(app.staticTexts["Sign in to continue"].waitForExistence(timeout: 5))
        app.buttons["Cancel"].tap()
        XCTAssertFalse(app.staticTexts["Sign in to continue"].exists)
    }

    func testContentFixtureSpoilerAndAdultReveal() {
        let app = launch("content")
        XCTAssertTrue(app.staticTexts["Przykładowy link o długim tytule"].waitForExistence(timeout: 5))
        let spoiler = app.buttons["Show spoiler"]
        XCTAssertTrue(spoiler.exists)
        spoiler.tap()
        XCTAssertTrue(app.staticTexts["Ukryty tekst ze spoilerem."].exists)
        let adult = app.buttons["Show adult content"]
        app.swipeUp()
        XCTAssertTrue(adult.waitForExistence(timeout: 5))
        adult.tap()
        XCTAssertTrue(app.staticTexts["Treść tylko dla dorosłych z wieloma zdaniami."].exists)
    }
}

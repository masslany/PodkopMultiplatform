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

    func testFixtureLinkAndEntryDetailsShowCardsAndActions() {
        let app = launch("guest")
        app.tabBars.buttons["Links"].tap()
        XCTAssertTrue(app.buttons["Przykładowy link o długim tytule"].firstMatch.waitForExistence(timeout: 5))
        app.buttons["Przykładowy link o długim tytule"].firstMatch.tap()
        XCTAssertTrue(app.descendants(matching: .any)["detail-link-101"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.descendants(matching: .any)["resource-linkComment:104"].waitForExistence(timeout: 5))
        app.buttons["More actions"].firstMatch.tap()
        XCTAssertTrue(app.staticTexts["Actions"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.buttons["Show upvoters"].exists)
        app.buttons["Done"].tap()
        app.tabBars.buttons["Entries"].tap()
        XCTAssertTrue(app.buttons["Open entry"].waitForExistence(timeout: 5))
        app.buttons["Open entry"].tap()
        XCTAssertTrue(app.descendants(matching: .any)["detail-entry-102"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.descendants(matching: .any)["resource-entryComment:103"].waitForExistence(timeout: 5))
    }

    func testScreenshotPreviewCanExcludeParent() {
        let app = launch("guest")
        app.tabBars.buttons["Entries"].tap()
        XCTAssertTrue(app.buttons["Open entry"].waitForExistence(timeout: 5))
        app.buttons["Open entry"].tap()
        let comment = app.descendants(matching: .any)["resource-entryComment:103"]
        XCTAssertTrue(comment.waitForExistence(timeout: 5))
        comment.buttons["More actions"].tap()
        app.buttons["Share as screenshot"].tap()
        XCTAssertTrue(app.switches["Include parent"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.images["Screenshot preview"].waitForExistence(timeout: 5))
        app.switches["Include parent"].tap()
        XCTAssertTrue(app.images["Screenshot preview"].exists)
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

    func testRealDetailSmokeWhenRequested() throws {
        guard ProcessInfo.processInfo.environment["PODKOP_REAL_DETAIL"] == "1" else {
            throw XCTSkip("Run explicitly with a configured network and PODKOP_REAL_DETAIL=1")
        }
        let app = XCUIApplication()
        app.launch()
        XCTAssertTrue(app.tabBars.buttons["Links"].waitForExistence(timeout: 25))
        app.tabBars.buttons["Links"].tap()
        let firstCard = app.descendants(matching: .any).matching(
            NSPredicate(format: "identifier BEGINSWITH %@", "resource-link:")
        ).firstMatch
        XCTAssertTrue(firstCard.waitForExistence(timeout: 25))
        firstCard.buttons.element(boundBy: 1).tap()
        XCTAssertTrue(app.descendants(matching: .any).matching(
            NSPredicate(format: "identifier BEGINSWITH %@", "detail-link-")
        ).firstMatch.waitForExistence(timeout: 25))
        XCTAssertTrue(app.staticTexts["Comments"].waitForExistence(timeout: 25))
    }

    func testRetryAndQueuedLink() {
        let app = launch("retryLink")
        XCTAssertTrue(app.staticTexts["Could not start Podkop"].waitForExistence(timeout: 5))
        app.buttons["Retry"].tap()
        XCTAssertTrue(app.descendants(matching: .any)["detail-link-21"].waitForExistence(timeout: 5))
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

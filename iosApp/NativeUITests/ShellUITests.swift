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

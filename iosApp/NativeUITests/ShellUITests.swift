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
        XCTAssertTrue(app.buttons["imageSave"].exists)
        XCTAssertTrue(app.buttons["imageCopy"].exists)
        XCTAssertTrue(app.buttons["imageShare"].exists)
        app.switches["Include parent"].tap()
        XCTAssertTrue(app.images["Screenshot preview"].exists)
    }

    func testFixtureComposerKeepsDraftWhenDiscardCancelled() {
        let app = launch("authenticated")
        app.buttons["Write a post"].tap()
        let editor = app.textViews.firstMatch
        XCTAssertTrue(editor.waitForExistence(timeout: 5))
        editor.tap()
        editor.typeText("Hello 👩🏽‍💻")
        app.buttons["Cancel"].tap()
        XCTAssertTrue(app.buttons["Keep writing"].waitForExistence(timeout: 5))
        app.buttons["Keep writing"].tap()
        XCTAssertTrue(editor.exists)
        XCTAssertTrue(app.buttons["composerSubmit"].isEnabled)
    }

    func testFixtureLinkSubmissionOpensDraftForm() {
        let app = launch("authenticated")
        app.tabBars.buttons["More"].tap()
        app.buttons["Add link"].tap()
        XCTAssertTrue(app.textFields["https://example.com/article"].waitForExistence(timeout: 5))
        app.textFields["https://example.com/article"].tap()
        app.textFields["https://example.com/article"].typeText("https://example.com")
        app.buttons["Continue"].tap()
        XCTAssertTrue(app.textFields["Title"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.buttons["Publish"].exists)
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
        app.buttons["Settings"].tap()
        app.buttons["settingsSignOut"].tap()
        XCTAssertTrue(app.alerts.buttons["Sign out"].waitForExistence(timeout: 5))
        app.alerts.buttons["Sign out"].tap()
        XCTAssertTrue(app.buttons["settingsSignOut"].waitForNonExistence(timeout: 5))
        app.navigationBars.buttons.firstMatch.tap()
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
        // The gallery height varies with text layout, so scroll until the card is reachable.
        for _ in 0..<4 where !adult.isHittable { app.swipeUp() }
        XCTAssertTrue(adult.waitForExistence(timeout: 5))
        adult.tap()
        XCTAssertTrue(app.staticTexts["Treść tylko dla dorosłych z wieloma zdaniami."].exists)
    }

    func testFixtureSearchSuggestionsOpenAdvancedSearch() {
        let app = launch("authenticated")
        XCTAssertTrue(app.buttons["Search"].waitForExistence(timeout: 5))
        app.buttons["Search"].firstMatch.tap()
        let field = app.searchFields.firstMatch
        XCTAssertTrue(field.waitForExistence(timeout: 5))
        field.tap()
        field.typeText("tech")
        XCTAssertTrue(app.staticTexts["#technologia"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.staticTexts["Ewa-Żółw"].exists)
        app.buttons["advancedSearch"].tap()
        let query = app.textFields["advancedQuery"]
        XCTAssertTrue(query.waitForExistence(timeout: 5))
        XCTAssertEqual(query.value as? String, "tech")
        app.buttons["advancedSubmit"].tap()
        XCTAssertTrue(app.descendants(matching: .any)["resource-link:101"].waitForExistence(timeout: 5))
    }

    func testFixtureHitsRankAndAccountCollections() {
        let app = launch("authenticated")
        app.tabBars.buttons["More"].tap()
        app.buttons["Hits"].tap()
        XCTAssertTrue(app.descendants(matching: .any)["resource-link:101"].waitForExistence(timeout: 5))
        app.buttons["hitsArchive"].tap()
        XCTAssertTrue(app.buttons["Show"].waitForExistence(timeout: 5))
        app.buttons["Cancel"].tap()
        app.navigationBars.buttons.firstMatch.tap()
        app.buttons["Rank"].tap()
        XCTAssertTrue(app.staticTexts["Ewa-Żółw"].waitForExistence(timeout: 5))
        app.navigationBars.buttons.firstMatch.tap()
        app.buttons["Favorites"].tap()
        XCTAssertTrue(app.descendants(matching: .any)["resource-entry:102"].waitForExistence(timeout: 5))
        app.navigationBars.buttons.firstMatch.tap()
        app.buttons["Observed"].tap()
        XCTAssertTrue(app.descendants(matching: .any)["resource-link:101"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.staticTexts["3 new comments on an observed link"].exists)
    }

    func testFixtureTagFromSearchObservesAndShowsGallery() {
        let app = launch("authenticated")
        app.buttons["Search"].firstMatch.tap()
        let field = app.searchFields.firstMatch
        XCTAssertTrue(field.waitForExistence(timeout: 5))
        field.tap()
        field.typeText("tech")
        XCTAssertTrue(app.staticTexts["#technologia"].waitForExistence(timeout: 5))
        app.staticTexts["#technologia"].tap()
        XCTAssertTrue(app.buttons["tagObserve"].waitForExistence(timeout: 5))
        XCTAssertFalse(app.buttons["tagNotifications"].exists)
        app.buttons["tagObserve"].tap()
        XCTAssertTrue(app.buttons["tagNotifications"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.descendants(matching: .any)["resource-entry:102"].waitForExistence(timeout: 5))
        app.buttons["tagGallery"].tap()
        XCTAssertFalse(app.descendants(matching: .any)["resource-entry:102"].exists)
    }

    func testFixtureProfileFromRankShowsDetailsAndSections() {
        let app = launch("authenticated")
        app.tabBars.buttons["More"].tap()
        app.buttons["Rank"].tap()
        XCTAssertTrue(app.staticTexts["Ewa-Żółw"].waitForExistence(timeout: 5))
        app.staticTexts["Ewa-Żółw"].tap()
        XCTAssertTrue(app.buttons["profileObserve"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.descendants(matching: .any)["resource-entry:102"].waitForExistence(timeout: 5))
        app.buttons["profileDetails"].tap()
        XCTAssertTrue(app.staticTexts["Weteran"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.textFields["profileNote"].exists || app.textViews["profileNote"].exists)
        app.buttons["profileSummary-following"].tap()
        XCTAssertTrue(app.staticTexts["#technologia"].waitForExistence(timeout: 5))
    }

    func testFixtureOwnProfileHasNoViewerActions() {
        let app = launch("authenticated")
        app.tabBars.buttons["More"].tap()
        app.buttons["Profile"].tap()
        XCTAssertTrue(app.buttons["profileDetails"].waitForExistence(timeout: 5))
        XCTAssertFalse(app.buttons["profileObserve"].exists)
        XCTAssertFalse(app.buttons["profileBlacklist"].exists)
    }

    func testFixtureBlacklistAddAndConfirmedRemove() {
        let app = launch("authenticated")
        app.tabBars.buttons["More"].tap()
        app.buttons["Settings"].tap()
        app.buttons["Manage blacklists"].tap()
        XCTAssertTrue(app.buttons["spamer"].waitForExistence(timeout: 5))
        app.segmentedControls["blacklistCategory"].buttons.element(boundBy: 1).tap()
        XCTAssertTrue(app.buttons["#polityka"].waitForExistence(timeout: 5))
        let input = app.textFields["blacklistInput"]
        input.tap()
        input.typeText("#Sport")
        app.buttons["blacklistAdd"].tap()
        XCTAssertTrue(app.buttons["#sport"].waitForExistence(timeout: 5))
        app.buttons["Remove #polityka"].tap()
        XCTAssertTrue(app.alerts.buttons["Remove"].waitForExistence(timeout: 5))
        app.alerts.buttons["Remove"].tap()
        XCTAssertTrue(app.buttons["#polityka"].waitForNonExistence(timeout: 5))
        XCTAssertTrue(app.buttons["#sport"].exists)
    }

    func testFixtureNotificationsGroupsAndTargets() {
        let app = launch("authenticated")
        XCTAssertTrue(app.buttons["toolbarNotifications"].waitForExistence(timeout: 5))
        app.buttons["toolbarNotifications"].tap()
        XCTAssertTrue(app.buttons["notificationsMarkAll"].waitForExistence(timeout: 5))
        app.buttons["notificationGroup-tags"].tap()
        let grouped = app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "#technologia")).firstMatch
        XCTAssertTrue(grouped.waitForExistence(timeout: 5))
        grouped.tap()
        XCTAssertTrue(app.buttons["tagObserve"].waitForExistence(timeout: 5))
        app.navigationBars.buttons.firstMatch.tap()
        app.buttons["notificationGroup-pm"].tap()
        XCTAssertFalse(app.buttons["notificationsMarkAll"].exists)
        let pm = app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Ewa-Żółw")).firstMatch
        XCTAssertTrue(pm.waitForExistence(timeout: 5))
        pm.tap()
        XCTAssertTrue(app.textFields["messageInput"].waitForExistence(timeout: 5))
    }

    func testFixtureInboxConversationSendAndNewConversation() {
        let app = launch("authenticated")
        app.tabBars.buttons["More"].tap()
        app.buttons["Messages"].tap()
        let conversation = app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Ewa-Żółw")).firstMatch
        XCTAssertTrue(conversation.waitForExistence(timeout: 5))
        conversation.tap()
        XCTAssertTrue(app.descendants(matching: .any)["message-m1"].waitForExistence(timeout: 5))
        XCTAssertFalse(app.buttons["messageSend"].isEnabled)
        let input = app.textFields["messageInput"]
        input.tap()
        input.typeText("Nowa wiadomość")
        app.buttons["messageSend"].tap()
        XCTAssertTrue(app.descendants(matching: .any)["message-sent-0"].waitForExistence(timeout: 5))
        XCTAssertNotEqual(input.value as? String, "Nowa wiadomość", "a sent message clears the input")
        app.navigationBars.buttons.firstMatch.tap()
        app.buttons["newConversation"].tap()
        let username = app.textFields["newConversationUsername"]
        XCTAssertTrue(username.waitForExistence(timeout: 5))
        username.tap()
        username.typeText("ewa")
        XCTAssertTrue(app.staticTexts["Ewa-Żółw"].waitForExistence(timeout: 5))
        app.staticTexts["Ewa-Żółw"].tap()
        XCTAssertTrue(app.textFields["messageInput"].waitForExistence(timeout: 5))
        app.navigationBars.buttons.firstMatch.tap()
        XCTAssertTrue(app.buttons["newConversation"].waitForExistence(timeout: 5), "new conversation was replaced")
    }

    func testFixtureDirtyConversationAsksBeforeLeaving() {
        let app = launch("authenticated")
        app.tabBars.buttons["More"].tap()
        app.buttons["Messages"].tap()
        let conversation = app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Ewa-Żółw")).firstMatch
        XCTAssertTrue(conversation.waitForExistence(timeout: 5))
        conversation.tap()
        let input = app.textFields["messageInput"]
        XCTAssertTrue(input.waitForExistence(timeout: 5))
        input.tap()
        input.typeText("szkic")
        app.navigationBars.buttons["Back"].tap()
        XCTAssertTrue(app.buttons["Keep writing"].waitForExistence(timeout: 5))
        app.buttons["Keep writing"].tap()
        XCTAssertEqual(input.value as? String, "szkic")
    }

    func testFixtureProfileOpensConversation() {
        let app = launch("authenticated")
        app.tabBars.buttons["More"].tap()
        app.buttons["Rank"].tap()
        XCTAssertTrue(app.staticTexts["Ewa-Żółw"].waitForExistence(timeout: 5))
        app.staticTexts["Ewa-Żółw"].tap()
        XCTAssertTrue(app.buttons["profileMessage"].waitForExistence(timeout: 5))
        app.buttons["profileMessage"].tap()
        XCTAssertTrue(app.textFields["messageInput"].waitForExistence(timeout: 5))
    }

    func testFixtureSettingsThemeCacheAndAbout() {
        let app = launch("guest")
        app.tabBars.buttons["More"].tap()
        app.buttons["Settings"].tap()
        XCTAssertFalse(app.buttons["settingsSignOut"].exists, "account actions need a session")
        app.segmentedControls["settingsTheme"].buttons["Dark"].tap()
        XCTAssertTrue(app.segmentedControls["settingsTheme"].buttons["Dark"].isSelected)
        app.buttons["settingsClearCache"].tap()
        XCTAssertTrue(app.staticTexts["Cache cleared"].waitForExistence(timeout: 5))
        app.buttons["Dismiss"].tap()
        app.buttons["About"].tap()
        XCTAssertTrue(app.staticTexts["ktor-client-core"].waitForExistence(timeout: 5))
    }
}

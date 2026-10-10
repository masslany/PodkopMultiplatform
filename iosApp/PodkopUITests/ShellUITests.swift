import XCTest

final class ShellUITests: XCTestCase {
    private func launch(_ fixture: String, _ arguments: String...) -> XCUIApplication {
        let app = XCUIApplication()
        app.launchArguments = ["-uiFixture", fixture] + arguments
        app.launch()
        return app
    }

    /// Entry cards open on a tap anywhere outside their controls, as on Android.
    private func openEntry(_ app: XCUIApplication) {
        let card = app.descendants(matching: .any)["resource-entry:102"]
        XCTAssertTrue(card.waitForExistence(timeout: 5))
        card.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).tap()
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
        // SwiftUI menus can be exposed as either buttons or pop-up buttons.
        XCTAssertTrue(app.descendants(matching: .any)["feedSort"].exists)
        // CI runs on an iPhone: two gallery columns cannot fit in this layout.
        XCTAssertFalse(app.buttons["feedGallery"].exists)
        app.tabBars.buttons["Upcoming"].tap()
        XCTAssertTrue(app.descendants(matching: .any)["resource-link:101"].waitForExistence(timeout: 5))
        app.tabBars.buttons["Entries"].tap()
        XCTAssertTrue(app.descendants(matching: .any)["resource-entry:102"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.descendants(matching: .any)["hotPeriod"].exists)
        app.tabBars.buttons["Links"].tap()
        XCTAssertFalse(app.buttons["feedGallery"].exists)
    }

    func testFixtureLinkAndEntryDetailsShowCardsAndActions() {
        let app = launch("guest")
        app.tabBars.buttons["Links"].tap()
        XCTAssertTrue(app.staticTexts["Przykładowy link o długim tytule"].firstMatch.waitForExistence(timeout: 5))
        app.staticTexts["Przykładowy link o długim tytule"].firstMatch.tap()
        XCTAssertTrue(app.descendants(matching: .any)["detail-link-101"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.descendants(matching: .any)["resource-linkComment:104"].waitForExistence(timeout: 5))
        app.buttons["More actions"].firstMatch.tap()
        XCTAssertTrue(app.buttons["Show diggers"].waitForExistence(timeout: 5))
        // The sheet fits its rows, so tapping the dimmed page above it closes it.
        app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.12)).tap()
        XCTAssertTrue(app.buttons["Show diggers"].waitForNonExistence(timeout: 5))
        app.tabBars.buttons["Entries"].tap()
        openEntry(app)
        XCTAssertTrue(app.descendants(matching: .any)["detail-entry-102"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.descendants(matching: .any)["resource-entryComment:103"].waitForExistence(timeout: 5))
    }

    func testFixtureBuryMenuAsksForAReason() {
        let app = launch("authenticated")
        app.tabBars.buttons["Links"].tap()
        XCTAssertTrue(app.staticTexts["Przykładowy link o długim tytule"].firstMatch.waitForExistence(timeout: 5))
        app.staticTexts["Przykładowy link o długim tytule"].firstMatch.tap()
        XCTAssertTrue(app.descendants(matching: .any)["detail-link-101"].waitForExistence(timeout: 5))
        let bury = app.buttons["Downvote"].firstMatch
        XCTAssertTrue(bury.waitForExistence(timeout: 5))
        bury.tap()
        let reason = app.buttons["duplicate"]
        XCTAssertTrue(reason.waitForExistence(timeout: 5), "the reasons open as a menu on the bury button")
        XCTAssertTrue(app.buttons["spam"].exists)
        reason.tap()
        XCTAssertTrue(app.buttons["Remove downvote"].waitForExistence(timeout: 5))
    }

    func testScreenshotPreviewCanExcludeParent() {
        let app = launch("guest")
        app.tabBars.buttons["Entries"].tap()
        openEntry(app)
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
        XCTAssertFalse(app.buttons["toolbarAddEntry"].exists)
        app.tabBars.buttons["Entries"].tap()
        let addEntry = app.buttons["toolbarAddEntry"]
        XCTAssertTrue(addEntry.waitForExistence(timeout: 5))
        addEntry.tap()
        let editor = app.textViews.firstMatch
        XCTAssertTrue(editor.waitForExistence(timeout: 5))
        editor.tap()
        let draft = "Hello 👩🏽‍💻"
        editor.typeText(draft)
        XCTAssertEqual(editor.value as? String, draft)
        app.buttons["Cancel"].tap()
        let keepWriting = app.alerts.buttons["Keep writing"]
        XCTAssertTrue(keepWriting.waitForExistence(timeout: 5))
        keepWriting.tap()
        XCTAssertTrue(editor.exists)
        XCTAssertEqual(editor.value as? String, draft)
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
        XCTAssertTrue(app.buttons["Profile"].waitForExistence(timeout: 5))
        app.buttons["Settings"].tap()
        // Like Android, the private-message notifications switch is for signed-in users only.
        XCTAssertTrue(app.switches["settingsMessageNotifications"].waitForExistence(timeout: 5))
        // Sign-out ends the list, below the fold on shorter phones.
        let signOut = app.buttons["settingsSignOut"]
        for _ in 0..<4 where !signOut.isHittable { app.swipeUp() }
        signOut.tap()
        XCTAssertTrue(app.alerts.buttons["Sign out"].waitForExistence(timeout: 5))
        app.alerts.buttons["Sign out"].tap()
        XCTAssertTrue(app.buttons["settingsSignOut"].waitForNonExistence(timeout: 5))
        XCTAssertFalse(app.switches["settingsMessageNotifications"].exists)
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
        // Stands in for Wykop's "+18" account setting, without which nothing can be revealed.
        let app = launch("content", "-adultContentAllowed")
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
        // Like Android, a blacklisted author's body stays hidden until the reader asks.
        let blacklisted = app.buttons["showBlacklistedContent"]
        for _ in 0..<4 where !blacklisted.isHittable { app.swipeUp() }
        XCTAssertTrue(blacklisted.waitForExistence(timeout: 5))
        XCTAssertFalse(app.staticTexts["Wpis od autora z czarnej listy."].exists)
        blacklisted.tap()
        XCTAssertTrue(app.staticTexts["Wpis od autora z czarnej listy."].waitForExistence(timeout: 5))
    }

    func testContentTermsMustBeAcceptedBeforeContent() {
        let app = launch("guest", "-contentTerms")
        let accept = app.buttons["acceptContentTerms"]
        XCTAssertTrue(accept.waitForExistence(timeout: 5))
        XCTAssertFalse(app.tabBars.buttons["More"].exists)
        accept.tap()
        XCTAssertTrue(app.tabBars.buttons["More"].waitForExistence(timeout: 5))
    }

    func testAdultContentStaysHiddenWithoutAccountSetting() {
        let app = launch("content")
        XCTAssertTrue(app.staticTexts["Przykładowy link o długim tytule"].waitForExistence(timeout: 5))
        let notice = app.descendants(matching: .any)["adultContentTurnedOff"].firstMatch
        for _ in 0..<4 where !notice.exists { app.swipeUp() }
        XCTAssertTrue(notice.waitForExistence(timeout: 5))
        XCTAssertFalse(app.buttons["Show adult content"].exists)
        XCTAssertFalse(app.staticTexts["Treść tylko dla dorosłych z wieloma zdaniami."].exists)
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
        app.buttons["more-observed"].tap()
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
        // Like Android, long-pressing an image offers copying and saving it.
        let photo = app.descendants(matching: .any)["galleryItem-107"]
        XCTAssertTrue(photo.waitForExistence(timeout: 5))
        photo.press(forDuration: 1.2)
        XCTAssertTrue(app.buttons["Copy image"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.buttons["Save"].exists)
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
        XCTAssertTrue(app.buttons["Weteran"].waitForExistence(timeout: 5))
        app.buttons["Weteran"].tap()
        XCTAssertTrue(app.staticTexts["10 lat na Wykopie"].waitForExistence(timeout: 5))
        // The details popover closes on a tap outside it.
        app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.95)).tap()
        XCTAssertTrue(app.staticTexts["10 lat na Wykopie"].waitForNonExistence(timeout: 5))
        XCTAssertTrue(app.textFields["profileNote"].exists || app.textViews["profileNote"].exists)
        app.buttons["profileSummary-following"].tap()
        XCTAssertTrue(app.staticTexts["#technologia"].waitForExistence(timeout: 5))
    }

    func testFixtureOwnProfileHasNoViewerActions() {
        let app = launch("authenticated")
        app.tabBars.buttons["More"].tap()
        XCTAssertTrue(app.buttons["Profile"].waitForExistence(timeout: 5))
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
        XCTAssertTrue(app.textViews["messageInput"].waitForExistence(timeout: 5))
    }

    func testFixtureNotificationGroupExpandsAndLoadsMore() {
        let app = launch("authenticated")
        XCTAssertTrue(app.buttons["toolbarNotifications"].waitForExistence(timeout: 5))
        app.buttons["toolbarNotifications"].tap()
        app.buttons["notificationGroup-tags"].tap()
        let expand = app.buttons["notificationGroupExpand-group:g1"]
        XCTAssertTrue(expand.waitForExistence(timeout: 5))
        expand.tap()
        XCTAssertTrue(app.buttons["notificationGroupMember-g1-2"].waitForExistence(timeout: 5))
        XCTAssertFalse(app.buttons["notificationGroupMember-g1-3"].exists)
        // wykop.pl stops at a group's first page; the app loads the rest on request.
        app.buttons["notificationGroupShowMore-group:g1"].tap()
        XCTAssertTrue(app.buttons["notificationGroupMember-g1-3"].waitForExistence(timeout: 5))
        XCTAssertFalse(app.buttons["notificationGroupShowMore-group:g1"].exists)
        app.buttons["notificationGroupMember-g1-1"].tap()
        XCTAssertTrue(app.descendants(matching: .any)["detail-entry-102"].waitForExistence(timeout: 5))
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
        let input = app.textViews["messageInput"]
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
        XCTAssertTrue(app.textViews["messageInput"].waitForExistence(timeout: 5))
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
        let input = app.textViews["messageInput"]
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
        XCTAssertTrue(app.textViews["messageInput"].waitForExistence(timeout: 5))
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

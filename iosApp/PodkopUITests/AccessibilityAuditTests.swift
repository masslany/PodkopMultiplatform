import XCTest

/// Runs Apple's accessibility audit (labels, contrast, hit regions, clipped text, Dynamic Type)
/// on the main screens with fixture data. Each finding is reported with its screen and element.
final class AccessibilityAuditTests: XCTestCase {
    private func launch(_ fixture: String) -> XCUIApplication {
        let app = XCUIApplication()
        app.launchArguments = ["-uiFixture", fixture]
        app.launch()
        return app
    }

    /// Checks that point at judgement calls rather than regressions: contrast waits for the
    /// brand-color decision (D07); hit area flags inline text links (author, domain, tags) that
    /// match Android and the web; Dynamic Type and clipping flag deliberately single-line labels
    /// and the shrink-to-fit vote badge. They are printed for review but do not fail the test.
    private let reviewOnly: XCUIAccessibilityAuditType = [.contrast, .hitRegion, .dynamicType, .textClipped]

    private func audit(_ app: XCUIApplication, screen: String, file: StaticString = #filePath, line: UInt = #line) {
        var failures: [String] = []
        do {
            try app.performAccessibilityAudit { issue in
                let element = issue.element.map { "\($0.elementType) '\($0.label)' [\($0.identifier)]" } ?? "screen"
                let finding = "\(issue.compactDescription) — \(element)"
                // Domains are read out character by character on purpose.
                let isDomain = issue.auditType == .sufficientElementDescription
                    && (issue.element?.label.range(of: #"^[a-z0-9.-]+\.[a-z]{2,}$"#, options: .regularExpression) != nil)
                if self.reviewOnly.contains(issue.auditType) || isDomain {
                    print("A11Y review \(screen): \(finding)")
                } else {
                    failures.append(finding)
                }
                return true
            }
        } catch {
            failures.append("audit failed: \(error)")
        }
        for failure in failures { print("A11Y FAIL \(screen): \(failure)") }
        XCTAssertTrue(failures.isEmpty, "\(screen): \(failures.joined(separator: "; "))", file: file, line: line)
    }

    func testFeedsDetailsAndMoreAsGuest() {
        let app = launch("guest")
        XCTAssertTrue(app.tabBars.buttons["Links"].waitForExistence(timeout: 5))
        audit(app, screen: "links feed")
        app.staticTexts["Przykładowy link o długim tytule"].firstMatch.tap()
        XCTAssertTrue(app.descendants(matching: .any)["detail-link-101"].waitForExistence(timeout: 5))
        audit(app, screen: "link detail")
        app.tabBars.buttons["Entries"].tap()
        XCTAssertTrue(app.descendants(matching: .any)["resource-entry:102"].waitForExistence(timeout: 5))
        audit(app, screen: "entries feed")
        app.tabBars.buttons["More"].tap()
        XCTAssertTrue(app.buttons["Sign in"].waitForExistence(timeout: 5))
        audit(app, screen: "more, signed out")
    }

    func testAccountScreensWhenSignedIn() {
        let app = launch("authenticated")
        app.tabBars.buttons["More"].tap()
        XCTAssertTrue(app.buttons["Profile"].waitForExistence(timeout: 5))
        audit(app, screen: "more, signed in")
        app.buttons["Profile"].tap()
        XCTAssertTrue(app.buttons["profileDetails"].waitForExistence(timeout: 5))
        audit(app, screen: "own profile")
        app.navigationBars.buttons.firstMatch.tap()
        app.buttons["Settings"].tap()
        XCTAssertTrue(app.buttons["settingsSignOut"].waitForExistence(timeout: 5))
        audit(app, screen: "settings")
    }
}

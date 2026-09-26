import XCTest
@testable import Podkop

@MainActor
private final class ControlledSettings: SettingsServicing {
    let saves = Pending<Void>()
    func setAutoplayGifs(_ enabled: Bool) async throws { try await saves.wait("autoplay:\(enabled)") }
    func setTheme(_ theme: ThemeChoice) async throws { try await saves.wait("theme:\(theme.rawValue)") }
    func clearMediaCache() {}
    func libraries() -> [LibraryNotice] { [] }
}

@MainActor
private final class StoredSettings: SettingsState {
    var theme: ThemeChoice = .auto
    var autoplayGifs = true
}

@MainActor
final class SettingsTests: XCTestCase {
    func testThemeChangesBeforeTheSaveFinishes() async throws {
        let service = ControlledSettings()
        let state = StoredSettings()
        let model = SettingsModel(service: service, state: state)

        model.setTheme(.dark)

        XCTAssertEqual(state.theme, .dark, "the picker must not snap back while the choice is saved")
        try await waitUntil("theme save") { service.saves.calls.count == 1 }
        XCTAssertEqual(service.saves.calls[0].input, "theme:\(ThemeChoice.dark.rawValue)")
        service.saves.succeed(0, ())
        try await Task.sleep(for: .milliseconds(20))
        XCTAssertEqual(state.theme, .dark)
        XCTAssertFalse(model.failed)
    }

    func testFailedSaveRestoresThePreviousValue() async throws {
        let service = ControlledSettings()
        let state = StoredSettings()
        let model = SettingsModel(service: service, state: state)

        model.setAutoplay(false)
        XCTAssertFalse(state.autoplayGifs)
        try await waitUntil("autoplay save") { service.saves.calls.count == 1 }
        service.saves.fail(0)

        try await waitUntil("rollback") { state.autoplayGifs }
        XCTAssertTrue(model.failed)
    }

    func testFailedSaveKeepsALaterChoice() async throws {
        let service = ControlledSettings()
        let state = StoredSettings()
        let model = SettingsModel(service: service, state: state)

        model.setTheme(.dark)
        model.setTheme(.light)
        try await waitUntil("both saves") { service.saves.calls.count == 2 }
        service.saves.fail(0)

        try await waitUntil("failure") { model.failed }
        XCTAssertEqual(state.theme, .light, "an older failed save must not undo a newer choice")
        service.saves.succeed(1, ())
    }

    func testChoosingTheCurrentValueSavesNothing() async throws {
        let service = ControlledSettings()
        let model = SettingsModel(service: service, state: StoredSettings())

        model.setTheme(.auto)

        try await Task.sleep(for: .milliseconds(50))
        XCTAssertTrue(service.saves.calls.isEmpty)
    }
}

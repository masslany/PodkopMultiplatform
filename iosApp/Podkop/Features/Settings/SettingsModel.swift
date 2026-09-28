import SwiftUI
import Observation
import PodkopShared

/// The settings the app reads while they are being saved; `SessionModel` holds them.
@MainActor protocol SettingsState: AnyObject {
    var theme: ThemeChoice { get set }
    var autoplayGifs: Bool { get set }
    var playVideosInline: Bool { get set }
}

@MainActor @Observable
final class SettingsModel {
    private(set) var failed = false
    private(set) var confirmation: String?
    private let service: SettingsServicing
    private let state: SettingsState

    init(service: SettingsServicing, state: SettingsState) {
        self.service = service
        self.state = state
    }

    /// Controls bound to these values must see the change at once, or they snap back while the
    /// shared layer saves it. The previous value returns if the save fails.
    func setAutoplay(_ enabled: Bool) {
        update(\.autoplayGifs, to: enabled) { try await $0.setAutoplayGifs(enabled) }
    }

    func setPlayVideosInline(_ enabled: Bool) {
        update(\.playVideosInline, to: enabled) { try await $0.setPlayVideosInline(enabled) }
    }

    func setTheme(_ theme: ThemeChoice) {
        update(\.theme, to: theme) { try await $0.setTheme(theme) }
    }

    func clearCache() {
        service.clearMediaCache()
        confirmation = String(localized: .settingsCacheCleared)
    }

    /// Same fields as Android's diagnostics snapshot; no identifiers or tokens.
    func copyDiagnostics(session: SessionModel) {
        UIPasteboard.general.string = [
            "app=Podkop",
            "platform=iOS \(UIDevice.current.systemVersion)",
            "version=\(AppBuild.version)",
            "buildType=\(AppBuild.isDebug ? "debug" : "release")",
            "loggedIn=\(session.isLoggedIn)",
            "themeOverride=\(session.theme.rawValue)",
            "autoplayGifs=\(session.autoplayGifs)",
            "playVideosInline=\(session.playVideosInline)",
        ].joined(separator: "\n")
        confirmation = String(localized: .settingsDiagnosticsCopied)
    }

    func dismissMessages() {
        failed = false
        confirmation = nil
    }

    private func update<Value: Equatable>(_ keyPath: ReferenceWritableKeyPath<SettingsState, Value>, to value: Value,
                                          save: @escaping (SettingsServicing) async throws -> Void) {
        let state = state
        let previous = state[keyPath: keyPath]
        guard value != previous else { return }
        state[keyPath: keyPath] = value
        let service = service
        Task { [weak self] in
            do {
                try await save(service)
            } catch {
                // A later choice may already have replaced this one; only undo our own.
                if state[keyPath: keyPath] == value { state[keyPath: keyPath] = previous }
                self?.failed = true
            }
        }
    }
}

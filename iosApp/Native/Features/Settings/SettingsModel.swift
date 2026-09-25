import SwiftUI
import Observation
import PodkopShared

@MainActor @Observable
final class SettingsModel {
    private(set) var failed = false
    private(set) var confirmation: String?
    private let service: SettingsServicing

    init(service: SettingsServicing) { self.service = service }

    func setAutoplay(_ enabled: Bool) { run { try await $0.setAutoplayGifs(enabled) } }
    func setTheme(_ theme: ThemeChoice) { run { try await $0.setTheme(theme) } }

    func clearCache() {
        service.clearMediaCache()
        confirmation = String(localized: "Cache cleared")
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
        ].joined(separator: "\n")
        confirmation = String(localized: "Diagnostics copied")
    }

    func dismissMessages() {
        failed = false
        confirmation = nil
    }

    private func run(_ action: @escaping (SettingsServicing) async throws -> Void) {
        let service = service
        Task { [weak self] in
            do { try await action(service) } catch { self?.failed = true }
        }
    }
}

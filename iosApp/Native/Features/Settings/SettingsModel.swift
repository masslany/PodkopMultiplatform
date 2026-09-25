import SwiftUI
import Observation
import PodkopShared

/// Mirrors the shared `ThemeOverride` names.
enum ThemeChoice: String, CaseIterable {
    case auto = "AUTO", light = "LIGHT", dark = "DARK"

    var colorScheme: ColorScheme? {
        switch self {
        case .auto: nil
        case .light: .light
        case .dark: .dark
        }
    }
}

struct NativeLibraryNotice: Identifiable, Equatable {
    let name: String
    let artifact: String
    let licenseName: String?
    let licenseURL: URL?
    let projectURL: URL?
    var id: String { artifact }
}

@MainActor protocol SettingsServicing {
    func setAutoplayGifs(_ enabled: Bool) async throws
    func setTheme(_ theme: ThemeChoice) async throws
    /// Removes downloaded media only; the session and settings stay intact.
    func clearMediaCache()
    func libraries() -> [NativeLibraryNotice]
}

@MainActor
final class SharedSettingsService: SettingsServicing {
    private let client: PodkopClient
    private let adapter: BridgeAdapter
    private let media: SharedMediaLoader?

    init(client: PodkopClient, adapter: BridgeAdapter, media: SharedMediaLoader?) {
        self.client = client
        self.adapter = adapter
        self.media = media
    }

    func setAutoplayGifs(_ enabled: Bool) async throws {
        let _: IOSSuccess = try await adapter.call { self.client.settings.setAutoplayGifs(enabled: enabled, completion: $0) }
    }

    func setTheme(_ theme: ThemeChoice) async throws {
        let _: IOSSuccess = try await adapter.call { self.client.settings.setTheme(name: theme.rawValue, completion: $0) }
    }

    func clearMediaCache() {
        client.mediaBytes.clearCache()
        media?.clearMemory()
        NativeImageDecoder.shared.clear()
    }

    func libraries() -> [NativeLibraryNotice] {
        client.about.libraries().map {
            NativeLibraryNotice(name: $0.name, artifact: $0.artifact, licenseName: $0.licenseName,
                                licenseURL: $0.licenseUrl.flatMap(URL.init(string:)),
                                projectURL: $0.projectUrl.flatMap(URL.init(string:)))
        }
    }
}

#if DEBUG
/// Fixture runs have no settings observation, so changes are applied to the session directly.
@MainActor
final class FixtureSettingsService: SettingsServicing {
    private unowned let session: SessionModel
    init(session: SessionModel) { self.session = session }
    func setAutoplayGifs(_ enabled: Bool) async throws { session.autoplayGifs = enabled }
    func setTheme(_ theme: ThemeChoice) async throws { session.theme = theme }
    func clearMediaCache() { NativeImageDecoder.shared.clear() }
    func libraries() -> [NativeLibraryNotice] {
        [NativeLibraryNotice(name: "ktor-client-core", artifact: "io.ktor:ktor-client-core",
                             licenseName: "Apache-2.0", licenseURL: URL(string: "https://www.apache.org/licenses/LICENSE-2.0"),
                             projectURL: URL(string: "https://github.com/ktorio/ktor"))]
    }
}
#endif

enum AppBuild {
    static var version: String {
        let info = Bundle.main.infoDictionary
        let short = info?["CFBundleShortVersionString"] as? String ?? "?"
        let build = info?["CFBundleVersion"] as? String ?? "?"
        return "\(short) (\(build))"
    }

    static var isDebug: Bool {
        #if DEBUG
        true
        #else
        false
        #endif
    }
}

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

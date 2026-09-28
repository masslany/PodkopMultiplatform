import SwiftUI
import Observation
import PodkopShared

@MainActor protocol SettingsServicing {
    func setAutoplayGifs(_ enabled: Bool) async throws
    func setPlayVideosInline(_ enabled: Bool) async throws
    func setTheme(_ theme: ThemeChoice) async throws
    /// Removes downloaded media only; the session and settings stay intact.
    func clearMediaCache()
    func libraries() -> [LibraryNotice]
}

@MainActor
final class SharedSettingsService: SettingsServicing {
    private let client: PodkopClient
    private let adapter: BridgeAdapter
    private let media: MediaStore?

    init(client: PodkopClient, adapter: BridgeAdapter, media: MediaStore?) {
        self.client = client
        self.adapter = adapter
        self.media = media
    }

    func setAutoplayGifs(_ enabled: Bool) async throws {
        let _: IOSSuccess = try await adapter.call { self.client.settings.setAutoplayGifs(enabled: enabled, completion: $0) }
    }

    func setPlayVideosInline(_ enabled: Bool) async throws {
        let _: IOSSuccess = try await adapter.call {
            self.client.settings.setPlayVideosInline(enabled: enabled, completion: $0)
        }
    }

    func setTheme(_ theme: ThemeChoice) async throws {
        let _: IOSSuccess = try await adapter.call { self.client.settings.setTheme(name: theme.rawValue, completion: $0) }
    }

    func clearMediaCache() {
        Task { [media] in await media?.clear() }
        ImageDecoder.shared.clear()
    }

    func libraries() -> [LibraryNotice] {
        client.about.libraries().map {
            LibraryNotice(name: $0.name, artifact: $0.artifact, licenseName: $0.licenseName,
                                licenseURL: $0.licenseUrl.flatMap(URL.init(string:)),
                                projectURL: $0.projectUrl.flatMap(URL.init(string:)))
        }
    }
}

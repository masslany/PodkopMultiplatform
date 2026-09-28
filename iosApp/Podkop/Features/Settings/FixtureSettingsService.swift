import SwiftUI

#if DEBUG
/// Fixture runs have no settings observation, so changes are applied to the session directly.
@MainActor
final class FixtureSettingsService: SettingsServicing {
    private unowned let session: SessionModel
    init(session: SessionModel) { self.session = session }
    func setAutoplayGifs(_ enabled: Bool) async throws { session.autoplayGifs = enabled }
    func setPlayVideosInline(_ enabled: Bool) async throws { session.playVideosInline = enabled }
    func setTheme(_ theme: ThemeChoice) async throws { session.theme = theme }
    func clearMediaCache() { ImageDecoder.shared.clear() }
    func libraries() -> [LibraryNotice] {
        [LibraryNotice(name: "ktor-client-core", artifact: "io.ktor:ktor-client-core",
                             licenseName: "Apache-2.0", licenseURL: URL(string: "https://www.apache.org/licenses/LICENSE-2.0"),
                             projectURL: URL(string: "https://github.com/ktorio/ktor"))]
    }
}
#endif

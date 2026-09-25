import Foundation

#if DEBUG
@MainActor
final class FixtureComposerMedia: ComposerMediaHandling {
    func uploadURL(_ url: String) async throws -> ComposerPhoto {
        ComposerPhoto(key: "fixture-photo", url: url, mimeType: "image/jpeg")
    }
    func uploadDevice(_ data: Data, fileName: String, mimeType: String) async throws -> ComposerPhoto {
        ComposerPhoto(key: "fixture-photo", url: "", mimeType: mimeType)
    }
    func delete(_ key: String) async throws {}
}
#endif

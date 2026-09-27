import Foundation
import Observation
import SwiftUI
import PhotosUI
import UniformTypeIdentifiers

/// One optional photo attached to a draft. Shared by the post composer and private messages.
///
/// Only photos uploaded by this attachment are deleted: replacing, removing or discarding cleans up
/// an owned upload, while a pre-existing server photo (an edit seed) is left untouched.
@MainActor @Observable
final class ComposerAttachment {
    private(set) var photoKey: String?
    private(set) var photoURL: String?
    private(set) var uploading = false
    private(set) var failed = false
    private let media: ComposerMediaHandling?
    private var ownedKey: String?
    private var generation = 0

    init(media: ComposerMediaHandling?, photoKey: String? = nil, photoURL: String? = nil) {
        self.media = media
        self.photoKey = photoKey
        self.photoURL = photoURL
    }

    var isAvailable: Bool { media != nil }

    func attachURL(_ rawURL: String) {
        guard let media, !uploading,
              let url = URL(string: rawURL.trimmingCharacters(in: .whitespacesAndNewlines)),
              ["http", "https"].contains(url.scheme?.lowercased() ?? "") else {
            failed = true
            return
        }
        upload { try await media.uploadURL(url.absoluteString) }
    }

    func attachDevice(_ data: Data, fileName: String, mimeType: String) {
        guard let media, !uploading else { return }
        upload { try await media.uploadDevice(data, fileName: fileName, mimeType: mimeType) }
    }

    func selectionFailed() { failed = true }

    func remove() {
        guard !uploading else { return }
        let owned = ownedKey
        ownedKey = nil
        photoKey = nil
        photoURL = nil
        deleteLater(owned)
    }

    /// Abandons the draft: late uploads are discarded and the owned photo is deleted.
    func discard() {
        generation += 1
        uploading = false
        let owned = ownedKey
        ownedKey = nil
        deleteLater(owned)
    }

    /// The server now references the photo; it must no longer be cleaned up.
    func markSent() {
        ownedKey = nil
        photoKey = nil
        photoURL = nil
    }

    private func upload(_ operation: @escaping () async throws -> ComposerPhoto) {
        generation += 1
        let token = generation
        uploading = true
        failed = false
        Task { [weak self] in
            do {
                let photo = try await operation()
                guard let self, token == self.generation else {
                    try? await self?.media?.delete(photo.key)
                    return
                }
                let previous = self.ownedKey
                self.ownedKey = photo.key
                self.photoKey = photo.key
                self.photoURL = photo.url
                self.uploading = false
                if let previous, previous != photo.key { self.deleteLater(previous) }
            } catch {
                guard let self, token == self.generation else { return }
                self.failed = true
                self.uploading = false
            }
        }
    }

    private func deleteLater(_ key: String?) {
        guard let key, let media else { return }
        Task { try? await media.delete(key) }
    }
}

/// Photo picker and URL entry for a `ComposerAttachment`, including HEIC-to-JPEG conversion.

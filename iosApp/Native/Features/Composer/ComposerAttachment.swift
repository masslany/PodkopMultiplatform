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
struct ComposerAttachmentControls: View {
    let attachment: ComposerAttachment
    var disabled = false
    @State private var selectedPhoto: PhotosPickerItem?
    @State private var showPhotoURL = false
    @State private var photoURLInput = ""

    var body: some View {
        HStack {
            PhotosPicker(selection: $selectedPhoto, matching: .images) {
                Label("Choose photo", systemImage: "photo.on.rectangle")
            }
            Button("Photo URL") { showPhotoURL = true }
        }
        .disabled(disabled || attachment.uploading)
        .alert("Photo URL", isPresented: $showPhotoURL) {
            TextField("https://example.com/photo.jpg", text: $photoURLInput)
            Button("Attach") { attachment.attachURL(photoURLInput) }
            Button("Cancel", role: .cancel) {}
        }
        .onChange(of: selectedPhoto) { _, item in
            guard let item else { return }
            Task {
                await load(item)
                selectedPhoto = nil
            }
        }
    }

    private func load(_ item: PhotosPickerItem) async {
        do {
            guard let loaded = try await item.loadTransferable(type: Data.self) else {
                attachment.selectionFailed()
                return
            }
            let type = item.supportedContentTypes.first { $0.conforms(to: .image) }
            let originalMime = type?.preferredMIMEType ?? "image/jpeg"
            if originalMime == "image/heic" || originalMime == "image/heif" {
                guard let jpeg = UIImage(data: loaded)?.jpegData(compressionQuality: 0.85) else {
                    attachment.selectionFailed()
                    return
                }
                attachment.attachDevice(jpeg, fileName: "photo.jpg", mimeType: "image/jpeg")
            } else {
                let ext = type?.preferredFilenameExtension ?? "jpg"
                attachment.attachDevice(loaded, fileName: "photo.\(ext)", mimeType: originalMime)
            }
        } catch {
            attachment.selectionFailed()
        }
    }
}

/// Attached-photo row and upload/failure feedback for a `ComposerAttachment`.
struct ComposerAttachmentStatus: View {
    let attachment: ComposerAttachment
    var disabled = false

    var body: some View {
        if attachment.uploading { ProgressView("Uploading photo…") }
        if attachment.photoKey != nil {
            HStack {
                Label("Attached photo", systemImage: "photo").foregroundStyle(.secondary)
                Button("Remove", role: .destructive) { attachment.remove() }
                    .disabled(disabled || attachment.uploading)
                    .accessibilityIdentifier("attachmentRemove")
            }
        }
        if attachment.failed {
            Label("Could not attach photo. Try again.", systemImage: "exclamationmark.triangle")
                .foregroundStyle(.red)
        }
    }
}

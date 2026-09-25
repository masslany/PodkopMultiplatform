import SwiftUI
import UIKit
import PhotosUI
import UniformTypeIdentifiers

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

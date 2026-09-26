import SwiftUI
import UIKit
import PhotosUI
import UniformTypeIdentifiers

struct ComposerAttachmentControls: View {
    let attachment: ComposerAttachment
    var disabled = false
    var compact = false
    @State private var selectedPhoto: PhotosPickerItem?
    @State private var showPhotoURL = false
    @State private var photoURLInput = ""

    var body: some View {
        HStack {
            PhotosPicker(selection: $selectedPhoto, matching: .images) {
                if compact { Image(systemName: "photo.badge.plus").frame(minWidth: 44, minHeight: 44) }
                else { Label(.composerChoosePhoto, systemImage: "photo.on.rectangle") }
            }
            .accessibilityLabel(.composerChoosePhoto)
            Button { showPhotoURL = true } label: {
                if compact { Image(systemName: "link").frame(minWidth: 44, minHeight: 44) }
                else { Text(.composerPhotoURL) }
            }
            .accessibilityLabel(.composerPhotoURL)
        }
        .disabled(disabled || attachment.uploading)
        .alert(.composerPhotoURL, isPresented: $showPhotoURL) {
            TextField(String("https://example.com/photo.jpg"), text: $photoURLInput)
            Button(.composerAttach) { attachment.attachURL(photoURLInput) }
            Button(.commonCancel, role: .cancel) {}
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
        if attachment.uploading { ProgressView(.composerUploadingPhoto) }
        if attachment.photoKey != nil {
            HStack {
                Label(.composerAttachedPhoto, systemImage: "photo").foregroundStyle(.secondary)
                Button(.commonRemove, role: .destructive) { attachment.remove() }
                    .disabled(disabled || attachment.uploading)
                    .accessibilityIdentifier("attachmentRemove")
            }
        }
        if attachment.failed {
            Label(.composerCouldNotAttachPhoto, systemImage: "exclamationmark.triangle")
                .foregroundStyle(.red)
        }
    }
}

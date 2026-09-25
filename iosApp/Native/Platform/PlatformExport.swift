import Photos
import SafariServices
import SwiftUI
import UIKit
import UniformTypeIdentifiers

enum PhotoSaveResult: Equatable { case saved, denied, failed }

/// Saving, copying and link presentation. Everything runs on the main actor and is scene-local.
@MainActor
enum PlatformExport {
    /// Adds image data to the photo library with add-only access. Original bytes are kept so
    /// animated GIFs stay animated.
    static func saveToPhotos(_ data: Data) async -> PhotoSaveResult {
        guard fileExtension(for: data) != nil else { return .failed }
        let status = await PHPhotoLibrary.requestAuthorization(for: .addOnly)
        guard status == .authorized || status == .limited else { return .denied }
        do {
            try await PHPhotoLibrary.shared().performChanges {
                PHAssetCreationRequest.forAsset().addResource(with: .photo, data: data, options: nil)
            }
            return .saved
        } catch {
            return .failed
        }
    }

    /// Copies image data, keeping GIF animation when the source is a GIF.
    static func copyImage(_ data: Data) -> Bool {
        if isGIF(data) {
            UIPasteboard.general.setData(data, forPasteboardType: UTType.gif.identifier)
            return true
        }
        guard let image = UIImage(data: data) else { return false }
        UIPasteboard.general.image = image
        return true
    }

    nonisolated static func isGIF(_ data: Data) -> Bool { data.starts(with: [0x47, 0x49, 0x46]) }

    nonisolated static func fileExtension(for data: Data) -> String? {
        if isGIF(data) { return "gif" }
        if data.starts(with: [0x89, 0x50, 0x4e, 0x47]) { return "png" }
        if data.starts(with: [0xff, 0xd8, 0xff]) { return "jpg" }
        return nil
    }

}

struct NativeSafariView: UIViewControllerRepresentable {
    let url: URL

    func makeUIViewController(context: Context) -> SFSafariViewController {
        let controller = SFSafariViewController(url: url)
        controller.dismissButtonStyle = .close
        return controller
    }

    func updateUIViewController(_ controller: SFSafariViewController, context: Context) {}
}

/// The activity controller receives a real file with the source format. Cleanup happens when
/// the activity completes or is cancelled, including when the sheet is dismissed on iPad.
private struct ImageShareSheet: UIViewControllerRepresentable {
    let data: Data
    let onMessage: (String) -> Void

    func makeUIViewController(context: Context) -> UIActivityViewController {
        guard let ext = PlatformExport.fileExtension(for: data) else {
            return UIActivityViewController(activityItems: [], applicationActivities: nil)
        }
        let directory = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString,
                                                                                        isDirectory: true)
        do {
            try FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
            let file = directory.appendingPathComponent("podkop.\(ext)")
            try data.write(to: file, options: .atomic)
            let controller = UIActivityViewController(activityItems: [file], applicationActivities: nil)
            controller.completionWithItemsHandler = { _, _, _, error in
                try? FileManager.default.removeItem(at: directory)
                if error != nil {
                    Task { @MainActor in
                        onMessage(String(localized: "Could not complete this action. Try again."))
                    }
                }
            }
            return controller
        } catch {
            try? FileManager.default.removeItem(at: directory)
            onMessage(String(localized: "Could not complete this action. Try again."))
            return UIActivityViewController(activityItems: [], applicationActivities: nil)
        }
    }

    func updateUIViewController(_ controller: UIActivityViewController, context: Context) {}
}

/// Save, copy and share controls for image bytes; feedback goes to a caller-provided message.
struct ImageExportControls: View {
    let data: Data
    let onMessage: (String) -> Void
    @State private var sharing = false

    var body: some View {
        HStack(spacing: 12) {
            Button {
                Task {
                    switch await PlatformExport.saveToPhotos(data) {
                    case .saved: onMessage(String(localized: "Saved to Photos"))
                    case .denied: onMessage(String(localized: "Allow adding photos in Settings to save images."))
                    case .failed: onMessage(String(localized: "Could not complete this action. Try again."))
                    }
                }
            } label: {
                Label("Save", systemImage: "square.and.arrow.down")
            }
            .accessibilityIdentifier("imageSave")
            Button {
                onMessage(PlatformExport.copyImage(data)
                          ? String(localized: "Image copied")
                          : String(localized: "Could not complete this action. Try again."))
            } label: {
                Label("Copy", systemImage: "doc.on.doc")
            }
            .accessibilityIdentifier("imageCopy")
            Button {
                sharing = true
            } label: {
                Label("Share", systemImage: "square.and.arrow.up")
            }
            .accessibilityIdentifier("imageShare")
            .disabled(PlatformExport.fileExtension(for: data) == nil)
        }
        .buttonStyle(.bordered)
        .sheet(isPresented: $sharing) { ImageShareSheet(data: data, onMessage: onMessage) }
    }
}

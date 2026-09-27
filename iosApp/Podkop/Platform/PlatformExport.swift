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

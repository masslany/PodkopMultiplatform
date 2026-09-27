import SwiftUI
import UIKit

@MainActor
enum ScreenshotSurface {
    /// Renders synchronously, so remote images must be passed in as bytes keyed by URL.
    static func render(resource: Resource, parent: Resource? = nil,
                       photoBytes: [String: Data] = [:], colorScheme: ColorScheme = .light,
                       width: CGFloat = 390, scale: CGFloat = 2) -> UIImage? {
        let images = photoBytes.reduce(into: [String: UIImage]()) { result, item in
            if let image = ImageDecoder.shared.image(from: item.value, key: item.key, maxDimension: 1200) {
                result[item.key] = image.images?.first ?? image
            }
        }
        let surface = VStack(alignment: .leading, spacing: 14) {
            if let parent {
                ResourceCard(resource: parent, style: .embedded,
                             screenshotPhoto: screenshotPhoto(for: parent, bytes: photoBytes), showsActions: false)
                Divider().overlay(PodkopTheme.separator)
            }
            ResourceCard(resource: resource, style: .embedded,
                         screenshotPhoto: screenshotPhoto(for: resource, bytes: photoBytes), showsActions: false)
        }
        .podkopCard(padding: 18)
        .padding(14)
        .frame(width: width)
        .background(PodkopTheme.background)
        .environment(\.mediaLoader, nil)
        .environment(\.screenshotImages, images)
        .environment(\.colorScheme, colorScheme)
        let renderer = ImageRenderer(content: surface)
        renderer.scale = scale
        return renderer.uiImage
    }

    private static func screenshotPhoto(for resource: Resource, bytes: [String: Data]) -> UIImage? {
        guard let photo = resource.photo, let data = bytes[photo.url] else { return nil }
        return ImageDecoder.shared.image(from: data, key: photo.url, maxDimension: 1200)
    }
}

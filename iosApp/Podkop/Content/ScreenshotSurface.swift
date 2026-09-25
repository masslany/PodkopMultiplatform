import SwiftUI
import UIKit

@MainActor
enum ScreenshotSurface {
    /// Renders synchronously, so remote images must be passed in as bytes keyed by URL.
    static func render(resource: Resource, parent: Resource? = nil,
                       photoBytes: [String: Data] = [:],
                       width: CGFloat = 390, scale: CGFloat = 2) -> UIImage? {
        let surface = VStack(spacing: 8) {
            if let parent {
                ResourceCard(resource: parent, screenshotPhoto: screenshotPhoto(for: parent, bytes: photoBytes))
            }
            ResourceCard(resource: resource, screenshotPhoto: screenshotPhoto(for: resource, bytes: photoBytes))
        }
        .environment(\.mediaLoader, nil)
        .padding()
        .frame(width: width)
        .background(Color(uiColor: .systemBackground))
        let renderer = ImageRenderer(content: surface)
        renderer.scale = scale
        return renderer.uiImage
    }

    private static func screenshotPhoto(for resource: Resource, bytes: [String: Data]) -> UIImage? {
        guard let photo = resource.photo, let data = bytes[photo.url] else { return nil }
        return ImageDecoder.shared.image(from: data, key: photo.url, maxDimension: 1200)
    }
}

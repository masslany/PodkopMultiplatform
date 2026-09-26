import SwiftUI
import PodkopShared

struct RemoteImage<Placeholder: View>: View {
    let url: String?
    var maxDimension = 600
    var contentMode: ContentMode = .fill
    @ViewBuilder var placeholder: () -> Placeholder
    @Environment(\.mediaLoader) private var loader
    @State private var image: UIImage?
    @Environment(\.screenshotImages) private var screenshotImages

    var body: some View {
        Group {
            if let image = screenshotImages[url ?? ""] ?? image {
                if contentMode == .fill {
                    // A filling image takes exactly the offered space and crops; sized by its own
                    // aspect ratio it would push its container wider than the screen.
                    Color.clear
                        .overlay { Image(uiImage: image).resizable().scaledToFill() }
                        .clipped()
                } else {
                    Image(uiImage: image).resizable().scaledToFit()
                }
            } else {
                placeholder()
            }
        }
        .task(id: url) {
            guard let url, !url.isEmpty, let loader else { return }
            guard let data = try? await loader.bytes(for: url), !Task.isCancelled else { return }
            let dimension = maxDimension
            let decoded = await Task.detached(priority: .utility) {
                ImageDecoder.shared.image(from: data, key: url, maxDimension: dimension)
            }.value
            if !Task.isCancelled { image = decoded?.images?.first ?? decoded }
        }
    }
}

/// Already decoded images for synchronous screenshot rendering; ordinary views use their loader.
private struct ScreenshotImagesKey: EnvironmentKey {
    static let defaultValue: [String: UIImage] = [:]
}

extension EnvironmentValues {
    var screenshotImages: [String: UIImage] {
        get { self[ScreenshotImagesKey.self] }
        set { self[ScreenshotImagesKey.self] = newValue }
    }
}

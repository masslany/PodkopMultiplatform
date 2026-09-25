import SwiftUI
import PodkopShared

struct RemoteImage<Placeholder: View>: View {
    let url: String?
    var maxDimension = 600
    var contentMode: ContentMode = .fill
    @ViewBuilder var placeholder: () -> Placeholder
    @Environment(\.mediaLoader) private var loader
    @State private var image: UIImage?

    var body: some View {
        Group {
            if let image {
                Image(uiImage: image).resizable().aspectRatio(contentMode: contentMode)
            } else {
                placeholder()
            }
        }
        .task(id: url) {
            guard let url, !url.isEmpty, let loader else { return }
            guard let data = try? await loader.bytes(for: url), !Task.isCancelled else { return }
            let dimension = maxDimension
            let decoded = await Task.detached(priority: .utility) {
                NativeImageDecoder.shared.image(from: data, key: url, maxDimension: dimension)
            }.value
            if !Task.isCancelled { image = decoded?.images?.first ?? decoded }
        }
    }
}

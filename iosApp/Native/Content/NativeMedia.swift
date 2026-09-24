import ImageIO
import SwiftUI
import UIKit

/// Decoded images are capped by source bytes, output dimensions, frame count and cache cost.
final class NativeImageDecoder {
    static let shared = NativeImageDecoder()
    static let maximumInputBytes = 20 * 1024 * 1024
    private let cache = NSCache<NSString, UIImage>()

    private init() {
        cache.totalCostLimit = 48 * 1024 * 1024
        cache.countLimit = 32
    }

    func image(from bytes: Data, key: String, maxDimension: Int) -> UIImage? {
        guard !bytes.isEmpty, bytes.count <= Self.maximumInputBytes else { return nil }
        let cappedDimension = min(max(maxDimension, 64), 2048)
        let cacheKey = "\(key)#\(cappedDimension)" as NSString
        if let cached = cache.object(forKey: cacheKey) { return cached }
        guard let source = CGImageSourceCreateWithData(bytes as CFData, nil),
              CGImageSourceGetCount(source) > 0 else { return nil }
        let count = CGImageSourceGetCount(source)
        let animated = count > 1
        let frameLimit = min(count, 24)
        let frameDimension = animated ? min(cappedDimension, 512) : cappedDimension
        var frames: [UIImage] = []
        var duration = 0.0
        for index in 0..<frameLimit {
            let options: [CFString: Any] = [
                kCGImageSourceCreateThumbnailFromImageAlways: true,
                kCGImageSourceCreateThumbnailWithTransform: true,
                kCGImageSourceThumbnailMaxPixelSize: frameDimension,
                kCGImageSourceShouldCacheImmediately: false
            ]
            guard let image = CGImageSourceCreateThumbnailAtIndex(source, index, options as CFDictionary) else {
                continue
            }
            frames.append(UIImage(cgImage: image))
            if animated {
                let properties = CGImageSourceCopyPropertiesAtIndex(source, index, nil) as? [String: Any]
                let gif = properties?[kCGImagePropertyGIFDictionary as String] as? [String: Any]
                let delay = gif?[kCGImagePropertyGIFUnclampedDelayTime as String] as? Double
                    ?? gif?[kCGImagePropertyGIFDelayTime as String] as? Double ?? 0.1
                duration += max(0.05, delay)
            }
        }
        guard let first = frames.first else { return nil }
        let result = animated && frames.count > 1
            ? UIImage.animatedImage(with: frames, duration: duration) ?? first : first
        let cost = frames.reduce(0) { total, frame in
            guard let cg = frame.cgImage else { return total }
            return total + cg.bytesPerRow * cg.height
        }
        cache.setObject(result, forKey: cacheKey, cost: min(cost, cache.totalCostLimit))
        return result
    }

    func clear() { cache.removeAllObjects() }
}

struct NativeDecodedImage: View {
    let bytes: Data
    let cacheKey: String
    let maxDimension: Int
    var animated = false
    @State private var image: UIImage?
    @State private var failed = false

    var body: some View {
        Group {
            if let image {
                NativeUIImageView(image: image, playing: animated)
                    .accessibilityLabel("Image")
            } else if failed {
                Label("Image unavailable", systemImage: "photo")
            } else {
                ProgressView().frame(maxWidth: .infinity, minHeight: 120)
            }
        }
        .task(id: cacheKey) {
            let bytes = bytes
            let cacheKey = cacheKey
            let maxDimension = maxDimension
            let decoded = await Task.detached(priority: .utility) {
                NativeImageDecoder.shared.image(from: bytes, key: cacheKey, maxDimension: maxDimension)
            }.value
            if !Task.isCancelled { image = decoded; failed = decoded == nil }
        }
        .onDisappear { image = nil }
    }
}

private struct NativeUIImageView: UIViewRepresentable {
    let image: UIImage
    let playing: Bool

    func makeUIView(context: Context) -> UIImageView {
        let view = UIImageView()
        view.contentMode = .scaleAspectFit
        view.clipsToBounds = true
        return view
    }

    func updateUIView(_ view: UIImageView, context: Context) {
        if view.image !== image { view.image = image }
        if playing { view.startAnimating() } else { view.stopAnimating() }
    }
}

struct NativeMediaView: View {
    let photo: NativePhoto
    let bytes: Data?
    let autoplay: Bool
    let foreground: Bool
    @State private var playbackOverride: Bool?
    @State private var visible = true
    @Environment(\.scenePhase) private var scenePhase

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            if let bytes {
                NativeDecodedImage(bytes: bytes, cacheKey: photo.url,
                                   maxDimension: 1200,
                                   animated: visible && foreground && scenePhase == .active
                                       && (playbackOverride ?? autoplay))
                    .frame(maxWidth: .infinity)
                    .frame(height: displayHeight)
                    .background(.quaternary, in: RoundedRectangle(cornerRadius: 8))
                if photo.isAnimated {
                    Button((playbackOverride ?? autoplay) ? "Pause animation" : "Play animation") {
                        playbackOverride = !(playbackOverride ?? autoplay)
                    }
                }
            } else {
                Label("Image unavailable", systemImage: "photo")
                    .frame(maxWidth: .infinity, minHeight: 120)
                    .background(.quaternary, in: RoundedRectangle(cornerRadius: 8))
            }
        }
        .onAppear { visible = true }
        .onDisappear { visible = false }
    }

    private var displayHeight: CGFloat {
        guard photo.width > 0, photo.height > 0 else { return 200 }
        return min(320, max(120, 320 * CGFloat(photo.height) / CGFloat(photo.width)))
    }
}

struct NativeImageViewer: View {
    let bytes: Data
    let key: String
    @State private var image: UIImage?

    var body: some View {
        Group {
            if let image {
                ZoomableImage(image: image)
                    .ignoresSafeArea(edges: .bottom)
            } else {
                ProgressView("Loading image…")
            }
        }
        .task(id: key) {
            let bytes = bytes
            let key = key
            let decoded = await Task.detached(priority: .utility) {
                NativeImageDecoder.shared.image(from: bytes, key: key, maxDimension: 2048)
            }.value
            if !Task.isCancelled { image = decoded }
        }
        .onDisappear { image = nil }
    }
}

private struct ZoomableImage: UIViewRepresentable {
    let image: UIImage

    func makeCoordinator() -> Coordinator { Coordinator() }
    func makeUIView(context: Context) -> UIScrollView {
        let scroll = UIScrollView()
        scroll.minimumZoomScale = 1
        scroll.maximumZoomScale = 4
        scroll.delegate = context.coordinator
        let imageView = UIImageView(image: image)
        imageView.contentMode = .scaleAspectFit
        imageView.translatesAutoresizingMaskIntoConstraints = false
        scroll.addSubview(imageView)
        NSLayoutConstraint.activate([
            imageView.leadingAnchor.constraint(equalTo: scroll.contentLayoutGuide.leadingAnchor),
            imageView.trailingAnchor.constraint(equalTo: scroll.contentLayoutGuide.trailingAnchor),
            imageView.topAnchor.constraint(equalTo: scroll.contentLayoutGuide.topAnchor),
            imageView.bottomAnchor.constraint(equalTo: scroll.contentLayoutGuide.bottomAnchor),
            imageView.widthAnchor.constraint(equalTo: scroll.frameLayoutGuide.widthAnchor),
            imageView.heightAnchor.constraint(equalTo: scroll.frameLayoutGuide.heightAnchor)
        ])
        context.coordinator.imageView = imageView
        return scroll
    }
    func updateUIView(_ scroll: UIScrollView, context: Context) {
        context.coordinator.imageView?.image = image
    }
    final class Coordinator: NSObject, UIScrollViewDelegate {
        weak var imageView: UIImageView?
        func viewForZooming(in scrollView: UIScrollView) -> UIView? { imageView }
    }
}

@MainActor
enum NativeScreenshotSurface {
    static func render(resource: NativeResource, parent: NativeResource? = nil,
                       width: CGFloat = 390, scale: CGFloat = 2) -> UIImage? {
        let surface = VStack(spacing: 8) {
            if let parent { NativeResourceCard(resource: parent) }
            NativeResourceCard(resource: resource)
        }
        .padding()
        .frame(width: width)
        .background(Color(uiColor: .systemBackground))
        let renderer = ImageRenderer(content: surface)
        renderer.scale = scale
        return renderer.uiImage
    }
}

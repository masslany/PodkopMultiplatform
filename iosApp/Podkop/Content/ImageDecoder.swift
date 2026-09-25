import ImageIO
import SwiftUI
import UIKit

/// Decoded images are capped by source bytes, output dimensions, frame count and cache cost.
final class ImageDecoder {
    static let shared = ImageDecoder()
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

struct DecodedImage: View {
    let bytes: Data
    let cacheKey: String
    let maxDimension: Int
    var animated = false
    @State private var image: UIImage?
    @State private var failed = false

    var body: some View {
        Group {
            if let image {
                AnimatedImageView(image: image, playing: animated)
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
                ImageDecoder.shared.image(from: bytes, key: cacheKey, maxDimension: maxDimension)
            }.value
            if !Task.isCancelled { image = decoded; failed = decoded == nil }
        }
        .onDisappear { image = nil }
    }
}

/// A UIImageView without an intrinsic size, so large images cannot widen SwiftUI layouts.
private final class ProposedSizeImageView: UIImageView {
    override var intrinsicContentSize: CGSize {
        CGSize(width: UIView.noIntrinsicMetric, height: UIView.noIntrinsicMetric)
    }
}

private struct AnimatedImageView: UIViewRepresentable {
    let image: UIImage
    let playing: Bool

    func makeUIView(context: Context) -> UIImageView {
        let view = ProposedSizeImageView()
        view.contentMode = .scaleAspectFit
        view.clipsToBounds = true
        return view
    }

    /// Takes the size SwiftUI offers instead of the image's pixel size.
    func sizeThatFits(_ proposal: ProposedViewSize, uiView: UIImageView, context: Context) -> CGSize? {
        guard let width = proposal.width, let height = proposal.height else { return nil }
        return CGSize(width: width, height: height)
    }

    func updateUIView(_ view: UIImageView, context: Context) {
        if view.image !== image { view.image = image }
        if playing { view.startAnimating() } else { view.stopAnimating() }
    }
}

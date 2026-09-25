import SwiftUI
import PodkopShared

@MainActor protocol MediaLoading: AnyObject {
    func bytes(for url: String) async throws -> Data
}

/// Fetches image bytes through the shared media service. Concurrent requests for one URL share
/// a single download, and recently used bytes stay in a small bounded memory cache.
@MainActor
final class SharedMediaLoader: MediaLoading {
    private let client: PodkopClient
    private let adapter: BridgeAdapter
    private let cache = NSCache<NSString, NSData>()
    private var inFlight: [String: Task<Data, Error>] = [:]

    init(client: PodkopClient, adapter: BridgeAdapter) {
        self.client = client
        self.adapter = adapter
        cache.totalCostLimit = 32 * 1024 * 1024
    }

    func bytes(for url: String) async throws -> Data {
        if let cached = cache.object(forKey: url as NSString) { return cached as Data }
        if let running = inFlight[url] { return try await running.value }
        let task = Task { [client, adapter] () throws -> Data in
            let value: IOSMediaBytes = try await adapter.call { client.mediaBytes.load(url: url, completion: $0) }
            return value.data as Data
        }
        inFlight[url] = task
        defer { inFlight[url] = nil }
        let data = try await task.value
        cache.setObject(data as NSData, forKey: url as NSString, cost: data.count)
        return data
    }

    func clearMemory() { cache.removeAllObjects() }
}

#if DEBUG
/// Fixture runs stay offline: every image resolves to a small generated PNG.
@MainActor
final class FixtureMediaLoader: MediaLoading {
    private lazy var png: Data = UIGraphicsImageRenderer(size: CGSize(width: 64, height: 40)).pngData { context in
        UIColor.systemTeal.setFill()
        context.fill(CGRect(x: 0, y: 0, width: 64, height: 40))
    }
    func bytes(for url: String) async throws -> Data { png }
}
#endif

private struct MediaLoaderKey: EnvironmentKey {
    static let defaultValue: MediaLoading? = nil
}

extension EnvironmentValues {
    var mediaLoader: MediaLoading? {
        get { self[MediaLoaderKey.self] }
        set { self[MediaLoaderKey.self] = newValue }
    }
}

/// Loads a remote image on appearance and cancels when it leaves the screen.
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

/// Author avatar with the name's initial as the placeholder, as before images were available.
struct AvatarView: View {
    let url: String?
    let name: String
    var size: CGFloat = 36

    var body: some View {
        RemoteImage(url: url, maxDimension: Int(size * 3)) {
            Circle().fill(ContentTokens.brand.opacity(0.16))
                .overlay(Text(String(name.prefix(1)).uppercased())
                    .font(.system(size: size * 0.42, weight: .bold)))
        }
        .frame(width: size, height: size)
        .clipShape(Circle())
        .accessibilityHidden(true)
    }
}

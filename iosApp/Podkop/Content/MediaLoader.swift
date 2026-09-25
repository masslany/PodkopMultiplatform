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



private struct MediaLoaderKey: EnvironmentKey {
    static let defaultValue: MediaLoading? = nil
}

extension EnvironmentValues {
    var mediaLoader: MediaLoading? {
        get { self[MediaLoaderKey.self] }
        set { self[MediaLoaderKey.self] = newValue }
    }
}

import CryptoKit
import SwiftUI

@MainActor protocol MediaLoading: AnyObject {
    func bytes(for url: String) async throws -> Data
    /// Drops these images from every cache, so the next request downloads them again.
    func evict(_ urls: [String]) async
}

extension MediaLoading {
    func evict(_ urls: [String]) async {}
}

/// Loads public media (photos, embed thumbnails, avatars, badges), like Android's Coil: a memory
/// cache, then a disk cache kept by URL, then the network.
///
/// Media URLs change whenever the file does, so cached files never go stale and the servers'
/// cache headers (which allow ten years anyway) are not needed. Downloads use their own session
/// with no cookies or account tokens, accept only http(s), and stop at `maximumBytes`.
@MainActor
final class MediaStore: MediaLoading {
    static let maximumBytes = 20 * 1024 * 1024

    private let session: URLSession
    private let disk: MediaDiskCache
    private let memory = NSCache<NSString, NSData>()
    private var inFlight: [String: Task<Data, Error>] = [:]

    init(disk: MediaDiskCache = MediaDiskCache(), session: URLSession = MediaStore.makeSession()) {
        self.disk = disk
        self.session = session
        memory.totalCostLimit = 32 * 1024 * 1024
        Task { await disk.trim() }
    }

    nonisolated static func makeSession() -> URLSession {
        let configuration = URLSessionConfiguration.ephemeral
        configuration.httpCookieStorage = nil
        configuration.httpShouldSetCookies = false
        configuration.urlCache = nil
        configuration.requestCachePolicy = .reloadIgnoringLocalCacheData
        configuration.httpMaximumConnectionsPerHost = 6
        configuration.timeoutIntervalForRequest = 15
        configuration.timeoutIntervalForResource = 30
        return URLSession(configuration: configuration)
    }

    func bytes(for url: String) async throws -> Data {
        if let cached = memory.object(forKey: url as NSString) { return cached as Data }
        if let running = inFlight[url] { return try await running.value }
        let task = Task { [disk, session] in try await Self.load(url, disk: disk, session: session) }
        inFlight[url] = task
        defer { inFlight[url] = nil }
        let data = try await task.value
        memory.setObject(data as NSData, forKey: url as NSString, cost: data.count)
        return data
    }

    func evict(_ urls: [String]) async {
        for url in urls {
            memory.removeObject(forKey: url as NSString)
            await disk.remove(url)
        }
    }

    /// Removes cached media only; account data and settings are untouched.
    func clear() async {
        memory.removeAllObjects()
        await disk.removeAll()
    }

    private nonisolated static func load(_ url: String, disk: MediaDiskCache, session: URLSession) async throws -> Data {
        if let data = await disk.read(url) { return data }
        guard let target = URL(string: url.trimmingCharacters(in: .whitespaces)),
              ["http", "https"].contains(target.scheme?.lowercased() ?? "") else {
            throw URLError(.unsupportedURL)
        }
        // A download lands in a file, so its size is checked before it is read into memory.
        let (file, response) = try await session.download(for: URLRequest(url: target))
        defer { try? FileManager.default.removeItem(at: file) }
        guard let http = response as? HTTPURLResponse, (200..<300).contains(http.statusCode) else {
            throw URLError(.badServerResponse)
        }
        let size = (try? file.resourceValues(forKeys: [.fileSizeKey]).fileSize) ?? 0
        guard size > 0, size <= maximumBytes else { throw URLError(.dataLengthExceedsMaximum) }
        let data = try Data(contentsOf: file)
        await disk.write(data, for: url)
        return data
    }
}

/// Downloaded media on disk, kept by URL. When it grows past `limit`, the least recently used
/// files go first, like Coil's disk cache.
actor MediaDiskCache {
    private let directory: URL
    private let limit: Int
    private var writesSinceTrim = 0

    init(directory: URL? = nil, limit: Int = 250 * 1024 * 1024) {
        let caches = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask)[0]
        self.directory = directory ?? caches.appendingPathComponent("media", isDirectory: true)
        self.limit = limit
        try? FileManager.default.createDirectory(at: self.directory, withIntermediateDirectories: true)
    }

    func read(_ url: String) -> Data? {
        let file = path(for: url)
        guard let data = try? Data(contentsOf: file) else { return nil }
        // The modification date marks the last use, which decides what a trim removes.
        try? FileManager.default.setAttributes([.modificationDate: Date()], ofItemAtPath: file.path)
        return data
    }

    func write(_ data: Data, for url: String) {
        try? data.write(to: path(for: url), options: .atomic)
        writesSinceTrim += 1
        if writesSinceTrim >= 25 { trim() }
    }

    func remove(_ url: String) {
        try? FileManager.default.removeItem(at: path(for: url))
    }

    func removeAll() {
        let files = (try? FileManager.default.contentsOfDirectory(at: directory, includingPropertiesForKeys: nil)) ?? []
        for file in files { try? FileManager.default.removeItem(at: file) }
    }

    func size() -> Int { entries().reduce(0) { $0 + $1.size } }

    /// Removes the least recently used files until the cache is back under 80% of its limit.
    func trim() {
        writesSinceTrim = 0
        var files = entries()
        var total = files.reduce(0) { $0 + $1.size }
        guard total > limit else { return }
        files.sort { $0.used < $1.used }
        for file in files where total > limit * 8 / 10 {
            try? FileManager.default.removeItem(at: file.url)
            total -= file.size
        }
    }

    private func entries() -> [(url: URL, size: Int, used: Date)] {
        let keys: [URLResourceKey] = [.fileSizeKey, .contentModificationDateKey]
        let files = (try? FileManager.default.contentsOfDirectory(at: directory, includingPropertiesForKeys: keys)) ?? []
        return files.compactMap { file in
            guard let values = try? file.resourceValues(forKeys: Set(keys)) else { return nil }
            return (file, values.fileSize ?? 0, values.contentModificationDate ?? .distantPast)
        }
    }

    private func path(for url: String) -> URL {
        let digest = SHA256.hash(data: Data(url.utf8)).map { String(format: "%02x", $0) }.joined()
        return directory.appendingPathComponent(digest)
    }
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

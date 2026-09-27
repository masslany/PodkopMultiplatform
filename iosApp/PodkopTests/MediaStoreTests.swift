import XCTest
@testable import Podkop

/// Answers every request with `body` and counts the requests.
private final class StubMediaProtocol: URLProtocol {
    nonisolated(unsafe) static var body = Data(repeating: 7, count: 1024)
    nonisolated(unsafe) static var status = 200
    nonisolated(unsafe) static var requests = 0

    override class func canInit(with request: URLRequest) -> Bool { true }
    override class func canonicalRequest(for request: URLRequest) -> URLRequest { request }
    override func startLoading() {
        Self.requests += 1
        let response = HTTPURLResponse(url: request.url!, statusCode: Self.status, httpVersion: nil, headerFields: nil)!
        client?.urlProtocol(self, didReceive: response, cacheStoragePolicy: .notAllowed)
        client?.urlProtocol(self, didLoad: Self.body)
        client?.urlProtocolDidFinishLoading(self)
    }
    override func stopLoading() {}
}

@MainActor
final class MediaStoreTests: XCTestCase {
    private var directory: URL!

    override func setUp() {
        directory = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString)
        StubMediaProtocol.body = Data(repeating: 7, count: 1024)
        StubMediaProtocol.status = 200
        StubMediaProtocol.requests = 0
    }

    override func tearDown() {
        try? FileManager.default.removeItem(at: directory)
    }

    private func store(limit: Int = 1024 * 1024) -> MediaStore {
        let configuration = URLSessionConfiguration.ephemeral
        configuration.protocolClasses = [StubMediaProtocol.self]
        return MediaStore(disk: MediaDiskCache(directory: directory, limit: limit),
                          session: URLSession(configuration: configuration))
    }

    func testImagesSurviveARelaunchOnDisk() async throws {
        let url = "https://example.com/avatar.jpg"
        let first = try await store().bytes(for: url)
        // A new store is a new launch: memory is empty, the disk still has the file.
        let second = try await store().bytes(for: url)
        XCTAssertEqual(first, second)
        XCTAssertEqual(StubMediaProtocol.requests, 1)
    }

    func testEvictDownloadsTheImageAgain() async throws {
        let url = "https://example.com/banner.jpg"
        let media = store()
        _ = try await media.bytes(for: url)
        await media.evict([url])
        _ = try await media.bytes(for: url)
        XCTAssertEqual(StubMediaProtocol.requests, 2)
    }

    func testRejectsOtherSchemesFailuresAndOversizedFiles() async {
        let media = store()
        await XCTAssertThrowsAsync(try await media.bytes(for: "file:///etc/hosts"))
        StubMediaProtocol.status = 404
        await XCTAssertThrowsAsync(try await media.bytes(for: "https://example.com/missing.jpg"))
        StubMediaProtocol.status = 200
        StubMediaProtocol.body = Data(count: MediaStore.maximumBytes + 1)
        await XCTAssertThrowsAsync(try await media.bytes(for: "https://example.com/huge.jpg"))
        let stored = await MediaDiskCache(directory: directory).size()
        XCTAssertEqual(stored, 0, "failed downloads must not be cached")
    }

    func testTrimRemovesTheLeastRecentlyUsedFiles() async throws {
        let disk = MediaDiskCache(directory: directory, limit: 2500)
        for name in ["a", "b", "c"] {
            await disk.write(Data(repeating: 1, count: 1000), for: name)
            try await Task.sleep(for: .milliseconds(20))
        }
        _ = await disk.read("a") // "a" is now the most recently used
        await disk.trim()
        let a = await disk.read("a")
        let b = await disk.read("b")
        let c = await disk.read("c")
        XCTAssertNotNil(a)
        XCTAssertNil(b, "the least recently used file goes first")
        XCTAssertNotNil(c)
    }
}

@MainActor
private func XCTAssertThrowsAsync<T>(_ expression: @autoclosure () async throws -> T,
                                     file: StaticString = #filePath, line: UInt = #line) async {
    do {
        _ = try await expression()
        XCTFail("expected an error", file: file, line: line)
    } catch {}
}

import XCTest
import PodkopShared
@testable import PodkopNative

final class BridgeTests: XCTestCase {
    func testRealFrameworkPolicyAndObservation() async throws {
        let client = PodkopClient.companion.create()
        let adapter = BridgeAdapter()
        let policy = client.links.policy(isLoggedIn: true, isUpcoming: false)
        XCTAssertEqual(policy.kind, "CursorInPage")
        XCTAssertEqual(policy.initial.kind, "initial")
        let stream = adapter.stream { client.startup.observe(onChange: $0) }
        var iterator = stream.makeAsyncIterator()
        let first = await iterator.next()
        XCTAssertEqual(first?.phase, "initializing")
        adapter.close()
        client.close()
    }

    func testCancelledOperationResumesSwiftTask() async throws {
        let client = PodkopClient.companion.create()
        let adapter = BridgeAdapter()
        let task = Task {
            let _: IOSSuccess = try await adapter.call { client.startup.start(key: "", secret: "", completion: $0) }
        }
        task.cancel()
        do {
            try await task.value
            XCTFail("expected cancellation")
        } catch is CancellationError {
            // Expected.
        }
        adapter.close()
        client.close()
    }
}

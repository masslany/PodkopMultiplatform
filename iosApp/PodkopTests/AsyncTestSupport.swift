import XCTest

@MainActor
extension XCTestCase {
    private enum WaitFailure: Error { case timedOut }

    /// A yield count cannot guarantee that a resumed continuation has reached the model.
    func waitUntil(_ description: String, file: StaticString = #filePath, line: UInt = #line,
                   _ condition: () -> Bool) async throws {
        let clock = ContinuousClock()
        let deadline = clock.now.advanced(by: .seconds(5))
        while !condition() {
            guard clock.now < deadline else {
                XCTFail("Timed out waiting for \(description)", file: file, line: line)
                throw WaitFailure.timedOut
            }
            try await Task.sleep(for: .milliseconds(10))
        }
    }
}

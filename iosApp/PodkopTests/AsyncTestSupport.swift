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

/// Suspends each call until the test completes it, so response order is under test control.
@MainActor
final class Pending<Value> {
    private(set) var calls: [(input: String, finish: (Result<Value, Error>) -> Void)] = []

    func wait(_ input: String) async throws -> Value {
        try await withCheckedThrowingContinuation { continuation in
            calls.append((input, { continuation.resume(with: $0) }))
        }
    }
    func succeed(_ index: Int, _ value: Value, file: StaticString = #filePath, line: UInt = #line) {
        call(index, file: file, line: line)?(.success(value))
    }
    func fail(_ index: Int, file: StaticString = #filePath, line: UInt = #line) {
        call(index, file: file, line: line)?(.failure(NSError(domain: "fixture", code: 1)))
    }

    // Out-of-range indexing would crash the test host and fail every later test in the run.
    private func call(_ index: Int, file: StaticString, line: UInt) -> ((Result<Value, Error>) -> Void)? {
        guard calls.indices.contains(index) else {
            XCTFail("Call \(index) was never made; \(calls.count) so far", file: file, line: line)
            return nil
        }
        return calls[index].finish
    }
}

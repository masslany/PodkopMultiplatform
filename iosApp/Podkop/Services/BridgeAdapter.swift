import Foundation
import PodkopShared

struct BridgeFailure: Error {
    let category: String
    let code: String?
}

final class OperationGate<Value> {
    private let lock = NSLock()
    private var continuation: CheckedContinuation<Value, Error>?
    private var cancelHandle: (() -> Void)?
    private var finished = false

    func install(_ continuation: CheckedContinuation<Value, Error>) {
        lock.lock()
        if finished {
            lock.unlock()
            continuation.resume(throwing: CancellationError())
            return
        }
        self.continuation = continuation
        lock.unlock()
    }

    func install(cancel: @escaping () -> Void) {
        lock.lock()
        let cancelled = finished
        if !cancelled { cancelHandle = cancel }
        lock.unlock()
        if cancelled { cancel() }
    }

    func complete(_ value: Value?, _ failure: IOSFailure?) {
        lock.lock()
        guard !finished else { lock.unlock(); return }
        finished = true
        let pending = continuation
        continuation = nil
        cancelHandle = nil
        lock.unlock()
        guard let pending else { return }
        if let failure {
            pending.resume(throwing: BridgeFailure(category: failure.category, code: failure.code))
        } else if let value {
            pending.resume(returning: value)
        } else {
            pending.resume(throwing: BridgeFailure(category: "unknown", code: nil))
        }
    }

    func cancel() {
        lock.lock()
        guard !finished else { lock.unlock(); return }
        finished = true
        let pending = continuation
        let handle = cancelHandle
        continuation = nil
        cancelHandle = nil
        lock.unlock()
        handle?()
        pending?.resume(throwing: CancellationError())
    }
}

final class ObservationGate {
    private let lock = NSLock()
    private var cancelHandle: (() -> Void)?
    private var cancelled = false

    func install(cancel: @escaping () -> Void) {
        lock.lock()
        let shouldCancel = cancelled
        if !shouldCancel { cancelHandle = cancel }
        lock.unlock()
        if shouldCancel { cancel() }
    }

    func cancel() {
        lock.lock()
        guard !cancelled else { lock.unlock(); return }
        cancelled = true
        let handle = cancelHandle
        cancelHandle = nil
        lock.unlock()
        handle?()
    }

    var isActive: Bool {
        lock.lock(); defer { lock.unlock() }
        return !cancelled
    }
}

/// One adapter for all Kotlin callback operations and replaceable observations.
/// Call `close()` before `PodkopClient.close()` to resolve pending Swift tasks.
final class BridgeAdapter {
    private let lock = NSLock()
    private var cancellations: [UUID: () -> Void] = [:]
    private var closed = false

    func call<Value>(_ start: @escaping (@escaping (Value?, IOSFailure?) -> Void) -> IOSOperation) async throws -> Value {
        let gate = OperationGate<Value>()
        let id = UUID()
        guard register(id: id, cancel: gate.cancel) else { throw CancellationError() }
        defer { unregister(id: id) }
        return try await withTaskCancellationHandler {
            try await withCheckedThrowingContinuation { continuation in
                gate.install(continuation)
                let handle = start { value, failure in gate.complete(value, failure) }
                gate.install(cancel: handle.cancel)
            }
        } onCancel: {
            gate.cancel()
        }
    }

    func stream<Value>(_ start: @escaping (@escaping (Value) -> Void) -> IOSObservation) -> AsyncStream<Value> {
        AsyncStream(bufferingPolicy: .bufferingNewest(1)) { continuation in
            let gate = ObservationGate()
            let id = UUID()
            guard register(id: id, cancel: {
                gate.cancel()
                continuation.finish()
            }) else {
                continuation.finish()
                return
            }
            continuation.onTermination = { [weak self] _ in
                gate.cancel()
                self?.unregister(id: id)
            }
            let handle = start { value in
                if gate.isActive { continuation.yield(value) }
            }
            gate.install(cancel: handle.cancel)
        }
    }

    func close() {
        lock.lock()
        guard !closed else { lock.unlock(); return }
        closed = true
        let actions = Array(cancellations.values)
        cancellations.removeAll()
        lock.unlock()
        actions.forEach { $0() }
    }

    private func register(id: UUID, cancel: @escaping () -> Void) -> Bool {
        lock.lock(); defer { lock.unlock() }
        guard !closed else { return false }
        cancellations[id] = cancel
        return true
    }

    private func unregister(id: UUID) {
        lock.lock(); defer { lock.unlock() }
        cancellations.removeValue(forKey: id)
    }
}

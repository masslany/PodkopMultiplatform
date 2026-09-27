import Foundation
import OSLog

/// Timing along the vote path (tap, server call, reload, what the row shows), to find why
/// votes on link comments react late. Debug builds only; lines start with "[VoteTrace]" and
/// show the milliseconds since the tap on that resource.
@MainActor
enum VoteTrace {
    #if DEBUG
    private static let logger = Logger(subsystem: "pl.masslany.podkop", category: "VoteTrace")
    private static let clock = ContinuousClock()
    private static var starts: [String: ContinuousClock.Instant] = [:]
    private static var lastKey: String?
    #endif

    static func key(_ resource: Resource) -> String { "\(resource.kind.rawValue):\(resource.sourceID)" }

    /// Starts the clock for one resource.
    static func begin(_ key: String, _ message: String) {
        #if DEBUG
        starts[key] = clock.now
        lastKey = key
        emit(key, message)
        #endif
    }

    /// Logs against a resource's clock, or the most recent tap's when `key` is nil.
    static func log(_ key: String?, _ message: String) {
        #if DEBUG
        emit(key ?? lastKey, message)
        #endif
    }

    #if DEBUG
    private static func emit(_ key: String?, _ message: String) {
        let elapsed = key.flatMap { starts[$0] }.map { start -> String in
            let ms = (clock.now - start).components
            return String(format: "+%5.0fms", Double(ms.seconds) * 1000 + Double(ms.attoseconds) / 1e15)
        } ?? "       "
        let line = "[VoteTrace] \(elapsed) \(key ?? "-") \(message)"
        logger.debug("\(line, privacy: .public)")
        print(line)
    }
    #endif
}

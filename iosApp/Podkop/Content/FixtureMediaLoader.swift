import SwiftUI

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

import UIKit
import WebKit

/// Rasterizes small SVG icons (Wykop achievement badges) to images. UIKit cannot decode SVG,
/// so one hidden, script-free WebKit view draws each icon once; results are cached by key and
/// concurrent requests for the same icon share one render.
@MainActor
final class SVGImageRenderer: NSObject, WKNavigationDelegate {
    static let shared = SVGImageRenderer()

    private let cache = NSCache<NSString, UIImage>()
    private var inFlight: [String: Task<UIImage?, Never>] = [:]
    private var queue: Task<Void, Never>?
    private var navigation: CheckedContinuation<Void, Never>?

    private lazy var webView: WKWebView = {
        let configuration = WKWebViewConfiguration()
        configuration.websiteDataStore = .nonPersistent()
        configuration.defaultWebpagePreferences.allowsContentJavaScript = false
        let view = WKWebView(frame: CGRect(x: 0, y: 0, width: 96, height: 96), configuration: configuration)
        view.isOpaque = false
        view.backgroundColor = .clear
        view.scrollView.backgroundColor = .clear
        view.navigationDelegate = self
        return view
    }()

    func image(svg data: Data, key: String, pointSize: CGFloat) async -> UIImage? {
        let cacheKey = "\(key)@\(Int(pointSize))" as NSString
        if let cached = cache.object(forKey: cacheKey) { return cached }
        if let running = inFlight[cacheKey as String] { return await running.value }
        // Renders run one at a time on the shared view.
        let previous = queue
        let task = Task { [weak self] () -> UIImage? in
            await previous?.value
            return await self?.render(data, pointSize: pointSize)
        }
        inFlight[cacheKey as String] = task
        queue = Task { _ = await task.value }
        let image = await task.value
        inFlight[cacheKey as String] = nil
        if let image { cache.setObject(image, forKey: cacheKey) }
        return image
    }

    private func render(_ data: Data, pointSize: CGFloat) async -> UIImage? {
        guard data.count < 256 * 1024 else { return nil }
        webView.frame = CGRect(x: 0, y: 0, width: pointSize, height: pointSize)
        let html = """
        <html><head><meta name="viewport" content="width=\(Int(pointSize)),initial-scale=1">
        <style>html,body{margin:0;padding:0;background:transparent;overflow:hidden}
        img{display:block;width:\(Int(pointSize))px;height:\(Int(pointSize))px;object-fit:contain}</style></head>
        <body><img src="data:image/svg+xml;base64,\(data.base64EncodedString())"></body></html>
        """
        await withCheckedContinuation { continuation in
            navigation = continuation
            webView.loadHTMLString(html, baseURL: nil)
        }
        let configuration = WKSnapshotConfiguration()
        configuration.rect = CGRect(x: 0, y: 0, width: pointSize, height: pointSize)
        configuration.afterScreenUpdates = true
        return try? await webView.takeSnapshot(configuration: configuration)
    }

    private func finishNavigation() {
        navigation?.resume()
        navigation = nil
    }

    nonisolated func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        MainActor.assumeIsolated { finishNavigation() }
    }

    nonisolated func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
        MainActor.assumeIsolated { finishNavigation() }
    }

    nonisolated func webView(_ webView: WKWebView, didFailProvisionalNavigation navigation: WKNavigation!,
                             withError error: Error) {
        MainActor.assumeIsolated { finishNavigation() }
    }
}

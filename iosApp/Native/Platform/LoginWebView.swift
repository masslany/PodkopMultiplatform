import SwiftUI
import WebKit

/// Embedded Wykop Connect page (owner decision D06). The redirect to the app's own host is never
/// loaded: it is cancelled and handed to `onCallback`, which passes it to the shared parser that
/// stores the tokens. A non-persistent data store keeps no Wykop cookies on the device.
struct LoginWebView: UIViewRepresentable {
    let url: URL
    let isCallback: (URL) -> Bool
    let onCallback: (URL) -> Void
    let onLoadingChange: (Bool) -> Void
    let onFailure: () -> Void

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    func makeUIView(context: Context) -> WKWebView {
        let configuration = WKWebViewConfiguration()
        configuration.websiteDataStore = .nonPersistent()
        let view = WKWebView(frame: .zero, configuration: configuration)
        view.navigationDelegate = context.coordinator
        view.uiDelegate = context.coordinator
        view.accessibilityIdentifier = "loginWebView"
        view.load(URLRequest(url: url))
        return view
    }

    func updateUIView(_ view: WKWebView, context: Context) {
        context.coordinator.parent = self
    }

    static func dismantleUIView(_ view: WKWebView, coordinator: Coordinator) {
        view.stopLoading()
        view.navigationDelegate = nil
        view.uiDelegate = nil
    }

    final class Coordinator: NSObject, WKNavigationDelegate, WKUIDelegate {
        var parent: LoginWebView
        private var delivered = false

        init(_ parent: LoginWebView) { self.parent = parent }

        func webView(_ webView: WKWebView, decidePolicyFor action: WKNavigationAction,
                     decisionHandler: @escaping @MainActor (WKNavigationActionPolicy) -> Void) {
            // Server redirects also pass through here, so the callback never reaches the network.
            guard let url = action.request.url, parent.isCallback(url) else {
                decisionHandler(.allow)
                return
            }
            decisionHandler(.cancel)
            guard !delivered else { return }
            delivered = true
            parent.onCallback(url)
        }

        /// Links that ask for a new window stay in this page.
        func webView(_ webView: WKWebView, createWebViewWith configuration: WKWebViewConfiguration,
                     for action: WKNavigationAction, windowFeatures: WKWindowFeatures) -> WKWebView? {
            if action.targetFrame == nil, let url = action.request.url {
                if parent.isCallback(url) {
                    if !delivered { delivered = true; parent.onCallback(url) }
                } else {
                    webView.load(URLRequest(url: url))
                }
            }
            return nil
        }

        func webView(_ webView: WKWebView, didStartProvisionalNavigation navigation: WKNavigation!) {
            parent.onLoadingChange(true)
        }

        func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
            parent.onLoadingChange(false)
        }

        func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
            fail(error)
        }

        func webView(_ webView: WKWebView, didFailProvisionalNavigation navigation: WKNavigation!,
                     withError error: Error) {
            fail(error)
        }

        private func fail(_ error: Error) {
            parent.onLoadingChange(false)
            // A cancelled load (including the intercepted callback) is not a failure.
            if (error as NSError).code == NSURLErrorCancelled || delivered { return }
            if (error as NSError).domain == "WebKitErrorDomain" && (error as NSError).code == 102 { return }
            parent.onFailure()
        }
    }
}

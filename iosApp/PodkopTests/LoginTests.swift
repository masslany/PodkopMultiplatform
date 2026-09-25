import WebKit
import XCTest
@testable import Podkop

/// Navigation action with a caller-chosen request; WebKit offers no public initializer.
private final class StubNavigationAction: WKNavigationAction {
    private let stubRequest: URLRequest
    init(_ url: String) { stubRequest = URLRequest(url: URL(string: url)!) }
    override var request: URLRequest { stubRequest }
}

@MainActor
final class LoginTests: XCTestCase {
    func testAppHostRedirectIsCancelledAndDeliveredOnce() {
        var delivered: [URL] = []
        let view = LoginWebView(
            url: URL(string: "https://wykop.pl/connect")!,
            isCallback: { $0.host == "masslany.pl" },
            onCallback: { delivered.append($0) },
            onLoadingChange: { _ in },
            onFailure: {}
        )
        let coordinator = view.makeCoordinator()
        let webView = WKWebView()
        var decisions: [WKNavigationActionPolicy] = []

        coordinator.webView(webView, decidePolicyFor: StubNavigationAction("https://wykop.pl/login")) {
            decisions.append($0)
        }
        let callback = "https://masslany.pl/wykop/connect?token=a&rtoken=b"
        coordinator.webView(webView, decidePolicyFor: StubNavigationAction(callback)) { decisions.append($0) }
        coordinator.webView(webView, decidePolicyFor: StubNavigationAction(callback)) { decisions.append($0) }

        XCTAssertEqual(decisions, [.allow, .cancel, .cancel], "the callback never loads")
        XCTAssertEqual(delivered.map(\.absoluteString), [callback], "delivered to the parser once")
    }

    func testCancelledLoadAfterCallbackIsNotAFailure() {
        var failures = 0
        let view = LoginWebView(url: URL(string: "https://wykop.pl/connect")!, isCallback: { _ in true },
                                onCallback: { _ in }, onLoadingChange: { _ in }, onFailure: { failures += 1 })
        let coordinator = view.makeCoordinator()
        let webView = WKWebView()
        coordinator.webView(webView, didFailProvisionalNavigation: nil,
                            withError: NSError(domain: NSURLErrorDomain, code: NSURLErrorCancelled))
        XCTAssertEqual(failures, 0)
        coordinator.webView(webView, didFailProvisionalNavigation: nil,
                            withError: NSError(domain: NSURLErrorDomain, code: NSURLErrorNotConnectedToInternet))
        XCTAssertEqual(failures, 1, "a real network failure is reported")
    }
}

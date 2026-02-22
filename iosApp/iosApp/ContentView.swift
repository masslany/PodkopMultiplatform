import UIKit
import SwiftUI
import ComposeApp

final class IOSStartupViewModel: ObservableObject {
    @Published var startupState: IOSAppStartupState = .initializing

    let viewControllerHolder: IOSViewControllerHolder

    private let helper = IOSDependencyHelper()
    private var key = ""
    private var secret = ""
    private var started = false

    init() {
        self.viewControllerHolder = helper.viewControllerHolder()
    }

    func startIfNeeded(key: String, secret: String) {
        self.key = key
        self.secret = secret

        guard !started else { return }
        started = true

        helper.startAndObserveStartupState(
            key: key,
            secret: secret,
            onStateChanged: { [weak self] state in
                self?.startupState = state
            }
        )
    }

    func retry() {
        helper.start(key: key, secret: secret)
    }

    func handleIncomingUrl(_ url: URL) {
        helper.handleDeepLink(url: url.absoluteString)
    }

    deinit {
        helper.stopObservingStartupState()
    }
}

struct ComposeView: UIViewControllerRepresentable {
    let viewControllerHolder: IOSViewControllerHolder

    func makeUIViewController(context: Context) -> UIViewController {
        let controller = MainViewControllerKt.MainViewController()
        controller.view.backgroundColor = .systemBackground

        viewControllerHolder.provider = { controller }

        return controller
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    @StateObject private var startupViewModel = IOSStartupViewModel()

    private var key: String {
        Bundle.main.object(forInfoDictionaryKey: "WYKOP_KEY") as? String ?? ""
    }

    private var secret: String {
        Bundle.main.object(forInfoDictionaryKey: "WYKOP_SECRET") as? String ?? ""
    }

    var body: some View {
        Group {
            switch startupViewModel.startupState {
            case .ready:
                ComposeView(viewControllerHolder: startupViewModel.viewControllerHolder)
                    .ignoresSafeArea()
            case .error:
                VStack(spacing: 16) {}
            default:
                VStack(spacing: 16) {}
            }
        }
        .onAppear {
            startupViewModel.startIfNeeded(key: key, secret: secret)
        }
        .onOpenURL { url in
            startupViewModel.handleIncomingUrl(url)
        }
        .onContinueUserActivity(NSUserActivityTypeBrowsingWeb) { userActivity in
            guard let url = userActivity.webpageURL else { return }
            startupViewModel.handleIncomingUrl(url)
        }
    }
}

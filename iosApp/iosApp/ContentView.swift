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
        startupState = .initializing
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

    private let credentials = IOSAppCredentials()

    var body: some View {
        Group {
            if let credentials {
                switch startupViewModel.startupState {
                case .ready:
                    ComposeView(viewControllerHolder: startupViewModel.viewControllerHolder)
                        .ignoresSafeArea()
                case .error:
                    StartupErrorView(
                        onRetry: startupViewModel.retry
                    )
                default:
                    StartupLoadingView()
                }
            }
        }
        .onAppear {
            guard let credentials else { return }
            startupViewModel.startIfNeeded(key: credentials.key, secret: credentials.secret)
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

private struct IOSAppCredentials {
    let key: String
    let secret: String

    init?(bundle: Bundle = .main) {
        guard
            let key = Self.readValue(for: "WYKOP_KEY", from: bundle),
            let secret = Self.readValue(for: "WYKOP_SECRET", from: bundle)
        else {
            return nil
        }

        self.key = key
        self.secret = secret
    }

    private static func readValue(for key: String, from bundle: Bundle) -> String? {
        let rawValue = bundle.object(forInfoDictionaryKey: key) as? String
        let trimmedValue = rawValue?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        guard !trimmedValue.isEmpty, trimmedValue != "$(\(key))" else {
            return nil
        }
        return trimmedValue
    }
}

private struct StartupLoadingView: View {
    var body: some View {
        VStack(spacing: 16) {
            ProgressView()
                .controlSize(.large)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .padding(.horizontal, 24)
    }
}

private struct StartupErrorView: View {
    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Text("Wystąpił problem podczas uruchamiania aplikacji")
                .font(.headline)
                .multilineTextAlignment(.center)

            Text("Upewnij się, że masz połączenie z internetem i spróbuj jeszcze raz.")
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)

            Button("Spróbuj ponownie", action: onRetry)
                .buttonStyle(.borderedProminent)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .padding(.horizontal, 24)
    }
}

import Foundation
import Observation
import PodkopShared

@MainActor
final class AppDependencies {
    static let shared = AppDependencies()
    let client = PodkopClient.companion.create()
    let adapter = BridgeAdapter()
    let router = AppRouter()
    lazy var ingress = LinkIngress(router: router)
    lazy var session = SessionModel(dependencies: self)
    let sceneActivity = SceneActivity()
    lazy var feedLoader: FeedLoading = {
        #if DEBUG
        if isFixture { return FixtureFeedLoader() }
        #endif
        return SharedFeedLoader(client: client, adapter: adapter)
    }()
    private var isFixture: Bool {
        #if DEBUG
        ProcessInfo.processInfo.arguments.contains("-nativeFixture")
        #else
        false
        #endif
    }
    var isForeground: Bool { sceneActivity.isForeground }

    private init() {}

    func scene(_ id: UUID, active: Bool) {
        sceneActivity.set(id, active: active)
        updatePolling()
    }

    func updatePolling() {
        if !isFixture && isForeground && session.phase == .ready {
            client.notifications.startPolling()
        } else {
            client.notifications.stopPolling()
        }
    }

    func close() {
        client.notifications.stopPolling()
        adapter.close()
        client.close()
    }

    func loadTweet(_ url: String) async throws -> NativeTweetPreview {
        let value: IOSTweetPreview = try await adapter.call {
            self.client.embeds.twitterPreview(url: url, completion: $0)
        }
        return NativeTweetPreview(value)
    }
}

@MainActor @Observable
final class SceneActivity {
    private var activeScenes = Set<UUID>()
    var isForeground: Bool { !activeScenes.isEmpty }
    var activeCount: Int { activeScenes.count }

    func set(_ id: UUID, active: Bool) {
        if active { activeScenes.insert(id) } else { activeScenes.remove(id) }
    }
}

@MainActor @Observable
final class SessionModel {
    enum Phase { case initializing, ready, error, missingConfiguration }
    var phase: Phase = .initializing
    var isLoggedIn = false
    var unreadCount = 0
    var autoplayGifs = true
    var banner: String?
    var loginURL: URL?
    private unowned let dependencies: AppDependencies
    private var started = false
    private var observationTasks: [Task<Void, Never>] = []

    init(dependencies: AppDependencies) { self.dependencies = dependencies }

    func startIfNeeded() {
        guard !started else { return }
        started = true
        #if DEBUG
        let arguments = ProcessInfo.processInfo.arguments
        if let marker = arguments.firstIndex(of: "-nativeFixture"),
           arguments.indices.contains(marker + 1) {
            switch arguments[marker + 1] {
            case "guest", "authenticated", "content":
                dependencies.router.paths = [:]
                dependencies.router.selectedTab = .links
                phase = .ready
                isLoggedIn = arguments[marker + 1] == "authenticated"
                dependencies.router.applySession(isLoggedIn: isLoggedIn, revision: 0)
                dependencies.ingress.ready = true
                dependencies.updatePolling()
            case "retry", "retryLink":
                phase = .error
                if arguments[marker + 1] == "retryLink" {
                    dependencies.ingress.accept(kind: "link", id: 21)
                }
            case "missing": phase = .missingConfiguration
            default: break
            }
            return
        }
        #endif
        let client = dependencies.client
        let adapter = dependencies.adapter
        observationTasks.append(Task {
            for await value in adapter.stream({ client.startup.observe(onChange: $0) }) {
                switch value.phase {
                case "ready":
                    phase = .ready
                    dependencies.ingress.ready = true
                    dependencies.updatePolling()
                case "error": phase = .error
                default: if phase != .missingConfiguration { phase = .initializing }
                }
            }
        })
        observationTasks.append(Task {
            for await value in adapter.stream({ client.session.observe(onChange: $0) }) {
                isLoggedIn = value.isLoggedIn
                dependencies.router.applySession(isLoggedIn: value.isLoggedIn, revision: Int(value.revision))
            }
        })
        observationTasks.append(Task {
            for await value in adapter.stream({ client.notifications.observeStatus(onChange: $0) }) {
                unreadCount = Int(value.totalUnreadCount)
            }
        })
        observationTasks.append(Task {
            for await value in adapter.stream({ client.settings.observe(onChange: $0) }) {
                autoplayGifs = value.autoplayGifs
            }
        })
        Task { await start() }
    }

    func start() async {
        guard let key = Bundle.main.object(forInfoDictionaryKey: "WYKOP_KEY") as? String,
              let secret = Bundle.main.object(forInfoDictionaryKey: "WYKOP_SECRET") as? String,
              !key.isEmpty, !secret.isEmpty,
              !key.hasPrefix("$("), !secret.hasPrefix("$(") else {
            phase = .missingConfiguration
            return
        }
        do {
            let _: IOSSuccess = try await dependencies.adapter.call {
                self.dependencies.client.startup.start(key: key, secret: secret, completion: $0)
            }
        } catch is CancellationError {
            return
        } catch {
            phase = .error
            banner = String(localized: "Startup failed. Try again.")
        }
    }

    func retry() async {
        #if DEBUG
        let arguments = ProcessInfo.processInfo.arguments
        if arguments.contains("-nativeFixture"),
           (arguments.contains("retry") || arguments.contains("retryLink")) {
            phase = .ready
            dependencies.ingress.ready = true
            dependencies.updatePolling()
            return
        }
        #endif
        guard phase != .missingConfiguration else { await start(); return }
        phase = .initializing
        do {
            let _: IOSSuccess = try await dependencies.adapter.call {
                self.dependencies.client.startup.retry(completion: $0)
            }
        } catch is CancellationError {
            return
        } catch {
            phase = .error
            banner = String(localized: "Startup failed. Try again.")
        }
    }

    func beginLogin() async {
        do {
            let raw: String = try await dependencies.adapter.call {
                self.dependencies.client.session.loginUrl(completion: $0)
            }
            guard let url = URL(string: raw), url.scheme == "https" else {
                banner = String(localized: "Unable to open sign in.")
                return
            }
            loginURL = url
        } catch is CancellationError {
            return
        } catch {
            banner = String(localized: "Unable to open sign in.")
        }
    }

    func logout() async {
        #if DEBUG
        if ProcessInfo.processInfo.arguments.contains("-nativeFixture") {
            isLoggedIn = false
            dependencies.router.applySession(isLoggedIn: false, revision: 1)
            return
        }
        #endif
        do {
            let _: IOSSuccess = try await dependencies.adapter.call {
                self.dependencies.client.session.logout(completion: $0)
            }
        } catch is CancellationError {
            return
        } catch {
            banner = String(localized: "Sign out failed. Please retry.")
        }
        // The shared session observation is the source of truth even on a partial failure.
    }

    func accept(_ url: URL) async {
        let ingress = dependencies.ingress
        guard ingress.begin(url) else { return }
        defer { ingress.end(url) }
        do {
            let intent: IOSLinkIntent = try await dependencies.adapter.call {
                self.dependencies.client.session.acceptUrl(url: url.absoluteString, completion: $0)
            }
            ingress.accept(kind: intent.kind, id: intent.id?.intValue)
            if intent.kind == "login" { dependencies.router.dismissSheet() }
        } catch is CancellationError {
            return
        } catch {
            banner = String(localized: "This link cannot be opened.")
        }
    }

    func stop() {
        observationTasks.forEach { $0.cancel() }
        observationTasks.removeAll()
    }
}

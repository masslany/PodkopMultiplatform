import Foundation
import Observation
import PodkopShared

@MainActor @Observable
final class SessionModel: SettingsState {
    enum Phase { case initializing, ready, error, missingConfiguration }
    var phase: Phase = .initializing
    var isLoggedIn = false {
        didSet {
            guard isLoggedIn != oldValue else { return }
            refreshUsername()
            refreshAdultContent(clearing: true)
            if !isLoggedIn { dependencies.messageNotifications.loggedOut() }
        }
    }
    var revision = 0 {
        didSet {
            guard revision != oldValue else { return }
            refreshUsername()
            refreshAdultContent(clearing: true)
        }
    }
    /// The signed-in user's name, used to highlight their own comments; nil while unknown.
    private(set) var username: String?
    private var usernameTask: Task<Void, Never>?
    /// Wykop's "+18" account setting, which can only be changed on wykop.pl. App Store guideline
    /// 1.2 allows adult content only when the user turned it on there, so it stays off for guests
    /// and until the account confirms it.
    private(set) var adultContentAllowed = false
    private var adultContentTask: Task<Void, Never>?
    var unreadCount = 0
    var notificationCounts = NotificationCounts()
    var autoplayGifs = true
    var playVideosInline = false
    var threadedEntryComments = false
    var theme: ThemeChoice = .auto
    var banner: String?
    var loginURL: URL?
    private(set) var loginPending = false
    private unowned let dependencies: AppDependencies
    private var started = false
    private var observationTasks: [Task<Void, Never>] = []

    init(dependencies: AppDependencies) { self.dependencies = dependencies }

    /// Another account's name must never stay behind, so it is cleared before each lookup.
    private func refreshUsername() {
        usernameTask?.cancel()
        username = nil
        guard isLoggedIn else { return }
        let loader = dependencies.profileLoader
        usernameTask = Task { [weak self] in
            guard let name = try? await loader.ownUsername(), !Task.isCancelled else { return }
            self?.username = name
        }
    }

    /// Re-reads the account's +18 setting; `clearing` hides adult content during the lookup, as
    /// another account's preference must never stay behind.
    func refreshAdultContent(clearing: Bool = false) {
        adultContentTask?.cancel()
        #if DEBUG
        if ProcessInfo.processInfo.arguments.contains("-uiFixture") {
            adultContentAllowed = ProcessInfo.processInfo.arguments.contains("-adultContentAllowed")
            return
        }
        #endif
        if clearing || !isLoggedIn { adultContentAllowed = false }
        guard isLoggedIn else { return }
        let client = dependencies.client
        let adapter = dependencies.adapter
        adultContentTask = Task { [weak self] in
            let settings: IOSAccountContentSettings? = try? await adapter.call {
                client.accountSettings.contentSettings(completion: $0)
            }
            guard !Task.isCancelled else { return }
            self?.adultContentAllowed = settings?.showAdult ?? false
        }
    }

    func startIfNeeded() {
        guard !started else { return }
        started = true
        #if DEBUG
        let arguments = ProcessInfo.processInfo.arguments
        if let marker = arguments.firstIndex(of: "-uiFixture"),
           arguments.indices.contains(marker + 1) {
            if arguments.contains("-contentTerms") {
                UserDefaults.standard.removeObject(forKey: ContentTermsView.storageKey)
            }
            switch arguments[marker + 1] {
            case "guest", "authenticated", "content":
                dependencies.router.paths = [:]
                dependencies.router.selectedTab = .links
                phase = .ready
                isLoggedIn = arguments[marker + 1] == "authenticated"
                revision = 0
                dependencies.router.applySession(isLoggedIn: isLoggedIn, revision: 0)
                refreshAdultContent()
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
                    refreshAdultContent()
                case "error": phase = .error
                default: if phase != .missingConfiguration { phase = .initializing }
                }
            }
        })
        observationTasks.append(Task {
            for await value in adapter.stream({ client.session.observe(onChange: $0) }) {
                isLoggedIn = value.isLoggedIn
                revision = Int(value.revision)
                dependencies.resourceUpdates.reset(for: revision)
                dependencies.router.applySession(isLoggedIn: value.isLoggedIn, revision: Int(value.revision))
                // Android refreshes counts for a signed-in session and clears them otherwise.
                if value.revision > 0 {
                    let _: IOSSuccess? = try? await adapter.call {
                        client.notifications.onSessionChanged(completion: $0)
                    }
                }
            }
        })
        observationTasks.append(Task {
            for await value in adapter.stream({ client.notifications.observeStatus(onChange: $0) }) {
                unreadCount = Int(value.totalUnreadCount)
                notificationCounts = NotificationCounts(
                    entries: Int(value.entriesUnreadCount), pm: Int(value.privateMessagesUnreadCount),
                    tags: Int(value.tagsUnreadCount),
                    observedDiscussions: Int(value.observedDiscussionsUnreadCount))
                dependencies.messageNotifications.observed(unread: notificationCounts.pm)
            }
        })
        observationTasks.append(Task {
            for await value in adapter.stream({ client.settings.observe(onChange: $0) }) {
                autoplayGifs = value.autoplayGifs
                playVideosInline = value.playVideosInline
                threadedEntryComments = value.threadedEntryComments
                theme = ThemeChoice(rawValue: value.themeOverride) ?? .auto
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
            banner = String(localized: .appStartupFailedTryAgain)
        }
    }

    func retry() async {
        #if DEBUG
        let arguments = ProcessInfo.processInfo.arguments
        if arguments.contains("-uiFixture"),
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
            banner = String(localized: .appStartupFailedTryAgain)
        }
    }

    func beginLogin() async {
        guard !loginPending else { return }
        loginPending = true
        defer { loginPending = false }
        do {
            let raw: String = try await dependencies.adapter.call {
                self.dependencies.client.session.loginUrl(completion: $0)
            }
            guard let url = URL(string: raw), url.scheme == "https" else {
                banner = String(localized: .appUnableOpenSignIn)
                return
            }
            loginURL = url
        } catch is CancellationError {
            return
        } catch {
            banner = String(localized: .appUnableOpenSignIn)
        }
    }

    /// Asks the shared parser whether the embedded login page must stop at this URL.
    func isAppURL(_ url: URL) -> Bool {
        dependencies.client.session.isAppUrl(url: url.absoluteString)
    }

    /// Hands the intercepted login redirect to the shared parser, which stores the tokens.
    func completeLogin(_ url: URL) async {
        await accept(url)
        if !isLoggedIn, dependencies.router.sheet == .login {
            loginURL = nil
            banner = String(localized: .appSignInFailedTryAgain)
        }
    }

    func logout() async {
        #if DEBUG
        if ProcessInfo.processInfo.arguments.contains("-uiFixture") {
            isLoggedIn = false
            revision += 1
            dependencies.resourceUpdates.reset(for: revision)
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
            banner = String(localized: .appSignOutFailedRetry)
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
            banner = String(localized: .appLinkCannotOpened)
        }
    }

    func stop() {
        observationTasks.forEach { $0.cancel() }
        observationTasks.removeAll()
    }
}

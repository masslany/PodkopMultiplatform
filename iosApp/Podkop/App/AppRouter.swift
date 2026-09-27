import Foundation
import Observation

private struct NavigationSnapshot: Codable {
    let version: Int
    let tab: AppTab
    let paths: [AppTab: [AppRoute]]
}

@MainActor @Observable
final class AppRouter {
    var selectedTab: AppTab = .links { didSet { persist() } }
    var paths: [AppTab: [AppRoute]] = [:] { didSet { persist() } }
    var sheet: AppSheet?
    var alert: AppAlert?
    var banner: String?
    var pendingAccountRoute: AppRoute?
    var composerIntent: ComposerIntent?
    var composerSeed: Resource?
    var pendingComposerIntent: ComposerIntent?
    var isLoggedIn = false {
        didSet {
            if !isLoggedIn { clearAccountRoutes() }
            else if let pendingAccountRoute {
                self.pendingAccountRoute = nil
                navigate(pendingAccountRoute)
            }
        }
    }
    private var sessionRevision: Int?
    private let storage: UserDefaults?
    private let storageKey = "podkop.navigation.v1"

    init(storage: UserDefaults? = .standard) {
        self.storage = storage
        if let data = storage?.data(forKey: storageKey) { restore(data) }
    }

    func navigate(_ route: AppRoute, in tab: AppTab? = nil) {
        if route.needsAccount && !isLoggedIn {
            pendingAccountRoute = route
            sheet = .login
            return
        }
        let target = tab ?? (route == .messages || route == .notifications ? .more : selectedTab)
        selectedTab = target
        paths[target, default: []].append(route)
    }

    func replacePath(_ path: [AppRoute], for tab: AppTab) {
        paths[tab] = path
    }

    func clearAccountRoutes() {
        paths = paths.mapValues { $0.filter { !$0.needsAccount } }
        pendingAccountRoute = nil
        pendingComposerIntent = nil
        composerIntent = nil
        composerSeed = nil
        if sheet == .composer { sheet = nil }
    }

    func applySession(isLoggedIn: Bool, revision: Int) {
        let waitingRoute = isLoggedIn ? pendingAccountRoute : nil
        let waitingComposer = isLoggedIn ? pendingComposerIntent : nil
        if let sessionRevision, sessionRevision != revision { clearAccountRoutes() }
        sessionRevision = revision
        if isLoggedIn { pendingAccountRoute = waitingRoute }
        self.isLoggedIn = isLoggedIn
        if isLoggedIn, let waitingComposer {
            self.pendingComposerIntent = nil
            composerIntent = waitingComposer
            sheet = .composer
        }
    }

    func presentComposer(_ intent: ComposerIntent, seed: Resource? = nil) {
        if isLoggedIn {
            composerIntent = intent
            composerSeed = seed
            sheet = .composer
        } else {
            pendingComposerIntent = intent
            sheet = .login
        }
    }

    func dismissSheet() {
        sheet = nil
        pendingAccountRoute = nil
        pendingComposerIntent = nil
        composerIntent = nil
        composerSeed = nil
    }

    func detail(for tab: AppTab) -> AppRoute? {
        paths[tab]?.last
    }

    func restore(_ data: Data) {
        guard let snapshot = try? JSONDecoder().decode(NavigationSnapshot.self, from: data),
              snapshot.version == 1,
              snapshot.paths.values.allSatisfy({ $0.count <= 32 }) else { return }
        selectedTab = snapshot.tab
        paths = snapshot.paths.mapValues { $0.filter { !$0.needsAccount } }
    }

    func snapshot() -> Data? {
        try? JSONEncoder().encode(NavigationSnapshot(
            version: 1, tab: selectedTab,
            paths: paths.mapValues { $0.filter { !$0.needsAccount } }
        ))
    }

    private func persist() {
        guard let storage else { return }
        storage.set(snapshot(), forKey: storageKey)
    }
}

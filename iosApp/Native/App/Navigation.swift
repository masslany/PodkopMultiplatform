import Foundation
import Observation

enum AppTab: String, CaseIterable, Codable, Identifiable {
    case links, upcoming, entries, more
    var id: String { rawValue }

    var title: String {
        switch self {
        case .links: String(localized: "Links")
        case .entries: String(localized: "Entries")
        case .upcoming: String(localized: "Upcoming")
        case .more: String(localized: "More")
        }
    }

    var symbol: String {
        switch self {
        case .links: "link"
        case .entries: "text.bubble"
        case .upcoming: "clock"
        case .more: "line.3.horizontal"
        }
    }
}

enum AppRoute: Hashable, Codable {
    case link(Int)
    case entry(Int)
    case messages
    case notifications
    case search
    case tags
    case tag(String)
    case profile
    case user(String)
    case settings
    case favorites
    case observed
    case hits
    case rank
    case about

    var needsAccount: Bool {
        switch self {
        case .messages, .notifications, .profile, .favorites, .observed: true
        default: false
        }
    }
}

enum AppSheet: String, Identifiable {
    case login, composer
    var id: String { rawValue }
}

struct AppAlert: Identifiable {
    let id = UUID()
    let title: String
    let message: String
}

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
    private let storageKey = "native.navigation.v1"

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
        if sheet == .composer { sheet = nil }
    }

    func applySession(isLoggedIn: Bool, revision: Int) {
        if let sessionRevision, sessionRevision != revision { clearAccountRoutes() }
        sessionRevision = revision
        self.isLoggedIn = isLoggedIn
    }

    func dismissSheet() {
        sheet = nil
        pendingAccountRoute = nil
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

@MainActor
final class LinkIngress {
    private let router: AppRouter
    private var pending: [AppRoute] = []
    private var activeURLs = Set<String>()
    private var recentURLs: [String: Date] = [:]
    var ready = false { didSet { if ready { flush() } } }

    init(router: AppRouter) { self.router = router }

    func begin(_ url: URL, now: Date = Date()) -> Bool {
        recentURLs = recentURLs.filter { now.timeIntervalSince($0.value) < 3 }
        let key = url.absoluteString
        guard !activeURLs.contains(key), recentURLs[key] == nil else { return false }
        activeURLs.insert(key)
        recentURLs[key] = now
        return true
    }
    func end(_ url: URL) { activeURLs.remove(url.absoluteString) }

    func accept(kind: String, id: Int?) {
        let route: AppRoute?
        switch kind {
        case "link": route = id.map(AppRoute.link)
        case "entry": route = id.map(AppRoute.entry)
        case "messages": route = .messages
        case "login": route = nil
        default: route = nil
        }
        guard let route else { return }
        if ready { router.navigate(route) } else { pending.append(route) }
    }

    func flush() {
        guard ready else { return }
        pending.forEach { router.navigate($0) }
        pending.removeAll()
    }
}

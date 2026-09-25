import SwiftUI
import PodkopShared
#if DEBUG
import UserNotifications
#endif

@main
struct PodkopNativeApp: App {
    #if DEBUG
    init() { UNUserNotificationCenter.current().delegate = DebugNotificationDelegate.shared }
    #endif

    var body: some Scene {
        WindowGroup { NativeRoot(dependencies: .shared) }
    }
}

#if DEBUG
private final class DebugNotificationDelegate: NSObject, UNUserNotificationCenterDelegate {
    static let shared = DebugNotificationDelegate()

    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification) async -> UNNotificationPresentationOptions {
        [.banner, .sound]
    }
}
#endif

private struct NativeRoot: View {
    let dependencies: AppDependencies
    @Environment(\.scenePhase) private var scenePhase
    @Environment(\.openURL) private var openURL
    @State private var sceneID = UUID()
    @State private var externalPage: ExternalPage?

    private var session: SessionModel { dependencies.session }
    private var router: AppRouter { dependencies.router }

    var body: some View {
        Group {
            switch session.phase {
            case .initializing:
                NativeSplashView()
                    .transition(.opacity)
            case .missingConfiguration:
                startupProblem(
                    title: String(localized: "Configuration required"),
                    explanation: String(localized: "Add the app configuration and try again.")
                )
            case .error:
                startupProblem(
                    title: String(localized: "Could not start Podkop"),
                    explanation: String(localized: "Check your connection and try again.")
                )
            case .ready:
                #if DEBUG
                if ProcessInfo.processInfo.arguments.contains("content") {
                    ContentFixtureGallery()
                } else {
                    tabs
                }
                #else
                tabs
                #endif
            }
        }
        .environment(\.mediaLoader, dependencies.mediaLoader)
        .environment(\.openURL, OpenURLAction { url in
            guard ["http", "https"].contains(url.scheme?.lowercased() ?? "") else {
                return .systemAction(url)
            }
            externalPage = ExternalPage(url: url)
            return .handled
        })
        .preferredColorScheme(session.theme.colorScheme)
        .animation(.easeOut(duration: 0.25), value: session.phase)
        .task { session.startIfNeeded() }
        .onChange(of: scenePhase, initial: true) { _, phase in
            dependencies.scene(sceneID, active: phase == .active)
        }
        .onDisappear { dependencies.scene(sceneID, active: false) }
        .onOpenURL { url in Task { await session.accept(url) } }
        .onContinueUserActivity(NSUserActivityTypeBrowsingWeb) { activity in
            if let url = activity.webpageURL { Task { await session.accept(url) } }
        }
        .onChange(of: session.loginURL) { _, url in
            if let url { openURL(url); session.loginURL = nil }
        }
        .sheet(item: Binding(get: { router.sheet }, set: { value in
            if value == nil { router.dismissSheet() } else { router.sheet = value }
        })) { sheet in
            switch sheet {
            case .login:
                VStack(spacing: 20) {
                    Text("Sign in to continue").font(.title2)
                    Button("Sign in") { Task { await session.beginLogin() } }
                        .buttonStyle(.borderedProminent)
                    Button("Cancel") { router.dismissSheet() }
                }
                .padding()
                .presentationDetents([.medium])
            case .composer:
                if let intent = router.composerIntent {
                    NativeComposerView(intent: intent, seed: router.composerSeed,
                                       dependencies: dependencies)
                }
            }
        }
        .sheet(item: $externalPage) { page in NativeSafariView(url: page.url) }
        .alert(item: Binding(get: { router.alert }, set: { router.alert = $0 })) { alert in
            Alert(title: Text(alert.title), message: Text(alert.message), dismissButton: .default(Text("OK")))
        }
        .overlay(alignment: .top) {
            if let message = session.banner ?? router.banner {
                HStack {
                    Text(message)
                    Spacer()
                    Button("Dismiss") { session.banner = nil; router.banner = nil }
                }
                .padding()
                .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 12))
                .padding()
                .accessibilityIdentifier("appBanner")
            }
        }
    }

    private var tabs: some View {
                TabView(selection: Binding(get: { router.selectedTab }, set: { router.selectedTab = $0 })) {
                    ForEach(AppTab.allCases) { tab in
                        TabContent(tab: tab, dependencies: dependencies)
                            .tabItem { Label(tab.title, image: tab.image) }
                            .tag(tab)
                    }
                }
    }

    private func startupProblem(title: String, explanation: String) -> some View {
        ContentUnavailableView {
            Label(title, systemImage: "exclamationmark.triangle")
        } description: {
            Text(explanation)
        } actions: {
            Button("Retry") { Task { await session.retry() } }
                .buttonStyle(.borderedProminent)
        }
    }
}

private struct ExternalPage: Identifiable {
    let id = UUID()
    let url: URL
}

private struct TabContent: View {
    let tab: AppTab
    let dependencies: AppDependencies
    @Environment(\.horizontalSizeClass) private var width

    private var router: AppRouter { dependencies.router }
    private var session: SessionModel { dependencies.session }

    var body: some View {
        Group {
            if tab != .more && width == .regular {
                NavigationSplitView {
                    listContent
                        .navigationTitle(tab.title)
                        .toolbar { topActions }
                } detail: {
                    if let route = router.detail(for: tab) {
                        destination(route)
                    } else {
                        ContentUnavailableView("Select an item", systemImage: tab.symbol)
                    }
                }
            } else {
                NavigationStack(path: Binding(
                    get: { router.paths[tab, default: []] },
                    set: { router.replacePath($0, for: tab) }
                )) {
                    listContent
                        .navigationTitle(tab.title)
                        .toolbar { topActions }
                        .navigationDestination(for: AppRoute.self) { destination($0) }
                }
            }
        }
    }

    @ViewBuilder private var listContent: some View {
        if tab == .more {
            List {
                Section {
                    if session.isLoggedIn {
                        routeButton("Profile", symbol: "person", route: .profile)
                        badgeRouteButton("Messages", symbol: "envelope", route: .messages,
                                         count: session.notificationCounts.pm)
                        badgeRouteButton("Notifications", symbol: "bell", route: .notifications,
                                         count: session.notificationCounts.total - session.notificationCounts.pm)
                        routeButton("Favorites", symbol: "star", route: .favorites)
                        routeButton("Observed", symbol: "eye", route: .observed)
                    } else {
                        Button("Sign in") { router.sheet = .login }
                    }
                }
                Section {
                    routeButton("Search", symbol: "magnifyingglass", route: .search)
                    routeButton("Hits", symbol: "flame", route: .hits)
                    routeButton("Rank", symbol: "chart.bar", route: .rank)
                }
                #if DEBUG
                if session.isLoggedIn {
                    Section {
                        routeButton("Add link", symbol: "link.badge.plus", route: .addLink)
                    }
                }
                #endif
                Section {
                    routeButton("Settings", symbol: "gear", route: .settings)
                    routeButton("About", symbol: "info.circle", route: .about)
                }
            }
        } else {
            NativeFeedView(tab: tab, dependencies: dependencies)
        }
    }

    private func badgeRouteButton(_ title: LocalizedStringKey, symbol: String, route: AppRoute,
                                  count: Int) -> some View {
        Button { router.navigate(route, in: tab) } label: {
            HStack {
                Label(title, systemImage: symbol)
                Spacer()
                if count > 0 {
                    Text("\(count)").font(.caption.bold()).padding(.horizontal, 7).padding(.vertical, 2)
                        .background(.red, in: Capsule()).foregroundStyle(.white)
                        .accessibilityLabel(String(localized: "Unread: \(count)"))
                }
            }
        }
    }

    private func routeButton(_ title: LocalizedStringKey, symbol: String, route: AppRoute) -> some View {
        Button { router.navigate(route, in: tab) } label: {
            Label(title, systemImage: symbol)
        }
    }

    @ToolbarContentBuilder private var topActions: some ToolbarContent {
        ToolbarItemGroup(placement: .topBarTrailing) {
            Button { router.navigate(.search, in: tab) } label: {
                Image(systemName: "magnifyingglass")
            }
            .accessibilityLabel("Search")
            #if DEBUG
            if tab != .more {
                Button { router.presentComposer(.createEntry) } label: {
                    Image(systemName: "square.and.pencil")
                }
                .accessibilityLabel("Write a post")
            }
            #endif
            if session.isLoggedIn {
                Button { router.navigate(.notifications, in: tab) } label: {
                    Image(systemName: session.unreadCount > 0 ? "bell.badge" : "bell")
                }
                .accessibilityLabel("Notifications")
                .accessibilityValue(session.unreadCount > 0 ? String(localized: "Unread: \(session.unreadCount)") : "")
                .accessibilityIdentifier("toolbarNotifications")
            }
        }
    }

    @ViewBuilder private func destination(_ route: AppRoute) -> some View {
        switch route {
        case .link(let id): NativeDetailView(kind: .link, id: id, dependencies: dependencies)
        case .entry(let id): NativeDetailView(kind: .entry, id: id, dependencies: dependencies)
        case .messages: NativeInboxView(tab: tab, dependencies: dependencies)
        case .conversation(let name): NativeConversationView(username: name, tab: tab, dependencies: dependencies)
        case .newConversation: NativeNewConversationView(tab: tab, dependencies: dependencies)
        case .notifications: NativeNotificationsView(tab: tab, dependencies: dependencies)
        case .search: NativeSearchView(tab: tab, dependencies: dependencies)
        case .advancedSearch(let query):
            NativeAdvancedSearchView(initialQuery: query, tab: tab, dependencies: dependencies)
        case .tags: DevelopmentView(title: String(localized: "Tags"))
        case .profile: NativeProfileView(username: nil, tab: tab, dependencies: dependencies)
        case .tag(let name): NativeTagView(tag: name, tab: tab, dependencies: dependencies)
        case .user(let name): NativeProfileView(username: name, tab: tab, dependencies: dependencies)
        case .settings: NativeSettingsView(tab: tab, dependencies: dependencies)
        case .blacklists: NativeBlacklistsView(tab: tab, dependencies: dependencies)
        case .favorites: NativeFavouritesView(tab: tab, dependencies: dependencies)
        case .observed: NativeObservedView(tab: tab, dependencies: dependencies)
        case .hits: NativeHitsView(tab: tab, dependencies: dependencies)
        case .addLink: NativeLinkSubmissionView(dependencies: dependencies)
        case .rank: NativeRankView(tab: tab, dependencies: dependencies)
        case .about: NativeAboutView(dependencies: dependencies)
        case .debug:
            #if DEBUG
            NativeDebugView(tab: tab, dependencies: dependencies)
            #else
            EmptyView()
            #endif
        }
    }
}

private struct DevelopmentView: View {
    let title: String
    var body: some View {
        ContentUnavailableView {
            Label(title, systemImage: "hammer")
        } description: {
            Text("This screen is in development.")
        }
        .navigationTitle(title)
    }
}

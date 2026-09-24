import SwiftUI
import PodkopShared

@main
struct PodkopNativeApp: App {
    var body: some Scene {
        WindowGroup { NativeRoot(dependencies: .shared) }
    }
}

private struct NativeRoot: View {
    let dependencies: AppDependencies
    @Environment(\.scenePhase) private var scenePhase
    @Environment(\.openURL) private var openURL
    @State private var sceneID = UUID()

    private var session: SessionModel { dependencies.session }
    private var router: AppRouter { dependencies.router }

    var body: some View {
        Group {
            switch session.phase {
            case .initializing:
                ProgressView(String(localized: "Starting Podkop…"))
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
                DevelopmentView(title: String(localized: "Write a post"))
            }
        }
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
                            .tabItem { Label(tab.title, systemImage: tab.symbol) }
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
                    #if DEBUG
                    if session.isLoggedIn {
                        routeButton("Profile", symbol: "person", route: .profile)
                        routeButton("Favorites", symbol: "star", route: .favorites)
                        routeButton("Observed", symbol: "eye", route: .observed)
                        routeButton("Messages", symbol: "envelope", route: .messages)
                        routeButton("Notifications", symbol: "bell", route: .notifications)
                    }
                    #endif
                    if session.isLoggedIn {
                        Button("Sign out") { Task { await session.logout() } }
                    } else {
                        Button("Sign in") { router.sheet = .login }
                    }
                }
                #if DEBUG
                Section {
                    routeButton("Search", symbol: "magnifyingglass", route: .search)
                    routeButton("Tags", symbol: "number", route: .tags)
                    routeButton("Hits", symbol: "flame", route: .hits)
                    routeButton("Rank", symbol: "chart.bar", route: .rank)
                    routeButton("Settings", symbol: "gear", route: .settings)
                    routeButton("About", symbol: "info.circle", route: .about)
                }
                #endif
            }
        } else {
            NativeFeedView(tab: tab, dependencies: dependencies)
        }
    }

    private func routeButton(_ title: LocalizedStringKey, symbol: String, route: AppRoute) -> some View {
        Button { router.navigate(route, in: tab) } label: {
            Label(title, systemImage: symbol)
        }
    }

    @ToolbarContentBuilder private var topActions: some ToolbarContent {
        ToolbarItemGroup(placement: .topBarTrailing) {
            #if DEBUG
            Button { router.navigate(.search, in: tab) } label: {
                Image(systemName: "magnifyingglass")
            }
            .accessibilityLabel("Search")
            if tab != .more {
                Button { router.sheet = session.isLoggedIn ? .composer : .login } label: {
                    Image(systemName: "square.and.pencil")
                }
                .accessibilityLabel("Write a post")
            }
            if session.isLoggedIn {
                Button { router.navigate(.notifications, in: tab) } label: {
                    Image(systemName: "bell")
                }
                .accessibilityLabel("Notifications")
            }
            #endif
        }
    }

    @ViewBuilder private func destination(_ route: AppRoute) -> some View {
        switch route {
        case .link(let id): DevelopmentView(title: "Link \(id)")
        case .entry(let id): DevelopmentView(title: "Entry \(id)")
        case .messages: DevelopmentView(title: String(localized: "Messages"))
        case .notifications: DevelopmentView(title: String(localized: "Notifications"))
        case .search: DevelopmentView(title: String(localized: "Search"))
        case .tags: DevelopmentView(title: String(localized: "Tags"))
        case .profile: DevelopmentView(title: String(localized: "Profile"))
        case .tag(let name): DevelopmentView(title: "#\(name)")
        case .user(let name): DevelopmentView(title: name)
        case .settings: DevelopmentView(title: String(localized: "Settings"))
        case .favorites: DevelopmentView(title: String(localized: "Favorites"))
        case .observed: DevelopmentView(title: String(localized: "Observed"))
        case .hits: DevelopmentView(title: String(localized: "Hits"))
        case .rank: DevelopmentView(title: String(localized: "Rank"))
        case .about: DevelopmentView(title: String(localized: "About"))
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

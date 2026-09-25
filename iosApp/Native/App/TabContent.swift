import SwiftUI
import PodkopShared

struct TabContent: View {
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
                if session.isLoggedIn {
                    Section {
                        routeButton("Add link", symbol: "link.badge.plus", route: .addLink)
                    }
                }
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
            if tab != .more {
                Button { router.presentComposer(.createEntry) } label: {
                    Image(systemName: "square.and.pencil")
                }
                .accessibilityLabel("Write a post")
            }
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

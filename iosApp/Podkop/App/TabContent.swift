import SwiftUI
import PodkopShared

struct TabContent: View {
    let tab: AppTab
    let dependencies: AppDependencies
    @Environment(\.horizontalSizeClass) private var width
    /// The window's width in points; the split layout needs Android's expanded width.
    @State private var availableWidth: CGFloat = 0

    /// Android shows list and detail side by side only from 840 dp ("expanded"). Narrower
    /// regular-width windows, like the unfolded iPhone Duo or iPad split screen, stay a larger
    /// phone layout instead of an overlaid sidebar.
    static let splitMinimumWidth: CGFloat = 840
    private var usesSplitLayout: Bool {
        tab != .more && width == .regular && availableWidth >= Self.splitMinimumWidth
    }

    private var router: AppRouter { dependencies.router }
    private var session: SessionModel { dependencies.session }

    var body: some View {
        Group {
            if usesSplitLayout {
                NavigationSplitView {
                    listContent
                        .navigationTitle(tab.title)
                        .toolbar { topActions }
                } detail: {
                    if let route = router.detail(for: tab) {
                        // Screens keep their model in @State, so a new route needs a new identity.
                        destination(route).id(route)
                    } else {
                        ContentUnavailableView(.appSelectItem, systemImage: tab.symbol)
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                            .background(PodkopTheme.background.ignoresSafeArea())
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
        .onGeometryChange(for: CGFloat.self) { $0.size.width } action: { availableWidth = $0 }
    }

    @ViewBuilder private var listContent: some View {
        if tab == .more {
            MoreView(dependencies: dependencies)
        } else {
            FeedView(tab: tab, dependencies: dependencies)
                .background(PodkopTheme.background.ignoresSafeArea())
        }
    }

    @ToolbarContentBuilder private var topActions: some ToolbarContent {
        if tab != .more {
            ToolbarItemGroup(placement: .topBarTrailing) {
                if session.isLoggedIn {
                    if tab == .entries {
                        Button { router.presentComposer(.createEntry) } label: {
                            Image(systemName: "plus")
                        }
                        .accessibilityLabel(.commonWritePost)
                        .accessibilityIdentifier("toolbarAddEntry")
                    } else if tab == .upcoming {
                        Button { router.navigate(.addLink, in: tab) } label: {
                            Image(systemName: "plus")
                        }
                        .accessibilityLabel(.commonAddLink)
                        .accessibilityIdentifier("toolbarAddLink")
                    }
                }
                Button { router.navigate(.search, in: tab) } label: {
                    Image(systemName: "magnifyingglass")
                }
                .accessibilityLabel(.commonSearch)
                .accessibilityIdentifier("toolbarSearch")
                if session.isLoggedIn {
                    Button { router.navigate(.notifications, in: tab) } label: {
                        Image(systemName: session.unreadCount > 0 ? "bell.badge" : "bell")
                    }
                    .accessibilityLabel(.commonNotifications)
                    .accessibilityValue(session.unreadCount > 0 ? String(localized: .commonUnread2(session.unreadCount)) : "")
                    .accessibilityIdentifier("toolbarNotifications")
                }
            }
        }
    }

    private func destination(_ route: AppRoute) -> some View {
        destinationContent(route)
            .background(PodkopTheme.background.ignoresSafeArea())
    }

    @ViewBuilder private func destinationContent(_ route: AppRoute) -> some View {
        switch route {
        case .link(let id): DetailView(kind: .link, id: id, dependencies: dependencies)
        case .entry(let id): DetailView(kind: .entry, id: id, dependencies: dependencies)
        case .messages: InboxView(tab: tab, dependencies: dependencies)
        case .conversation(let name): ConversationView(username: name, tab: tab, dependencies: dependencies)
        case .newConversation: NewConversationView(tab: tab, dependencies: dependencies)
        case .notifications: NotificationsView(tab: tab, dependencies: dependencies)
        case .search: SearchView(tab: tab, dependencies: dependencies)
        case .advancedSearch(let query):
            AdvancedSearchView(initialQuery: query, tab: tab, dependencies: dependencies)
        case .tags: SearchView(tab: tab, dependencies: dependencies)
        case .profile: ProfileView(username: nil, tab: tab, dependencies: dependencies)
        case .tag(let name): TagView(tag: name, tab: tab, dependencies: dependencies)
        case .tagContent(let name, let kind): TagView(tag: name, kind: kind, tab: tab, dependencies: dependencies)
        case .user(let name): ProfileView(username: name, tab: tab, dependencies: dependencies)
        case .settings: SettingsView(tab: tab, dependencies: dependencies)
        case .blacklists: BlacklistsView(tab: tab, dependencies: dependencies)
        case .favorites: FavouritesView(tab: tab, dependencies: dependencies)
        case .observed: ObservedView(tab: tab, dependencies: dependencies)
        case .hits: HitsView(tab: tab, dependencies: dependencies)
        case .addLink: LinkSubmissionView(dependencies: dependencies)
        case .rank: RankView(tab: tab, dependencies: dependencies)
        case .about: AboutView(dependencies: dependencies)
        case .debug:
            #if DEBUG
            DebugView(tab: tab, dependencies: dependencies)
            #else
            EmptyView()
            #endif
        }
    }
}

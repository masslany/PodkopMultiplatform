import SwiftUI

/// The More tab (Android's `MoreScreen`): the profile header or a sign-in card, then the
/// Społeczność, Treści and System sections. It has no navigation bar, like Android; the profile
/// banner runs under the status bar.
struct MoreView: View {
    let dependencies: AppDependencies
    @State private var model: MoreModel

    private var router: AppRouter { dependencies.router }
    private var session: SessionModel { dependencies.session }

    init(dependencies: AppDependencies) {
        self.dependencies = dependencies
        _model = State(initialValue: MoreModel(loader: dependencies.profileLoader))
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                header
                if session.isLoggedIn {
                    MoreMenuSection(title: "Community", items: [
                        item("notifications", "Notifications", "bell.fill", WykopTheme.voteNegative, .notifications,
                             badge: session.notificationCounts.total - session.notificationCounts.pm),
                        item("messages", "Messages", "text.bubble.fill", WykopTheme.votePositive, .messages,
                             badge: session.notificationCounts.pm),
                        item("favorites", "Favorites", "heart.fill", WykopTheme.genderPink, .favorites),
                    ])
                }
                MoreMenuSection(title: "Content", items: contentItems)
                MoreMenuSection(title: "System", items: [
                    item("settings", "Settings", "gearshape.fill", Color(white: 0.55), .settings),
                    item("about", "About", "chevron.left.forwardslash.chevron.right",
                         Color(red: 0.36, green: 0.36, blue: 0.4), .about),
                ])
                footer
            }
            .padding(.bottom, 24)
            .frame(maxWidth: 700)
            .frame(maxWidth: .infinity)
        }
        .ignoresSafeArea(edges: showsProfile ? .top : [])
        .background(WykopTheme.background.ignoresSafeArea())
        .toolbar(.hidden, for: .navigationBar)
        .animation(.easeInOut(duration: 0.25), value: showsProfile)
        .onChange(of: session.revision, initial: true) { _, revision in
            model.update(loggedIn: session.isLoggedIn, revision: revision)
        }
        .onChange(of: session.isLoggedIn) { _, loggedIn in
            model.update(loggedIn: loggedIn, revision: session.revision)
        }
        .onAppear { model.refresh() }
        .accessibilityIdentifier("moreMenu")
    }

    private var showsProfile: Bool { session.isLoggedIn && model.profile != nil }

    @ViewBuilder private var header: some View {
        if let profile = model.profile, session.isLoggedIn {
            MoreProfileHeader(profile: profile) { router.navigate(.profile, in: .more) }
                .transition(.opacity)
        } else if session.isLoggedIn {
            // The preview is loading or failed; keep the space calm instead of flashing a card.
            Color.clear.frame(height: 8)
        } else {
            MoreSignInCard { router.sheet = .login }
                .padding(.horizontal, 16)
                .padding(.top, 8)
                .transition(.opacity)
        }
    }

    private var contentItems: [MoreItem] {
        var items = [
            item("hits", "Hits", "flame.fill", WykopTheme.hotOrange, .hits),
            item("rank", "Rank", "chart.bar.fill", Color(red: 0.55, green: 0.36, blue: 0.86), .rank),
            item("search", "Search", "magnifyingglass", Color(white: 0.5), .search),
        ]
        if session.isLoggedIn {
            items.append(item("observed", "My Wykop", "binoculars.fill", Color(red: 0.19, green: 0.69, blue: 0.78), .observed))
            items.append(item("addLink", "Add link", "link.badge.plus", WykopTheme.nameGreen, .addLink))
        }
        return items
    }

    private func item(_ id: String, _ title: LocalizedStringKey, _ symbol: String, _ tint: Color,
                      _ route: AppRoute, badge: Int = 0) -> MoreItem {
        MoreItem(id: id, title: title, symbol: symbol, tint: tint, badge: max(0, badge)) {
            router.navigate(route, in: .more)
        }
    }

    private var footer: some View {
        let info = Bundle.main.infoDictionary
        let version = info?["CFBundleShortVersionString"] as? String ?? ""
        let build = info?["CFBundleVersion"] as? String ?? ""
        return VStack(spacing: 6) {
            Image("TabUpcoming")
                .renderingMode(.template)
                .foregroundStyle(.tertiary)
            Text(verbatim: "Podkop \(version) (\(build))")
                .font(.caption)
                .foregroundStyle(.tertiary)
        }
        .frame(maxWidth: .infinity)
        .padding(.top, 8)
        .accessibilityElement(children: .combine)
    }
}

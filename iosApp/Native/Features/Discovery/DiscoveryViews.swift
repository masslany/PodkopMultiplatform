import SwiftUI

extension ResourceActions {
    /// Card actions shared by feeds and discovery lists: navigation plus voting, favourites and
    /// the actions sheet through the shared `ResourceInteractor`. Guests are asked to sign in.
    @MainActor
    static func navigation(for item: NativeResource, in tab: AppTab,
                           dependencies: AppDependencies, openURL: OpenURLAction) -> ResourceActions {
        let router = dependencies.router
        let interactor = dependencies.interactor
        let loggedIn = dependencies.session.isLoggedIn
        let open: (() -> Void)? = switch item.kind {
        case .link: { router.navigate(.link(item.sourceID), in: tab) }
        case .entry: { router.navigate(.entry(item.sourceID), in: tab) }
        case .linkComment: item.parentID.map { id in { router.navigate(.link(id), in: tab) } }
        case .entryComment: item.parentID.map { id in { router.navigate(.entry(id), in: tab) } }
        case .unknown: nil
        }
        let signIn = { router.sheet = .login }
        let canVoteUp = item.vote.canUp || item.vote.canUndo
        let canVoteDown = item.kind == .linkComment && (item.vote.canDown || item.vote.canUndo)
        let votable = item.deletion == nil && item.kind != .unknown
        return ResourceActions(
            open: open,
            openAuthor: { router.navigate(.user($0), in: tab) },
            openTag: { router.navigate(.tag($0), in: tab) },
            openURL: { openURL($0) },
            voteUp: !votable ? nil : !loggedIn ? signIn : canVoteUp ? { interactor.voteUp(item) } : nil,
            voteDown: !votable || item.kind != .linkComment ? nil
                : !loggedIn ? signIn : canVoteDown ? { interactor.voteDown(item) } : nil,
            favourite: !votable ? nil : loggedIn ? { interactor.toggleFavourite(item) } : signIn,
            comment: open,
            menu: item.kind == .link || item.kind == .entry ? { interactor.actionTarget = item } : nil,
            loadTweet: { try await dependencies.loadTweet($0) },
            pending: interactor.isPending(item)
        )
    }
}

/// Resource rows for a `ListPager`, including empty, failure, and next-page states.
struct PagedResourceRows: View {
    @Environment(\.openURL) private var openURL
    let pager: ListPager<NativeResource>
    let tab: AppTab
    let dependencies: AppDependencies
    var emptyTitle: LocalizedStringKey = "Nothing here yet"

    var body: some View {
        LazyVStack(spacing: 12) {
            if pager.refreshError {
                HStack {
                    Label("Could not refresh", systemImage: "exclamationmark.triangle")
                    Spacer()
                    Button("Retry") { Task { await pager.refresh() } }
                }
                .font(.subheadline)
                .padding(10)
                .background(WykopTheme.card, in: RoundedRectangle(cornerRadius: 12, style: .continuous))
            }
            switch pager.phase {
            case .idle, .loading:
                ProgressView("Loading…").frame(maxWidth: .infinity, minHeight: 180)
            case .failed:
                ContentUnavailableView {
                    Label("Could not load content", systemImage: "wifi.exclamationmark")
                } actions: {
                    Button("Retry") { pager.retry() }
                }
            case .loaded where pager.items.isEmpty:
                ContentUnavailableView(emptyTitle, systemImage: "tray")
            case .loaded:
                ForEach(pager.items) { item in
                    NativeResourceCard(
                        resource: item,
                        actions: .navigation(for: item, in: tab, dependencies: dependencies, openURL: openURL),
                        autoplayGifs: dependencies.session.autoplayGifs,
                        isForeground: dependencies.isForeground
                    )
                    .onAppear { pager.loadNextIfNeeded(after: item) }
                }
                PagerFooter(pager: pager)
            }
        }
    }
}

struct PagerFooter<Item>: View {
    let pager: ListPager<Item>

    var body: some View {
        if pager.nextLoading { ProgressView("Loading…").padding() }
        if pager.nextError {
            Button("Retry next page") { pager.retry() }.buttonStyle(.bordered)
        }
    }
}

/// Compact identity row for users in suggestions and rankings.
struct UserIdentityRow: View {
    let username: String
    let color: String?
    let gender: String?
    var detail: String?
    var avatarURL: String?

    var body: some View {
        HStack(spacing: 10) {
            AvatarView(url: avatarURL, name: username, size: 36, gender: gender)
            VStack(alignment: .leading, spacing: 2) {
                Text(username).font(.body.weight(.semibold)).foregroundStyle(authorColor(color))
                if let detail {
                    Text(detail).font(.caption).foregroundStyle(.secondary)
                }
            }
        }
    }
}

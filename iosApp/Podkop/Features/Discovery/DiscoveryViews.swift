import SwiftUI

extension Vote {
    /// Android's `toVoteState`: a vote button shows only when the API allows voting or undoing
    /// the viewer's own vote, so signed-out readers see scores without buttons.
    var allowsUp: Bool { canUp || (state == "positive" && canUndo) }
    var allowsDown: Bool { canDown || (state == "negative" && canUndo) }
}

extension ResourceActions {
    /// Card actions shared by feeds and discovery lists: navigation plus voting, favourites,
    /// replies and the actions sheet, each offered only when the API allows it.
    @MainActor
    static func navigation(for item: Resource, in tab: AppTab, root: Resource? = nil,
                           dependencies: AppDependencies, openURL: OpenURLAction) -> ResourceActions {
        let router = dependencies.router
        let interactor = dependencies.interactor
        let open: (() -> Void)? = switch item.kind {
        case .link: { router.navigate(.link(item.sourceID), in: tab) }
        case .entry: { router.navigate(.entry(item.sourceID), in: tab) }
        case .linkComment: item.parentID.map { id in { router.navigate(.link(id), in: tab) } }
        case .entryComment: item.parentID.map { id in { router.navigate(.entry(id), in: tab) } }
        case .unknown: nil
        }
        let live = item.deletion == nil && item.kind != .unknown
        let reply: (() -> Void)? = switch item.kind {
        case .entry: { router.presentComposer(.createEntryComment(entryID: item.sourceID, replyTarget: item.author?.name)) }
        case .entryComment: item.parentID.map { id in
            { router.presentComposer(.createEntryComment(entryID: id, replyTarget: item.author?.name)) } }
        case .linkComment: item.parentID.map { id in
            { router.presentComposer(.createLinkComment(linkID: id, parentCommentID: item.sourceID,
                                                        replyTarget: item.author?.name)) } }
        case .link, .unknown: nil
        }
        return ResourceActions(
            open: open,
            openAuthor: { router.navigate(.user($0), in: tab) },
            openTag: { router.navigate(.tag($0), in: tab) },
            openURL: { openURL($0) },
            voteUp: live && item.vote.allowsUp ? { interactor.voteUp(item) } : nil,
            voteDown: live && item.kind == .linkComment && item.vote.allowsDown ? { interactor.voteDown(item) } : nil,
            favourite: live && item.canFavourite ? { interactor.toggleFavourite(item) } : nil,
            comment: live && item.canReply ? reply : nil,
            // Comments need their link or entry for links and voters, so only embedded ones get a menu.
            menu: item.kind == .link || item.kind == .entry || root != nil
                ? { interactor.actionTarget = .init(resource: item, root: root ?? item) } : nil,
            loadTweet: { try await dependencies.loadTweet($0) },
            pending: interactor.isPending(item)
        )
    }
}

/// A resource in a list with the comments the API embeds under it, like Android's feed cards.
struct ResourceListRow: View {
    @Environment(\.openURL) private var openURL
    let item: Resource
    let tab: AppTab
    let dependencies: AppDependencies

    var body: some View {
        let actions = ResourceActions.navigation(for: item, in: tab, dependencies: dependencies, openURL: openURL)
        ResourceThreadCard(
            root: item, rootActions: actions, children: item.inlineComments,
            childActions: { .navigation(for: $0, in: tab, root: item, dependencies: dependencies, openURL: openURL) },
            autoplayGifs: dependencies.session.autoplayGifs,
            isForeground: dependencies.isForeground,
            open: actions.open
        ) {
            if !item.inlineComments.isEmpty, item.commentCount > item.inlineComments.count, let open = actions.open {
                ThreadMoreButton(title: String(localized: .discoveryShowComments(item.commentCount)), action: open)
            }
        }
    }
}

/// Resource rows for a `ListPager`, including empty, failure, and next-page states.
struct PagedResourceRows: View {
    @Environment(\.openURL) private var openURL
    let pager: ListPager<Resource>
    let tab: AppTab
    let dependencies: AppDependencies
    var emptyTitle: LocalizedStringResource = .commonNothingHereYet

    var body: some View {
        LazyVStack(spacing: 12) {
            if pager.refreshError {
                HStack {
                    Label(.commonCouldNotRefresh, systemImage: "exclamationmark.triangle")
                    Spacer()
                    Button(.commonRetry) { Task { await pager.refresh() } }
                }
                .font(.subheadline)
                .padding(10)
                .background(WykopTheme.card, in: RoundedRectangle(cornerRadius: 12, style: .continuous))
            }
            switch pager.phase {
            case .idle, .loading:
                ProgressView(.commonLoading).frame(maxWidth: .infinity, minHeight: 180)
            case .failed:
                ContentUnavailableView {
                    Label(.commonCouldNotLoadContent, systemImage: "wifi.exclamationmark")
                } actions: {
                    Button(.commonRetry) { pager.retry() }
                }
            case .loaded where pager.items.isEmpty:
                ContentUnavailableView(emptyTitle, systemImage: "tray")
            case .loaded:
                ForEach(pager.items) { item in
                    ResourceListRow(item: item, tab: tab, dependencies: dependencies)
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
        if pager.nextLoading { ProgressView(.commonLoading).padding() }
        if pager.nextError {
            Button(.commonRetryNextPage) { pager.retry() }.buttonStyle(.bordered)
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

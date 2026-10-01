import SwiftUI

struct DetailView: View {
    @Environment(\.openURL) private var openURL
    @State private var model: DetailModel
    let dependencies: AppDependencies
    @State private var actionTarget: Resource?
    /// Where the link title ends in the scroll content, and whether it has scrolled under the bar.
    /// Only the boolean is state, so scrolling does not re-render the page on every frame.
    @State private var titleBottom: CGFloat?
    @State private var showsLinkTitle = false

    init(kind: ResourceKind, id: Int, dependencies: AppDependencies) {
        _model = State(initialValue: DetailModel(kind: kind, id: id,
                                                loader: dependencies.detailLoader,
                                                mutator: dependencies.detailMutator,
                                                updates: dependencies.resourceUpdates,
                                                threadedEntryComments: { [session = dependencies.session] in
                                                    session.threadedEntryComments
                                                }))
        self.dependencies = dependencies
    }

    var body: some View {
        ScrollView {
            // No stack spacing: entry comments draw one continuous line, so gaps are explicit.
            LazyVStack(alignment: .leading, spacing: 0) {
                switch model.phase {
                case .idle, .loading:
                    ProgressView(.commonLoading).frame(maxWidth: .infinity, minHeight: 220)
                case .failed:
                    ContentUnavailableView {
                        Label(.commonCouldNotLoadContent, systemImage: "wifi.exclamationmark")
                    } actions: {
                        Button(.commonRetry) { model.reload() }
                    }
                case .loaded:
                    // Like Android, a link sits on the page background and an entry on a card.
                    if let resource = model.resource {
                        if resource.kind == .link {
                            ResourceCard(resource: resource, actions: actions(for: resource),
                                         style: .detailHeader,
                                         autoplayGifs: dependencies.session.autoplayGifs,
                                         isForeground: dependencies.isForeground,
                                         onTitleBottom: { titleBottom = $0 })
                                .padding(.top, resource.photo != nil ? 0 : 12)
                                .padding(.bottom, 16)
                        } else {
                            ResourceCard(resource: resource, actions: actions(for: resource),
                                         style: .card,
                                         autoplayGifs: dependencies.session.autoplayGifs,
                                         isForeground: dependencies.isForeground)
                                .padding(.horizontal, 12)
                                .padding(.top, 12)
                                .padding(.bottom, 16)
                        }
                    }
                }
                if model.phase == .loaded {
                    if model.kind == .link, model.phase == .loaded { relatedSection.padding(.bottom, 16) }
                    commentsSection
                }
            }
            .padding(.bottom, 20)
            .frame(maxWidth: 760)
            .frame(maxWidth: .infinity)
            .coordinateSpace(name: ResourceCard.detailContentSpace)
        }
        .onScrollGeometryChange(for: CGFloat.self) { $0.contentOffset.y + $0.contentInsets.top } action: { _, top in
            let shows = titleBottom.map { top > $0 } ?? false
            if shows != showsLinkTitle { showsLinkTitle = shows }
        }
        .refreshable { model.reload() }
        .task { model.start() }
        .onDisappear { model.stop() }
        .onChange(of: dependencies.resourceUpdates.revision) { _, _ in model.reconcileUpdates() }
        .onChange(of: dependencies.session.revision) { _, _ in model.sessionChanged() }
        // Failed votes and favourites show the app banner, like the lists' ResourceInteractor.
        .onChange(of: model.deletedKind) { _, kind in
            guard let kind else { return }
            dependencies.router.banner = kind.deletedMessage
            model.acknowledgeDeletion()
        }
        .onChange(of: model.actionFailed) { _, failed in
            guard failed else { return }
            dependencies.router.banner = String(localized: .commonCouldNotCompleteAction)
            model.acknowledgeFailure()
        }
        .sheet(item: $actionTarget) { target in
            ResourceActionsSheet(resource: target, root: model.resource ?? target,
                                       parent: screenshotParent(for: target),
                                       dependencies: dependencies,
                                       delete: { model.submit(.delete(target)) })
        }
        .navigationTitle(navigationTitle)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            // Links start with an empty bar and show their title once it scrolls under it.
            if model.kind == .link {
                ToolbarItem(placement: .principal) {
                    Text(showsLinkTitle ? model.resource?.title ?? "" : "")
                        .font(.headline)
                        .lineLimit(1)
                        .truncationMode(.tail)
                        .animation(.easeInOut(duration: 0.15), value: showsLinkTitle)
                }
            }
        }
        .accessibilityIdentifier("detail-\(model.kind.rawValue)-\(model.id)")
    }

    private var navigationTitle: String {
        model.kind == .link ? String(localized: .commonLink) : String(localized: .detailsEntry)
    }

    /// Direct children of the page's `LazyVStack`, so only visible comment threads are built and
    /// the next page loads when the last thread appears, not all at once.
    @ViewBuilder private var commentsSection: some View {
        if model.kind == .link {
            Menu {
                Button(.commonBest) { model.selectCommentSort("best") }
                Button(.commonNewest) { model.selectCommentSort("newest") }
                Button(.commonOldest) { model.selectCommentSort("oldest") }
            } label: {
                DropdownLabel(title: commentSortTitle)
            }
            .accessibilityIdentifier("commentSort")
            .padding(.horizontal, 12)
            .padding(.bottom, 12)
        }
        if let rows = model.threadRows, !rows.isEmpty {
            ForEach(Array(rows.enumerated()), id: \.element.id) { index, row in
                threadRow(row, next: rows.indices.contains(index + 1) ? rows[index + 1] : nil)
                    .onAppear {
                        if row.id == rows.last?.id { model.loadMoreComments() }
                    }
            }
            if model.commentsLoading { ProgressView(.commonLoading).frame(maxWidth: .infinity).padding(.vertical, 12) }
            if model.nextCommentsError {
                ThreadMoreButton(title: String(localized: .commonRetryNextPage)) { model.retryComments() }
                    .padding(.vertical, 12)
            }
        } else if model.commentsLoading && model.comments.isEmpty {
            ProgressView(.commonLoading).frame(maxWidth: .infinity).padding(.vertical, 12)
        } else if model.commentsError && model.comments.isEmpty {
            VStack(spacing: 8) {
                Text(.detailsLinksDetailsScreenErrorLoadingComments)
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
                ThreadMoreButton(title: String(localized: .detailsRetryComments)) { model.retryComments() }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
        } else if model.comments.isEmpty {
            // Like Android, only links say so; an entry without comments just ends.
            if model.kind == .link {
                Text(.detailsLinksDetailsScreenNoComments)
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
            }
        } else {
            ForEach(model.comments) { comment in
                Group {
                    if model.kind == .link {
                        commentThread(comment)
                            .padding(.horizontal, 12)
                            .padding(.bottom, 12)
                    } else {
                        EntryCommentRow(comment: comment,
                                        actions: actions(for: comment, replyParentID: comment.sourceID),
                                        isOwn: isOwn(comment),
                                        isLast: comment.id == model.comments.last?.id,
                                        autoplayGifs: dependencies.session.autoplayGifs,
                                        isForeground: dependencies.isForeground)
                    }
                }
                .onAppear {
                    if comment.id == model.comments.last?.id { model.loadMoreComments() }
                }
            }
            if model.commentsLoading { ProgressView(.commonLoading).frame(maxWidth: .infinity).padding(.vertical, 12) }
            if model.nextCommentsError {
                ThreadMoreButton(title: String(localized: .commonRetryNextPage)) { model.retryComments() }
                    .padding(.vertical, 12)
            }
        }
    }

    @ViewBuilder private func threadRow(_ row: DetailModel.ThreadRow, next: DetailModel.ThreadRow?) -> some View {
        // Threads are separated from each other, not from their own replies.
        let endsThread = next?.depth == 0
        switch row {
        case .comment(let comment, let depth, _, let connectors):
            EntryThreadCommentRow(comment: comment,
                                  actions: actions(for: comment, replyParentID: comment.sourceID),
                                  depth: depth,
                                  connectors: connectors,
                                  endsThread: endsThread,
                                  isLast: next == nil,
                                  autoplayGifs: dependencies.session.autoplayGifs,
                                  isForeground: dependencies.isForeground)
        case .moreReplies(let parentID, let depth, let remaining, let loading, let failed, let connectors):
            EntryThreadMoreRow(
                title: failed ? String(localized: .detailsRetryReplies)
                    : String(localized: .detailsEntryDetailsButtonShowMoreReplies(remaining)),
                loading: loading,
                depth: depth,
                connectors: connectors,
                endsThread: endsThread,
                isLast: next == nil
            ) {
                model.loadThreadReplies(for: parentID)
            }
        }
    }

    private func isOwn(_ resource: Resource) -> Bool {
        guard let me = dependencies.session.username, let author = resource.author?.name else { return false }
        return author == me
    }

    private func linkCommentAccent(_ comment: Resource, parent: Resource?) -> Color? {
        CommentAccent.resolve(author: comment.author?.name, linkAuthor: model.resource?.author?.name,
                              parentAuthor: parent?.author?.name,
                              currentUser: dependencies.session.username)?.color
    }

    /// A comment card with its replies inside it: the two the API embeds until the reader asks
    /// for all of them (Android's `LinkDetailsCommentItem`).
    private func commentThread(_ comment: Resource) -> some View {
        let state = model.replies[comment.sourceID] ?? DetailModel.ReplyState()
        let replies = state.rows.isEmpty ? comment.inlineComments : state.rows
        let remaining = max(0, comment.commentCount - replies.count)
        return ResourceThreadCard(
            root: comment, rootActions: actions(for: comment, replyParentID: comment.sourceID),
            children: model.kind == .link ? replies : [],
            childActions: { actions(for: $0, replyParentID: comment.sourceID) },
            autoplayGifs: dependencies.session.autoplayGifs,
            isForeground: dependencies.isForeground,
            accent: { linkCommentAccent($0, parent: $1) }
        ) {
            if model.kind == .link, !state.exhausted, remaining > 0 || state.loading || state.error {
                ThreadMoreButton(
                    title: state.error ? String(localized: .detailsRetryReplies)
                        : String(localized: .detailsShowAll(remaining)),
                    loading: state.loading
                ) {
                    model.loadReplies(for: comment.sourceID)
                }
            }
        }
    }

    private var commentSortTitle: String {
        switch model.commentSort {
        case "newest": String(localized: .commonNewest)
        case "oldest": String(localized: .commonOldest)
        default: String(localized: .commonBest)
        }
    }

    /// Related links scroll sideways as compact cards, as on Android.
    private var relatedSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(.detailsRelatedLinks).font(.headline).padding(.horizontal, 16)
            if model.related.isEmpty {
                Group {
                    switch model.relatedPhase {
                    case .loading: ProgressView()
                    case .failed: Text(.detailsLinksDetailsScreenErrorLoadingRelated)
                    case .loaded: Text(.detailsLinksDetailsScreenNoRelated)
                    }
                }
                .font(.footnote)
                .foregroundStyle(.secondary)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 8)
            } else {
            ScrollView(.horizontal) {
                LazyHStack(spacing: 10) {
                    ForEach(model.related) { item in
                        RelatedLinkCard(
                            resource: item,
                            pending: model.mutating.contains(ResourceIdentity(item)),
                            open: { openRelated(item) },
                            openAuthor: { dependencies.router.navigate(.user($0)) },
                            voteUp: item.vote.allowsUp ? {
                                model.submit(.relatedVote(linkID: model.id, resource: item,
                                                          remove: item.vote.state == "positive", down: false))
                            } : nil,
                            voteDown: item.vote.allowsDown ? {
                                model.submit(.relatedVote(linkID: model.id, resource: item,
                                                          remove: item.vote.state == "negative", down: true))
                            } : nil
                        )
                    }
                }
                .padding(.horizontal, 12)
            }
            .scrollIndicators(.hidden)
            }
        }
    }

    private func openRelated(_ item: Resource) {
        if let raw = item.sourceURL, let url = URL(string: raw) { openURL(url) }
    }

    /// Detail actions follow Android: each is offered only when the API allows it for the viewer.
    private func actions(for resource: Resource, replyParentID: Int? = nil) -> ResourceActions {
        let live = resource.deletion == nil
        let commentAction: () -> Void = {
            if model.kind == .link {
                dependencies.router.presentComposer(.createLinkComment(
                    linkID: model.id, parentCommentID: replyParentID,
                    replyTarget: replyParentID == nil ? nil : resource.author?.name))
            } else if resource.kind == .entryComment,
                      let parentID = model.thread?.replyParentID(for: resource.sourceID) {
                // Threaded: the reply nests under the comment.
                dependencies.router.presentComposer(.createEntryThreadReply(
                    entryID: model.id, parentCommentID: parentID, replyTarget: resource.author?.name))
            } else {
                dependencies.router.presentComposer(.createEntryComment(
                    entryID: model.id, replyTarget: resource.author?.name))
            }
        }
        let canDown = (resource.kind == .link || resource.kind == .linkComment) && resource.vote.allowsDown
        return ResourceActions(
            openAuthor: { dependencies.router.navigate(.user($0)) },
            openTag: { dependencies.router.navigate(.tag($0)) },
            openURL: { openURL($0) },
            voteUp: live && resource.vote.allowsUp ? {
                model.submit(.voteUp(resource, remove: resource.vote.state == "positive"))
            } : nil,
            voteDown: live && canDown ? {
                model.submit(.voteDown(resource, remove: resource.vote.state == "negative", reason: nil))
            } : nil,
            buryLink: live && canDown && resource.kind == .link ? { reason in
                model.submit(.voteDown(resource, remove: false, reason: reason.rawValue))
            } : nil,
            favourite: live && resource.canFavourite ? {
                model.submit(.favourite(resource, enabled: !resource.favourite))
            } : nil,
            comment: live && canReply(resource) ? commentAction : nil,
            menu: { actionTarget = resource },
            surveyVote: resource.kind == .entry && resource.survey?.canVote == true ? { option in
                requireAccount { model.submit(.survey(entryID: resource.sourceID, option: option)) }
            } : nil,
            loadTweet: { try await dependencies.loadTweet($0) },
            loadStreamable: dependencies.session.playVideosInline
                ? { try await dependencies.loadStreamableVideo($0) } : nil,
            pending: model.mutating.contains(ResourceIdentity(resource))
        )
    }

    /// Comments inherit the right to reply from the link or entry being viewed.
    private func canReply(_ resource: Resource) -> Bool {
        resource.canReply || (dependencies.session.isLoggedIn && (model.resource?.canReply ?? false))
    }

    private func requireAccount(_ action: () -> Void) {
        if dependencies.session.isLoggedIn { action() }
        else { dependencies.router.sheet = .login }
    }

    private func screenshotParent(for resource: Resource) -> Resource? {
        switch resource.kind {
        case .entryComment: model.resource
        case .linkComment:
            // A reply's parentID is its link (actions need it), so find the comment holding it.
            model.comments.first { comment in
                comment.inlineComments.contains { $0.sourceID == resource.sourceID }
                    || model.replies[comment.sourceID]?.rows.contains { $0.sourceID == resource.sourceID } == true
            }
        default: nil
        }
    }
}

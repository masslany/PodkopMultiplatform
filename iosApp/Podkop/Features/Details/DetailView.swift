import SwiftUI

struct DetailView: View {
    @Environment(\.openURL) private var openURL
    @State private var model: DetailModel
    let dependencies: AppDependencies
    @State private var actionTarget: Resource?
    @State private var downvoteTarget: Resource?
    /// Where the link title ends in the scroll content, and whether it has scrolled under the bar.
    /// Only the boolean is state, so scrolling does not re-render the page on every frame.
    @State private var titleBottom: CGFloat?
    @State private var showsLinkTitle = false

    init(kind: ResourceKind, id: Int, dependencies: AppDependencies) {
        _model = State(initialValue: DetailModel(kind: kind, id: id,
                                                loader: dependencies.detailLoader,
                                                mutator: dependencies.detailMutator,
                                                updates: dependencies.resourceUpdates))
        self.dependencies = dependencies
    }

    var body: some View {
        ScrollView {
            LazyVStack(alignment: .leading, spacing: 16) {
                switch model.phase {
                case .idle, .loading:
                    ProgressView("Loading…").frame(maxWidth: .infinity, minHeight: 220)
                case .failed:
                    ContentUnavailableView {
                        Label("Could not load content", systemImage: "wifi.exclamationmark")
                    } actions: {
                        Button("Retry") { model.reload() }
                    }
                case .loaded:
                    // Like Android, the resource sits on the page background, not on a card.
                    if let resource = model.resource {
                        ResourceCard(resource: resource, actions: actions(for: resource),
                                           style: .detailHeader,
                                           autoplayGifs: dependencies.session.autoplayGifs,
                                           isForeground: dependencies.isForeground,
                                           onTitleBottom: { titleBottom = $0 })
                            .padding(.horizontal, resource.kind == .link ? 0 : 16)
                            .padding(.top, resource.kind == .link && resource.photo != nil ? 0 : 12)
                    }
                }
                if model.actionFailed {
                    Label("Action failed. Try again.", systemImage: "exclamationmark.triangle")
                        .foregroundStyle(.red)
                        .padding(.horizontal, 16)
                }
                if model.phase == .loaded {
                    if model.kind == .link, !model.related.isEmpty { relatedSection }
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
        .sheet(item: $actionTarget) { target in
            ResourceActionsSheet(resource: target, root: model.resource ?? target,
                                       parent: screenshotParent(for: target),
                                       dependencies: dependencies,
                                       delete: { model.submit(.delete(target)) })
        }
        .confirmationDialog("Why downvote this link?", isPresented: Binding(
            get: { downvoteTarget != nil },
            set: { if !$0 { downvoteTarget = nil } }
        )) {
            ForEach(["duplicate", "spam", "fake", "wrong", "invalid"], id: \.self) { reason in
                Button(reason.capitalized) {
                    if let target = downvoteTarget {
                        model.submit(.voteDown(target, remove: false, reason: reason))
                    }
                    downvoteTarget = nil
                }
            }
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
        model.kind == .link ? String(localized: "Link") : String(localized: "Entry")
    }

    /// Direct children of the page's `LazyVStack`, so only visible comment threads are built and
    /// the next page loads when the last thread appears, not all at once.
    @ViewBuilder private var commentsSection: some View {
        Group {
            if model.kind == .link {
                Menu {
                    Button("Best") { model.selectCommentSort("best") }
                    Button("Newest") { model.selectCommentSort("newest") }
                    Button("Oldest") { model.selectCommentSort("oldest") }
                } label: {
                    DropdownLabel(title: commentSortTitle)
                }
                .accessibilityIdentifier("commentSort")
            } else {
                Text("Comments").font(.headline).padding(.horizontal, 4)
            }
        }
        .padding(.horizontal, 12)
        Group {
            if model.commentsLoading && model.comments.isEmpty {
                ProgressView("Loading…").frame(maxWidth: .infinity)
            } else if model.commentsError && model.comments.isEmpty {
                ThreadMoreButton(title: String(localized: "Retry comments")) { model.retryComments() }
            } else if model.comments.isEmpty {
                ContentUnavailableView("Nothing here yet", systemImage: "bubble")
            } else {
                ForEach(model.comments) { comment in
                    commentThread(comment)
                        .onAppear {
                            if comment.id == model.comments.last?.id { model.loadMoreComments() }
                        }
                }
                if model.commentsLoading { ProgressView("Loading…").frame(maxWidth: .infinity) }
                if model.nextCommentsError {
                    ThreadMoreButton(title: String(localized: "Retry next page")) { model.retryComments() }
                }
            }
        }
        .padding(.horizontal, 12)
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
            isForeground: dependencies.isForeground
        ) {
            if model.kind == .link, !state.exhausted, remaining > 0 || state.loading || state.error {
                ThreadMoreButton(
                    title: state.error ? String(localized: "Retry replies")
                        : String(localized: "Show all (\(remaining))"),
                    loading: state.loading
                ) {
                    model.loadReplies(for: comment.sourceID)
                }
            }
        }
    }

    private var commentSortTitle: String {
        switch model.commentSort {
        case "newest": String(localized: "Newest")
        case "oldest": String(localized: "Oldest")
        default: String(localized: "Best")
        }
    }

    /// Related links scroll sideways as compact cards, as on Android.
    private var relatedSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Related links").font(.headline).padding(.horizontal, 16)
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
            } else {
                dependencies.router.presentComposer(.createEntryComment(
                    entryID: model.id, replyTarget: replyParentID == nil ? nil : resource.author?.name))
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
                if resource.kind == .link && resource.vote.state != "negative" {
                    downvoteTarget = resource
                } else {
                    model.submit(.voteDown(resource, remove: resource.vote.state == "negative", reason: nil))
                }
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
            model.comments.first { $0.sourceID == resource.parentID }
        default: nil
        }
    }
}

import SwiftUI

struct NativeDetailView: View {
    @State private var model: DetailModel
    let dependencies: AppDependencies
    @State private var actionTarget: NativeResource?
    @State private var downvoteTarget: NativeResource?

    init(kind: NativeResourceKind, id: Int, dependencies: AppDependencies) {
        _model = State(initialValue: DetailModel(kind: kind, id: id,
                                                loader: dependencies.detailLoader,
                                                mutator: dependencies.detailMutator,
                                                updates: dependencies.resourceUpdates))
        self.dependencies = dependencies
    }

    var body: some View {
        ScrollView {
            LazyVStack(alignment: .leading, spacing: 12) {
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
                    if let resource = model.resource {
                        NativeResourceCard(resource: resource, actions: actions(for: resource),
                                           autoplayGifs: dependencies.session.autoplayGifs,
                                           isForeground: dependencies.isForeground)
                    }
                }
                if model.actionFailed {
                    Label("Action failed. Try again.", systemImage: "exclamationmark.triangle")
                        .foregroundStyle(.red)
                }
                if model.phase == .loaded {
                    commentsSection
                    if model.kind == .link, !model.related.isEmpty { relatedSection }
                }
            }
            .frame(maxWidth: 760)
            .frame(maxWidth: .infinity)
            .padding(12)
        }
        .refreshable { model.reload() }
        .task { model.start() }
        .onDisappear { model.stop() }
        .onChange(of: dependencies.resourceUpdates.revision) { _, _ in model.reconcileUpdates() }
        .onChange(of: dependencies.session.revision) { _, _ in model.sessionChanged() }
        .sheet(item: $actionTarget) { target in
            NativeResourceActionsSheet(resource: target, root: model.resource ?? target,
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
        .navigationTitle(model.kind == .link ? String(localized: "Link") : String(localized: "Entry"))
        .navigationBarTitleDisplayMode(.inline)
        .accessibilityIdentifier("detail-\(model.kind.rawValue)-\(model.id)")
    }

    private var commentsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Comments").font(.title3.bold())
                Spacer()
                if model.kind == .link {
                    Menu {
                        Button("Best") { model.selectCommentSort("best") }
                        Button("Newest") { model.selectCommentSort("newest") }
                        Button("Oldest") { model.selectCommentSort("oldest") }
                    } label: {
                        Label(commentSortTitle, systemImage: "line.3.horizontal.decrease")
                    }
                    .accessibilityIdentifier("commentSort")
                }
            }
            if model.commentsLoading && model.comments.isEmpty {
                ProgressView("Loading…").frame(maxWidth: .infinity)
            } else if model.commentsError && model.comments.isEmpty {
                Button("Retry comments") { model.retryComments() }
                    .buttonStyle(.bordered)
            } else if model.comments.isEmpty {
                ContentUnavailableView("Nothing here yet", systemImage: "bubble")
            } else {
                ForEach(model.comments) { comment in
                    NativeResourceCard(resource: comment,
                                       actions: actions(for: comment, replyParentID: comment.sourceID),
                                       autoplayGifs: dependencies.session.autoplayGifs,
                                       isForeground: dependencies.isForeground)
                        .onAppear {
                            if comment.id == model.comments.last?.id { model.loadMoreComments() }
                        }
                    if model.kind == .link { replies(for: comment.sourceID) }
                }
                if model.commentsLoading { ProgressView("Loading…").frame(maxWidth: .infinity) }
                if model.nextCommentsError {
                    Button("Retry next page") { model.retryComments() }
                        .buttonStyle(.bordered)
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

    @ViewBuilder private func replies(for commentID: Int) -> some View {
        let state = model.replies[commentID] ?? DetailModel.ReplyState()
        VStack(alignment: .leading, spacing: 8) {
            ForEach(state.rows) { reply in
                NativeResourceCard(resource: reply,
                                   actions: actions(for: reply, replyParentID: commentID),
                                   autoplayGifs: dependencies.session.autoplayGifs,
                                   isForeground: dependencies.isForeground)
            }
            if state.loading { ProgressView().padding(.leading, 20) }
            if !state.exhausted && !state.loading {
                Button(state.error ? "Retry replies" : "Show replies") {
                    model.loadReplies(for: commentID)
                }
                .buttonStyle(.bordered)
                .padding(.leading, 20)
            }
        }
    }

    private var relatedSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Related links").font(.title3.bold())
            ForEach(model.related) { item in
                HStack {
                    Button {
                        dependencies.router.navigate(.link(item.sourceID))
                    } label: {
                        Text(item.title.isEmpty ? item.body : item.title)
                            .multilineTextAlignment(.leading)
                    }
                    .buttonStyle(.plain)
                    Spacer()
                    Button {
                        requireAccount {
                            model.submit(.relatedVote(linkID: model.id, resource: item,
                                                      remove: item.vote.state == "positive", down: false))
                        }
                    } label: {
                        Label("\(item.vote.up)", systemImage: "hand.thumbsup")
                    }
                    .disabled(!(item.vote.canUp || item.vote.canUndo))
                    Button {
                        requireAccount {
                            model.submit(.relatedVote(linkID: model.id, resource: item,
                                                      remove: item.vote.state == "negative", down: true))
                        }
                    } label: {
                        Image(systemName: "hand.thumbsdown")
                    }
                    .disabled(!(item.vote.canDown || item.vote.canUndo))
                }
                .padding(12)
                .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 12))
            }
        }
    }

    private func actions(for resource: NativeResource, replyParentID: Int? = nil) -> ResourceActions {
        #if DEBUG
        let commentAction: (() -> Void)? = {
            if model.kind == .link {
                dependencies.router.presentComposer(.createLinkComment(
                    linkID: model.id, parentCommentID: replyParentID,
                    replyTarget: replyParentID == nil ? nil : resource.author?.name))
            } else {
                dependencies.router.presentComposer(.createEntryComment(
                    entryID: model.id, replyTarget: replyParentID == nil ? nil : resource.author?.name))
            }
        }
        #else
        let commentAction: (() -> Void)? = nil
        #endif
        return ResourceActions(
            openAuthor: { dependencies.router.navigate(.user($0)) },
            openTag: { dependencies.router.navigate(.tag($0)) },
            openURL: { UIApplication.shared.open($0) },
            voteUp: resource.vote.canUp || resource.vote.canUndo ? {
                requireAccount {
                    model.submit(.voteUp(resource, remove: resource.vote.state == "positive"))
                }
            } : nil,
            voteDown: (resource.kind == .link || resource.kind == .linkComment) &&
                (resource.vote.canDown || resource.vote.canUndo) ? {
                requireAccount {
                    if resource.kind == .link && resource.vote.state != "negative" {
                        downvoteTarget = resource
                    } else {
                        model.submit(.voteDown(resource, remove: resource.vote.state == "negative", reason: nil))
                    }
                }
            } : nil,
            favourite: {
                requireAccount { model.submit(.favourite(resource, enabled: !resource.favourite)) }
            },
            comment: commentAction,
            menu: { actionTarget = resource },
            surveyVote: resource.kind == .entry && resource.survey?.canVote == true ? { option in
                requireAccount { model.submit(.survey(entryID: resource.sourceID, option: option)) }
            } : nil,
            loadTweet: { try await dependencies.loadTweet($0) }
        )
    }

    private func requireAccount(_ action: () -> Void) {
        if dependencies.session.isLoggedIn { action() }
        else { dependencies.router.sheet = .login }
    }

    private func screenshotParent(for resource: NativeResource) -> NativeResource? {
        switch resource.kind {
        case .entryComment: model.resource
        case .linkComment:
            model.comments.first { $0.sourceID == resource.parentID }
        default: nil
        }
    }
}

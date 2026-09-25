import Foundation
import Observation
import PodkopShared

@MainActor @Observable
final class DetailModel {
    enum Phase: Equatable { case idle, loading, loaded, failed }
    struct ReplyState {
        var rows: [NativeResource] = []
        var nextPage = 1
        var loading = false
        var error = false
        var exhausted = false
    }

    let kind: NativeResourceKind
    let id: Int
    private(set) var phase: Phase = .idle
    private(set) var resource: NativeResource?
    private(set) var comments: [NativeResource] = []
    private(set) var related: [NativeResource] = []
    private(set) var commentsError = false
    private(set) var commentsLoading = false
    private(set) var nextCommentsError = false
    private(set) var commentsExhausted = false
    private(set) var replies: [Int: ReplyState] = [:]
    private(set) var commentSort = "best"
    private(set) var mutating = Set<ResourceIdentity>()
    private(set) var actionFailed = false

    private let loader: DetailLoading
    private let updates: ResourceUpdates
    private let mutator: DetailMutating
    private var generation = 0
    private var active: Task<Void, Never>?
    private var commentsTask: Task<Void, Never>?
    private var replyTasks: [Int: Task<Void, Never>] = [:]
    private var mutationTasks: [ResourceIdentity: Task<Void, Never>] = [:]
    private var nextCommentsPage = 2
    private var appliedUpdateRevision = 0

    init(kind: NativeResourceKind, id: Int, loader: DetailLoading,
         mutator: DetailMutating, updates: ResourceUpdates) {
        self.kind = kind
        self.id = id
        self.loader = loader
        self.mutator = mutator
        self.updates = updates
    }

    func start() {
        guard phase == .idle else { return }
        reload()
    }

    func reload() {
        generation += 1
        active?.cancel()
        commentsTask?.cancel()
        replyTasks.values.forEach { $0.cancel() }
        replyTasks.removeAll()
        replies = [:]
        commentsLoading = false
        commentsError = false
        nextCommentsError = false
        commentsExhausted = false
        nextCommentsPage = 2
        phase = resource == nil ? .loading : .loaded
        let token = generation
        active = Task { [weak self] in
            guard let self else { return }
            do {
                let value = try await loader.resource(kind: kind, id: id)
                guard token == generation, !Task.isCancelled else { return }
                resource = updates.reconcile(value)
                phase = .loaded
            } catch is CancellationError {
                return
            } catch {
                guard token == generation else { return }
                phase = resource == nil ? .failed : .loaded
            }
            guard token == generation, !Task.isCancelled else { return }
            active = nil
            loadFirstComments()
            if kind == .link {
                do {
                    let values = try await loader.related(linkID: id)
                    if token == generation, !Task.isCancelled { related = values.compactMap(updates.reconcile) }
                } catch { /* Related links do not hide the main resource. */ }
            }
        }
    }

    func selectCommentSort(_ sort: String) {
        guard kind == .link, ["best", "newest", "oldest"].contains(sort), commentSort != sort else { return }
        commentSort = sort
        loadFirstComments()
    }

    private func loadFirstComments() {
        commentsTask?.cancel()
        replyTasks.values.forEach { $0.cancel() }
        replyTasks.removeAll()
        replies = [:]
        commentsLoading = true
        commentsError = false
        nextCommentsError = false
        commentsExhausted = false
        nextCommentsPage = 2
        let token = generation
        let sort = commentSort
        commentsTask = Task { [weak self] in
            guard let self else { return }
            do {
                let page = try await loader.comments(kind: kind, id: id, page: 1, sort: sort)
                guard token == generation, sort == commentSort, !Task.isCancelled else { return }
                comments = unique(page.items.compactMap(updates.reconcile))
                commentsExhausted = page.items.isEmpty || page.total.map { comments.count >= $0 } == true
            } catch is CancellationError {
                return
            } catch {
                guard token == generation, sort == commentSort else { return }
                commentsError = true
            }
            commentsLoading = false
            commentsTask = nil
        }
    }

    func loadMoreComments() {
        guard phase == .loaded, !commentsLoading, !commentsError,
              !nextCommentsError, !commentsExhausted, commentsTask == nil else { return }
        commentsLoading = true
        let token = generation
        let pageNumber = nextCommentsPage
        let sort = commentSort
        commentsTask = Task { [weak self] in
            guard let self else { return }
            do {
                let page = try await loader.comments(kind: kind, id: id, page: pageNumber, sort: sort)
                guard token == generation, sort == commentSort, !Task.isCancelled else { return }
                let fresh = page.items.compactMap(updates.reconcile).filter { item in
                    !comments.contains { ResourceIdentity($0) == ResourceIdentity(item) }
                }
                comments.append(contentsOf: unique(fresh))
                nextCommentsPage += 1
                commentsExhausted = page.items.isEmpty || fresh.isEmpty ||
                    page.total.map { comments.count >= $0 } == true
            } catch is CancellationError {
                return
            } catch {
                guard token == generation, sort == commentSort else { return }
                nextCommentsError = true
            }
            commentsLoading = false
            commentsTask = nil
        }
    }

    func retryComments() {
        if commentsError { loadFirstComments() }
        else if nextCommentsError { nextCommentsError = false; loadMoreComments() }
    }

    func loadReplies(for commentID: Int) {
        guard kind == .link, replyTasks[commentID] == nil else { return }
        var state = replies[commentID] ?? ReplyState()
        guard !state.loading, !state.exhausted else { return }
        state.loading = true
        state.error = false
        replies[commentID] = state
        let page = state.nextPage
        let token = generation
        replyTasks[commentID] = Task { [weak self] in
            guard let self else { return }
            do {
                let result = try await loader.replies(linkID: id, commentID: commentID, page: page)
                guard token == generation, !Task.isCancelled else { return }
                var latest = replies[commentID] ?? ReplyState()
                let fresh = result.items.filter { item in
                    !latest.rows.contains { ResourceIdentity($0) == ResourceIdentity(item) }
                }
                latest.rows.append(contentsOf: unique(fresh))
                latest.nextPage += 1
                latest.exhausted = result.items.isEmpty || fresh.isEmpty ||
                    result.total.map { latest.rows.count >= $0 } == true
                latest.loading = false
                replies[commentID] = latest
            } catch is CancellationError {
                return
            } catch {
                guard token == generation else { return }
                replies[commentID]?.error = true
                replies[commentID]?.loading = false
            }
            replyTasks[commentID] = nil
        }
    }

    func stop() {
        generation += 1
        active?.cancel()
        commentsTask?.cancel()
        replyTasks.values.forEach { $0.cancel() }
        for identity in mutating {
            mutationTasks[identity]?.cancel()
            updates.publish(.invalidated, for: identity)
        }
        mutationTasks.removeAll()
        mutating.removeAll()
        active = nil
        commentsTask = nil
        replyTasks.removeAll()
        for id in replies.keys { replies[id]?.loading = false }
        commentsLoading = false
        if phase == .loading { phase = .idle }
    }

    func submit(_ mutation: DetailMutation) {
        let identity = mutation.identity
        guard !mutating.contains(identity) else { return }
        actionFailed = false
        mutating.insert(identity)
        let currentSession = updates.sessionRevision
        mutationTasks[identity] = Task { [weak self] in
            guard let self else { return }
            do {
                try await mutator.apply(mutation)
                guard !Task.isCancelled, currentSession == updates.sessionRevision else { return }
                if case .delete = mutation {
                    updates.publish(.deleted, for: identity)
                } else {
                    updates.publish(.invalidated, for: identity)
                }
            } catch is CancellationError {
                return
            } catch {
                guard currentSession == updates.sessionRevision else { return }
                actionFailed = true
            }
            mutating.remove(identity)
            mutationTasks[identity] = nil
        }
    }

    func sessionChanged() {
        for task in mutationTasks.values { task.cancel() }
        mutationTasks.removeAll()
        mutating.removeAll()
        actionFailed = false
        reload()
    }

    func reconcileUpdates() {
        guard appliedUpdateRevision != updates.revision else { return }
        let affected = [resource].compactMap { $0 } + comments + related
        let refresh = updates.needsReload(affected, since: appliedUpdateRevision)
        appliedUpdateRevision = updates.revision
        resource = resource.flatMap(updates.reconcile)
        comments = comments.compactMap(updates.reconcile)
        related = related.compactMap(updates.reconcile)
        if refresh { reload() }
    }

    private func unique(_ values: [NativeResource]) -> [NativeResource] {
        var seen = Set<ResourceIdentity>()
        return values.filter { seen.insert(ResourceIdentity($0)).inserted }
    }
}

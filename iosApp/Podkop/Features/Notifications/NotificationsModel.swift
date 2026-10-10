import Foundation
import Observation
import PodkopShared

@MainActor @Observable
final class NotificationsModel {
    private(set) var group: NotificationGroupKind = .entries
    private(set) var markingAll = false
    private(set) var markAllFailed = false
    let pager = ListPager<AppNotification>(key: \.id)
    /// Expanded grouped rows by row id, each paging through its group's own notifications.
    private(set) var expanded: [String: ListPager<AppNotification>] = [:]
    private let loader: NotificationsLoading

    init(loader: NotificationsLoading) { self.loader = loader }

    var rows: [NotificationRow] { NotificationRow.rows(for: pager.items, group: group) }

    /// Android offers mark-all only outside private messages and only with unread items.
    func canMarkAll(_ counts: NotificationCounts) -> Bool {
        group != .pm && counts[group] > 0 && !markingAll
    }

    func start() {
        // Leaving the screen stops loading; groups expanded meanwhile pick up where they were.
        expanded.values.forEach { $0.resume() }
        guard pager.phase == .idle else { return }
        reload(refreshingStatus: true)
    }

    func select(_ group: NotificationGroupKind) {
        guard group != self.group || pager.phase == .failed else { return }
        self.group = group
        markAllFailed = false
        collapseAll()
        reload(refreshingStatus: false)
    }

    func refresh() async {
        collapseAll()
        try? await loader.refreshStatus()
        await pager.refresh()
    }

    func stop() {
        pager.stop()
        expanded.values.forEach { $0.stop() }
    }

    /// Shows or hides the notifications inside a grouped row, like website's "Rozwiń".
    func toggleExpansion(_ row: NotificationRow) {
        if let members = expanded.removeValue(forKey: row.id) {
            members.stop()
            return
        }
        guard let groupID = row.groupID else { return }
        let members = ListPager<AppNotification>(key: \.id)
        expanded[row.id] = members
        let group = group, loader = loader
        members.load(first: loader.firstGroupRequest()) {
            try await loader.loadGroup(group, groupID: groupID, request: $0, loaded: $1)
        }
    }

    /// Loads the next page of an expanded group; website only ever shows the first.
    func showMore(_ row: NotificationRow) { expanded[row.id]?.loadNext() }

    /// Opens first; then, like website, marks just that notification read.
    func openedMember(_ member: NotificationRow, in row: NotificationRow) {
        guard !member.isRead, let id = member.notificationIDs.first, let members = expanded[row.id] else { return }
        let group = group, loader = loader
        Task { [weak self] in
            guard (try? await loader.markAsRead(group, id: id)) != nil, let self, self.group == group else { return }
            members.update { $0.id == id ? $0.markedRead() : $0 }
        }
    }

    /// Opens first; a single unread notification is then marked read in the background.
    func opened(_ row: NotificationRow) {
        guard !row.isRead else { return }
        if row.groupID != nil {
            // Like on website, opening a tag's stream marks its notifications read on the server.
            if case .tag(let name) = row.target { markTagRead(name) }
            return
        }
        guard row.notificationIDs.count == 1, let id = row.notificationIDs.first else { return }
        let group = group, loader = loader
        Task { [weak self] in
            guard (try? await loader.markAsRead(group, id: id)) != nil, let self, self.group == group else { return }
            self.pager.update { item in
                guard item.id == id else { return item }
                var read = item
                read.isRead = true
                return read
            }
        }
    }

    func markAll(_ counts: NotificationCounts) {
        guard canMarkAll(counts) else { return }
        markingAll = true
        markAllFailed = false
        let group = group, loader = loader
        Task { [weak self] in
            do {
                try await loader.markAllAsRead(group)
                guard let self else { return }
                if self.group == group {
                    self.pager.update { $0.markedRead() }
                    self.expanded.values.forEach { $0.update { $0.markedRead() } }
                }
                self.markingAll = false
            } catch {
                guard let self else { return }
                self.markingAll = false
                self.markAllFailed = !(error is CancellationError)
            }
        }
    }

    func dismissMarkAllFailure() { markAllFailed = false }

    private func markTagRead(_ name: String) {
        pager.update { $0.tagName == name ? $0.markedRead() : $0 }
        expanded.values.forEach { $0.update { $0.tagName == name ? $0.markedRead() : $0 } }
    }

    private func collapseAll() {
        expanded.values.forEach { $0.stop() }
        expanded = [:]
    }

    private func reload(refreshingStatus: Bool) {
        let group = group, loader = loader
        if refreshingStatus { Task { try? await loader.refreshStatus() } }
        pager.load(first: loader.firstRequest(group)) { try await loader.load(group, request: $0, loaded: $1) }
    }
}

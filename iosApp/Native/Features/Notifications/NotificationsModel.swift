import Foundation
import Observation
import PodkopShared

@MainActor @Observable
final class NotificationsModel {
    private(set) var group: NotificationGroupKind = .entries
    private(set) var markingAll = false
    private(set) var markAllFailed = false
    let pager = ListPager<NativeNotification>(key: \.id)
    private let loader: NotificationsLoading

    init(loader: NotificationsLoading) { self.loader = loader }

    var rows: [NotificationRow] { NotificationRow.rows(for: pager.items, group: group) }

    /// Android offers mark-all only outside private messages and only with unread items.
    func canMarkAll(_ counts: NotificationCounts) -> Bool {
        group != .pm && counts[group] > 0 && !markingAll
    }

    func start() {
        guard pager.phase == .idle else { return }
        reload(refreshingStatus: true)
    }

    func select(_ group: NotificationGroupKind) {
        guard group != self.group || pager.phase == .failed else { return }
        self.group = group
        markAllFailed = false
        reload(refreshingStatus: false)
    }

    func refresh() async {
        try? await loader.refreshStatus()
        await pager.refresh()
    }

    func stop() { pager.stop() }

    /// Opens first; a single unread notification is then marked read in the background.
    func opened(_ row: NotificationRow) {
        guard !row.isRead, row.notificationIDs.count == 1, let id = row.notificationIDs.first else { return }
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
                    self.pager.update { item in
                        var read = item
                        read.isRead = true
                        return read
                    }
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

    private func reload(refreshingStatus: Bool) {
        let group = group, loader = loader
        if refreshingStatus { Task { try? await loader.refreshStatus() } }
        pager.load(first: loader.firstRequest(group)) { try await loader.load(group, request: $0, loaded: $1) }
    }
}

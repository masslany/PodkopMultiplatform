import SwiftUI

struct NotificationsView: View {
    @Environment(\.openURL) private var openURL
    @State private var model: NotificationsModel
    let tab: AppTab
    let dependencies: AppDependencies
    private var counts: NotificationCounts { dependencies.session.notificationCounts }

    init(tab: AppTab, dependencies: AppDependencies) {
        _model = State(initialValue: NotificationsModel(loader: dependencies.notificationsLoader))
        self.tab = tab
        self.dependencies = dependencies
    }

    var body: some View {
        PodkopList {
            Section {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(NotificationGroupKind.allCases, id: \.self) { group in
                            Button { model.select(group) } label: {
                                HStack(spacing: 4) {
                                    Text(title(for: group))
                                    if counts[group] > 0 {
                                        Text(verbatim: "\(counts[group])").font(.caption.bold())
                                            .padding(.horizontal, 6).padding(.vertical, 1)
                                            .background(.red, in: Capsule()).foregroundStyle(.white)
                                    }
                                }
                            }
                            .buttonStyle(.bordered)
                            .tint(model.group == group ? ContentTokens.brand : .secondary)
                            .accessibilityAddTraits(model.group == group ? .isSelected : [])
                            .accessibilityValue(counts[group] > 0 ? String(localized: .commonUnread2(counts[group])) : "")
                            .accessibilityIdentifier("notificationGroup-\(group.rawValue)")
                        }
                    }
                }
                if model.group != .pm {
                    Button {
                        model.markAll(counts)
                    } label: {
                        if model.markingAll { ProgressView() } else { Text(.notificationsMarkAllRead) }
                    }
                    .disabled(!model.canMarkAll(counts))
                    .accessibilityIdentifier("notificationsMarkAll")
                }
            }
            rows
        }
        .refreshable { await model.refresh() }
        .navigationTitle(.commonNotifications)
        .task { model.start() }
        .onDisappear { model.stop() }
        .alert(.commonCouldNotCompleteAction,
               isPresented: Binding(get: { model.markAllFailed }, set: { if !$0 { model.dismissMarkAllFailure() } })) {
            Button(.commonOk, role: .cancel) {}
        }
    }

    @ViewBuilder private var rows: some View {
        let pager = model.pager
        switch pager.phase {
        case .idle, .loading:
            ProgressView(.commonLoading).frame(maxWidth: .infinity)
        case .failed:
            HStack {
                Text(.commonCouldNotLoadContent).foregroundStyle(.secondary)
                Spacer()
                Button(.commonRetry) { pager.retry() }
            }
        case .loaded where pager.items.isEmpty:
            Text(.notificationsNoNotificationsInCategory).foregroundStyle(.secondary)
        case .loaded:
            let rows = model.rows
            ForEach(rows) { row in
                Button { open(row) } label: { NotificationRowView(row: row) }
                    .foregroundStyle(.primary)
                    .disabled(row.target == .none)
                    .onAppear { if row.id == rows.last?.id { pager.loadNext() } }
            }
            PagerFooter(pager: pager)
        }
    }

    private func open(_ row: NotificationRow) {
        let router = dependencies.router
        switch row.target {
        case .link(let id): router.navigate(.link(id), in: tab)
        case .entry(let id): router.navigate(.entry(id), in: tab)
        case .conversation(let name): router.navigate(.conversation(name), in: tab)
        case .profile(let name): router.navigate(.user(name), in: tab)
        case .tag(let name): router.navigate(.tag(name), in: tab)
        case .external(let url): openURL(url)
        case .none: return
        }
        model.opened(row)
    }

    private func title(for group: NotificationGroupKind) -> String {
        switch group {
        case .entries: String(localized: .commonEntries)
        case .pm: String(localized: .notificationsPrivateMessages)
        case .tags: String(localized: .commonTags)
        case .observedDiscussions: String(localized: .notificationsObservedDiscussions)
        }
    }
}

struct NotificationRowView: View {
    let row: NotificationRow

    var body: some View {
        HStack(alignment: .top, spacing: 10) {
            Circle().fill(row.isRead ? Color.clear : Color.red).frame(width: 8, height: 8).padding(.top, 6)
                .accessibilityHidden(true)
            if let actor = row.actor {
                AvatarView(url: row.actorAvatarURL, name: actor, size: 32)
            }
            VStack(alignment: .leading, spacing: 4) {
                if let actor = row.actor {
                    Text(actor).font(.subheadline.bold()).foregroundStyle(authorColor(row.actorColor))
                }
                Text(action).font(.subheadline).foregroundStyle(.secondary)
                if let content { Text(content).font(.subheadline).lineLimit(3) }
                Text(row.createdAt.formatted(.relative(presentation: .named))).font(.caption).foregroundStyle(.secondary)
            }
        }
        .accessibilityElement(children: .combine)
        .accessibilityValue(row.isRead ? "" : String(localized: .commonUnread))
    }

    /// Mirrors Android's title: grouped tag counts, observed comment actions, or the headline.
    private var action: String {
        switch row.grouped {
        case .entries: return String(localized: .notificationsHaveNewEntriesIn(row.groupCount))
        case .links: return String(localized: .notificationsHaveNewLinksIn(row.groupCount))
        case .generic: return String(localized: .notificationsHaveNewNotificationsIn(row.groupCount))
        case nil: break
        }
        switch row.observed {
        case .entry:
            return row.notificationIDs.count > 1
                ? String(localized: .notificationsNewCommentsEntry(row.groupCount))
                : String(localized: .notificationsCommentEntryObserve)
        case .link:
            return row.notificationIDs.count > 1
                ? String(localized: .notificationsNewCommentsLink(row.groupCount))
                : String(localized: .notificationsCommentLinkObserve)
        case nil:
            return row.headline ?? String(localized: .notificationsNotification)
        }
    }

    private var content: String? {
        if row.grouped != nil { return row.tagName.map { "#\($0)" } }
        if row.observed != nil { return row.observedTitle }
        return nil
    }
}

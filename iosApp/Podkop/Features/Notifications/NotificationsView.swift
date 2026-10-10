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
                Group {
                    if row.groupID != nil {
                        // As on Android, the group's "Rozwiń" sits inside its row.
                        VStack(alignment: .trailing, spacing: 4) {
                            rowButton(row).buttonStyle(.plain)
                            expandButton(row)
                        }
                    } else {
                        rowButton(row)
                    }
                }
                .onAppear { if row.id == rows.last?.id { pager.loadNext() } }
                if let members = model.expanded[row.id] { groupMembers(members, of: row) }
            }
            PagerFooter(pager: pager)
        }
    }

    private func rowButton(_ row: NotificationRow) -> some View {
        Button { open(row) } label: {
            NotificationRowView(row: row).frame(maxWidth: .infinity, alignment: .leading).contentShape(Rectangle())
        }
        .foregroundStyle(.primary)
        .disabled(row.target == .none)
    }

    /// Like websites's "Rozwiń": shows the group's own notifications under its row.
    private func expandButton(_ row: NotificationRow) -> some View {
        let expanded = model.expanded[row.id] != nil
        return Button {
            model.toggleExpansion(row)
        } label: {
            Label(expanded ? String(localized: .notificationsGroupedCollapse)
                           : String(localized: .notificationsGroupedExpand),
                  systemImage: expanded ? "chevron.up" : "chevron.down")
                .font(.subheadline)
        }
        .buttonStyle(.borderless)
        .foregroundStyle(.secondary)
        .accessibilityIdentifier("notificationGroupExpand-\(row.id)")
    }

    @ViewBuilder private func groupMembers(_ members: ListPager<AppNotification>, of row: NotificationRow) -> some View {
        switch members.phase {
        case .idle, .loading:
            ProgressView(.commonLoading).frame(maxWidth: .infinity)
        case .failed:
            Button(.commonRetry) { members.retry() }
                .buttonStyle(.borderless)
                .foregroundStyle(.secondary)
                .frame(maxWidth: .infinity)
        case .loaded:
            ForEach(NotificationRow.memberRows(members.items)) { member in
                Button { open(member, in: row) } label: {
                    NotificationRowView(row: member).padding(.leading, 20)
                }
                .foregroundStyle(.primary)
                .disabled(member.target == .none)
                .accessibilityIdentifier("notificationGroupMember-\(member.id)")
            }
            PagerFooter(pager: members)
            // website only shows a group's first page; the rest load on request.
            if !members.exhausted, !members.nextLoading, !members.nextError {
                Button(.notificationsGroupedShowMore) { model.showMore(row) }
                    .buttonStyle(.borderless)
                    .foregroundStyle(.secondary)
                    .frame(maxWidth: .infinity)
                    .accessibilityIdentifier("notificationGroupShowMore-\(row.id)")
            }
        }
    }

    private func open(_ row: NotificationRow) {
        guard navigate(to: row.target, tagKind: row.grouped?.tagKind) else { return }
        model.opened(row)
    }

    private func open(_ member: NotificationRow, in row: NotificationRow) {
        guard navigate(to: member.target, tagKind: nil) else { return }
        model.openedMember(member, in: row)
    }

    /// Returns false when there is nothing to open.
    private func navigate(to target: NotificationTargetValue, tagKind: TagModel.Kind?) -> Bool {
        let router = dependencies.router
        switch target {
        case .link(let id): router.navigate(.link(id), in: tab)
        case .entry(let id): router.navigate(.entry(id), in: tab)
        case .conversation(let name): router.navigate(.conversation(name), in: tab)
        case .profile(let name): router.navigate(.user(name), in: tab)
        case .tag(let name): router.navigate(tagKind.map { .tagContent(name, $0) } ?? .tag(name), in: tab)
        case .external(let url): openURL(url)
        case .none: return false
        }
        return true
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
            // Keeps the separator at the text even when a group's "Rozwiń" follows it.
            .alignmentGuide(.listRowSeparatorLeading) { $0[.leading] }
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
        // Like website: "used #tag in an entry" or "added a link tagged #tag".
        switch row.tagged {
        case .entry: return String(localized: .notificationsTagEntryAction(row.tagName ?? ""))
        case .link: return String(localized: .notificationsTagLinkAction(row.tagName ?? ""))
        case nil: break
        }
        switch row.observed {
        case .entry:
            return row.groupID != nil
                ? String(localized: .notificationsNewCommentsEntry(row.groupCount))
                : String(localized: .notificationsCommentEntryObserve)
        case .link:
            return row.groupID != nil
                ? String(localized: .notificationsNewCommentsLink(row.groupCount))
                : String(localized: .notificationsCommentLinkObserve)
        case nil:
            return row.headline ?? String(localized: .notificationsNotification)
        }
    }

    private var content: String? {
        if row.grouped != nil { return row.tagName.map { "#\($0)" } }
        if row.observed != nil || row.tagged != nil { return row.observedTitle }
        return nil
    }
}

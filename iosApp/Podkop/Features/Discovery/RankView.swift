import SwiftUI

struct RankView: View {
    @State private var model: RankModel
    let tab: AppTab
    let dependencies: AppDependencies

    init(tab: AppTab, dependencies: AppDependencies) {
        _model = State(initialValue: RankModel(loader: dependencies.collectionLoader))
        self.tab = tab
        self.dependencies = dependencies
    }

    var body: some View {
        PodkopList {
            switch model.pager.phase {
            case .idle, .loading:
                ProgressView(.commonLoading).frame(maxWidth: .infinity, minHeight: 180)
            case .failed:
                ContentUnavailableView {
                    Label(.commonCouldNotLoadContent, systemImage: "wifi.exclamationmark")
                } actions: {
                    Button(.commonRetry) { model.pager.retry() }
                }
            case .loaded:
                if model.pager.refreshError {
                    Label(.commonCouldNotRefresh, systemImage: "exclamationmark.triangle")
                }
                Section {
                    ForEach(model.pager.items) { user in
                        Button { dependencies.router.navigate(.user(user.username), in: tab) } label: {
                            RankRow(user: user)
                        }
                        .foregroundStyle(.primary)
                        .onAppear { model.pager.loadNextIfNeeded(after: user) }
                    }
                } header: {
                    // Android's `RankTableHeader`, lined up with the rows' position column.
                    HStack(spacing: 12) {
                        Text(.discoveryRankHeaderPosition).frame(minWidth: 44)
                        Text(.discoveryRankHeaderUser)
                    }
                    .font(.subheadline.weight(.medium))
                    .padding(.leading, 6)
                    .textCase(nil)
                    .accessibilityHidden(true)
                }
                PagerFooter(pager: model.pager)
            }
        }
        .refreshable { await model.refresh() }
        .navigationTitle(.commonRank)
        .task { model.start() }
        .onDisappear { model.stop() }
    }
}

private struct RankRow: View {
    let user: RankUser

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            VStack(spacing: 2) {
                Text(verbatim: "#\(user.position)").font(.headline.monospacedDigit())
                if user.trend != 0 {
                    Label(String(abs(user.trend)), systemImage: user.trend > 0 ? "arrow.up" : "arrow.down")
                        .font(.caption2)
                        .foregroundStyle(user.trend > 0 ? .green : .red)
                        .accessibilityLabel(user.trend > 0
                            ? String(localized: .discoveryUp(abs(user.trend)))
                            : String(localized: .discoveryDown(abs(user.trend))))
                }
            }
            .frame(minWidth: 44)
            VStack(alignment: .leading, spacing: 6) {
                UserIdentityRow(username: user.username, color: user.color, gender: user.gender,
                                detail: memberSince, avatarURL: user.avatarURL)
                ViewThatFits {
                    HStack(spacing: 12) { counts }
                    VStack(alignment: .leading, spacing: 2) { counts }
                }
                .font(.caption)
                .foregroundStyle(.secondary)
            }
        }
        .accessibilityElement(children: .combine)
    }

    @ViewBuilder private var counts: some View {
        Text(.discoveryActions(user.actions))
        Text(.discoveryLinks(user.links))
        Text(.discoveryEntries(user.entries))
        Text(.commonFollowers(user.followers))
    }

    private var memberSince: String? {
        guard let raw = user.memberSince, let date = Dates.parse(raw) else { return nil }
        let text = RelativeDateTimeFormatter().localizedString(for: date, relativeTo: Date())
        return String(localized: .commonJoined(text))
    }
}

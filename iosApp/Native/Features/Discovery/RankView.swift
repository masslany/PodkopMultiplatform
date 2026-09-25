import SwiftUI

struct NativeRankView: View {
    @State private var model: RankModel
    let tab: AppTab
    let dependencies: AppDependencies

    init(tab: AppTab, dependencies: AppDependencies) {
        _model = State(initialValue: RankModel(loader: dependencies.collectionLoader))
        self.tab = tab
        self.dependencies = dependencies
    }

    var body: some View {
        List {
            switch model.pager.phase {
            case .idle, .loading:
                ProgressView("Loading…").frame(maxWidth: .infinity, minHeight: 180)
            case .failed:
                ContentUnavailableView {
                    Label("Could not load content", systemImage: "wifi.exclamationmark")
                } actions: {
                    Button("Retry") { model.pager.retry() }
                }
            case .loaded:
                if model.pager.refreshError {
                    Label("Could not refresh", systemImage: "exclamationmark.triangle")
                }
                ForEach(model.pager.items) { user in
                    Button { dependencies.router.navigate(.user(user.username), in: tab) } label: {
                        RankRow(user: user)
                    }
                    .foregroundStyle(.primary)
                    .onAppear { model.pager.loadNextIfNeeded(after: user) }
                }
                PagerFooter(pager: model.pager)
            }
        }
        .refreshable { await model.refresh() }
        .navigationTitle("Rank")
        .task { model.start() }
        .onDisappear { model.stop() }
    }
}

private struct RankRow: View {
    let user: NativeRankUser

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            VStack(spacing: 2) {
                Text("#\(user.position)").font(.headline.monospacedDigit())
                if user.trend != 0 {
                    Label("\(abs(user.trend))", systemImage: user.trend > 0 ? "arrow.up" : "arrow.down")
                        .font(.caption2)
                        .foregroundStyle(user.trend > 0 ? .green : .red)
                        .accessibilityLabel(user.trend > 0
                            ? String(localized: "Up \(abs(user.trend))")
                            : String(localized: "Down \(abs(user.trend))"))
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
        Text("Actions: \(user.actions)")
        Text("Links: \(user.links)")
        Text("Entries: \(user.entries)")
        Text("Followers: \(user.followers)")
    }

    private var memberSince: String? {
        guard let raw = user.memberSince, let date = NativeDates.parse(raw) else { return nil }
        let text = RelativeDateTimeFormatter().localizedString(for: date, relativeTo: Date())
        return String(localized: "Joined \(text)")
    }
}

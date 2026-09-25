import SwiftUI

extension ResourceActions {
    /// Navigation-only card actions shared by feeds and discovery lists.
    @MainActor
    static func navigation(for item: NativeResource, in tab: AppTab,
                           dependencies: AppDependencies) -> ResourceActions {
        let router = dependencies.router
        let open: (() -> Void)? = switch item.kind {
        case .link: { router.navigate(.link(item.sourceID), in: tab) }
        case .entry: { router.navigate(.entry(item.sourceID), in: tab) }
        case .linkComment: item.parentID.map { id in { router.navigate(.link(id), in: tab) } }
        case .entryComment: item.parentID.map { id in { router.navigate(.entry(id), in: tab) } }
        case .unknown: nil
        }
        return ResourceActions(
            open: open,
            openAuthor: { router.navigate(.user($0), in: tab) },
            openTag: { router.navigate(.tag($0), in: tab) },
            openURL: { UIApplication.shared.open($0) },
            comment: open,
            loadTweet: { try await dependencies.loadTweet($0) }
        )
    }
}

/// Resource rows for a `ListPager`, including empty, failure, and next-page states.
struct PagedResourceRows: View {
    let pager: ListPager<NativeResource>
    let tab: AppTab
    let dependencies: AppDependencies
    var emptyTitle: LocalizedStringKey = "Nothing here yet"

    var body: some View {
        LazyVStack(spacing: 12) {
            if pager.refreshError {
                HStack {
                    Label("Could not refresh", systemImage: "exclamationmark.triangle")
                    Spacer()
                    Button("Retry") { Task { await pager.refresh() } }
                }
                .font(.subheadline)
                .padding(10)
                .background(.thinMaterial, in: RoundedRectangle(cornerRadius: 10))
            }
            switch pager.phase {
            case .idle, .loading:
                ProgressView("Loading…").frame(maxWidth: .infinity, minHeight: 180)
            case .failed:
                ContentUnavailableView {
                    Label("Could not load content", systemImage: "wifi.exclamationmark")
                } actions: {
                    Button("Retry") { pager.retry() }
                }
            case .loaded where pager.items.isEmpty:
                ContentUnavailableView(emptyTitle, systemImage: "tray")
            case .loaded:
                ForEach(pager.items) { item in
                    NativeResourceCard(
                        resource: item,
                        actions: .navigation(for: item, in: tab, dependencies: dependencies),
                        autoplayGifs: dependencies.session.autoplayGifs,
                        isForeground: dependencies.isForeground
                    )
                    .onAppear { pager.loadNextIfNeeded(after: item) }
                }
                PagerFooter(pager: pager)
            }
        }
    }
}

struct PagerFooter<Item>: View {
    let pager: ListPager<Item>

    var body: some View {
        if pager.nextLoading { ProgressView("Loading…").padding() }
        if pager.nextError {
            Button("Retry next page") { pager.retry() }.buttonStyle(.bordered)
        }
    }
}

/// Compact identity row for users in suggestions and rankings.
struct UserIdentityRow: View {
    let username: String
    let color: String?
    let gender: String?
    var detail: String?

    var body: some View {
        HStack(spacing: 10) {
            VStack(spacing: 2) {
                Circle().fill(ContentTokens.brand.opacity(0.16))
                    .frame(width: 36, height: 36)
                    .overlay(Text(String(username.prefix(1)).uppercased()).font(.headline))
                if let tint = genderTint {
                    Capsule().fill(tint).frame(width: 24, height: 3)
                }
            }
            .accessibilityHidden(true)
            VStack(alignment: .leading, spacing: 2) {
                Text(username).font(.body.bold()).foregroundStyle(authorColor(color))
                if let detail {
                    Text(detail).font(.caption).foregroundStyle(.secondary)
                }
            }
        }
    }

    private var genderTint: Color? {
        switch gender {
        case "male": .blue
        case "female": .pink
        default: nil
        }
    }
}

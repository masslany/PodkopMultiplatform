import SwiftUI

struct NativeObservedView: View {
    @State private var model: ObservedModel
    let tab: AppTab
    let dependencies: AppDependencies

    init(tab: AppTab, dependencies: AppDependencies) {
        _model = State(initialValue: ObservedModel(loader: dependencies.collectionLoader,
                                                   updates: dependencies.resourceUpdates))
        self.tab = tab
        self.dependencies = dependencies
    }

    var body: some View {
        CollectionScroll(refresh: model.refresh) {
            Picker("Type", selection: Binding(get: { model.kind }, set: { model.select($0) })) {
                ForEach(ObservedModel.Kind.allCases, id: \.self) { Text(title(for: $0)).tag($0) }
            }
            .pickerStyle(.segmented)
            .accessibilityIdentifier("observedType")
        } rows: {
            rows
        }
        .navigationTitle("Observed")
        .task { model.start() }
        .onDisappear { model.stop() }
        .onChange(of: dependencies.resourceUpdates.revision) { _, _ in
            model.reconcile(dependencies.resourceUpdates)
        }
    }

    @ViewBuilder private var rows: some View {
        let pager = model.pager
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
            LazyVStack(spacing: 12) {
                ForEach(pager.items) { item in
                    VStack(alignment: .leading, spacing: 6) {
                        if let count = item.newContentCount, count > 0,
                           item.resource.kind == .link || item.resource.kind == .entry {
                            Label(item.resource.kind == .link
                                  ? String(localized: "\(count) new comments on an observed link")
                                  : String(localized: "\(count) new comments on an observed entry"),
                                  systemImage: "bell.badge")
                                .font(.caption.bold())
                                .foregroundStyle(ContentTokens.brand)
                        }
                        ResourceListRow(item: item.resource, tab: tab, dependencies: dependencies)
                    }
                    .onAppear { pager.loadNextIfNeeded(after: item) }
                }
                PagerFooter(pager: pager)
            }
        }
    }

    private var emptyTitle: LocalizedStringKey {
        switch model.kind {
        case .all: "Nothing observed yet"
        case .profiles: "No content from observed profiles"
        case .discussions: "No observed discussions"
        case .tags: "No content from observed tags"
        }
    }

    private func title(for kind: ObservedModel.Kind) -> String {
        switch kind {
        case .all: String(localized: "All")
        case .profiles: String(localized: "Profiles")
        case .discussions: String(localized: "Discussions")
        case .tags: String(localized: "Tags")
        }
    }
}

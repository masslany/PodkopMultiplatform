import SwiftUI

struct ObservedView: View {
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
            Picker(.discoveryType, selection: Binding(get: { model.kind }, set: { model.select($0) })) {
                ForEach(ObservedModel.Kind.allCases, id: \.self) { Text(title(for: $0)).tag($0) }
            }
            .pickerStyle(.segmented)
            .accessibilityIdentifier("observedType")
        } rows: {
            rows
        }
        .navigationTitle(.discoveryObserved)
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
            ProgressView(.commonLoading).frame(maxWidth: .infinity, minHeight: 180)
        case .failed:
            ContentUnavailableView {
                Label(.commonCouldNotLoadContent, systemImage: "wifi.exclamationmark")
            } actions: {
                Button(.commonRetry) { pager.retry() }
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
                                  ? String(localized: .discoveryNewCommentsObservedLink(count))
                                  : String(localized: .discoveryNewCommentsObservedEntry(count)),
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

    private var emptyTitle: LocalizedStringResource {
        switch model.kind {
        case .all: .discoveryNothingObservedYet
        case .profiles: .discoveryNoContentFromObserved
        case .discussions: .discoveryNoObservedDiscussions
        case .tags: .discoveryNoContentFromObservedTags
        }
    }

    private func title(for kind: ObservedModel.Kind) -> String {
        switch kind {
        case .all: String(localized: .commonAll)
        case .profiles: String(localized: .commonProfiles)
        case .discussions: String(localized: .discoveryDiscussions)
        case .tags: String(localized: .commonTags)
        }
    }
}

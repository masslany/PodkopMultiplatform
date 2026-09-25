import SwiftUI

struct NativeFeedView: View {
    @State private var model: FeedModel
    let router: AppRouter
    let session: SessionModel
    let dependencies: AppDependencies

    init(tab: AppTab, dependencies: AppDependencies) {
        _model = State(initialValue: FeedModel(tab: tab, loggedIn: dependencies.session.isLoggedIn,
                                               loader: dependencies.feedLoader,
                                               updates: dependencies.resourceUpdates))
        router = dependencies.router
        session = dependencies.session
        self.dependencies = dependencies
    }

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                if model.query.tab == .links, !model.hits.isEmpty { hitStrip }
                controls
                if model.refreshError {
                    HStack {
                        Label("Could not refresh", systemImage: "exclamationmark.triangle")
                        Spacer()
                        Button("Retry") { Task { await model.refresh() } }
                    }
                    .font(.subheadline)
                    .padding(10)
                    .background(WykopTheme.card, in: RoundedRectangle(cornerRadius: 12, style: .continuous))
                }
                if model.phase == .loading && model.items.isEmpty {
                    ProgressView("Loading…").frame(maxWidth: .infinity, minHeight: 180)
                } else if model.phase == .failed && model.items.isEmpty {
                    ContentUnavailableView {
                        Label("Could not load content", systemImage: "wifi.exclamationmark")
                    } actions: {
                        Button("Retry") { model.retry() }
                    }
                } else if model.items.isEmpty && model.phase == .loaded {
                    ContentUnavailableView("Nothing here yet", systemImage: "tray")
                } else {
                    if model.gallery {
                        LazyVGrid(columns: [GridItem(.adaptive(minimum: 260), spacing: 12)], spacing: 12) {
                            rows
                        }
                    } else {
                        LazyVStack(spacing: 12) { rows }
                    }
                    pageFooter
                }
            }
            .padding(.horizontal, 12)
            .padding(.bottom, 20)
            .frame(maxWidth: 900)
            .frame(maxWidth: .infinity)
        }
        .refreshable { await model.refresh() }
        .task { model.start() }
        .onDisappear { model.stop() }
        .onChange(of: session.revision) { _, value in
            model.setSession(session.isLoggedIn, revision: value)
        }
        .onChange(of: session.isLoggedIn) { _, value in
            model.setSession(value, revision: session.revision)
        }
        .onChange(of: dependencies.resourceUpdates.revision) { _, _ in
            model.reconcile(dependencies.resourceUpdates)
        }
        .onChange(of: dependencies.resourceUpdates.feedRevision) { _, _ in
            Task { await model.refresh() }
        }
        .accessibilityIdentifier("feed-\(model.query.tab.rawValue)")
    }

    private var controls: some View {
        HStack {
            Menu {
                if model.query.tab == .entries {
                    sortItem("Hot", value: "hot")
                    sortItem("Newest", value: "newest")
                    sortItem("Active", value: "active")
                } else {
                    sortItem("Newest", value: "newest")
                    sortItem("Active", value: "active")
                    if model.query.tab == .upcoming {
                        sortItem("Commented", value: "commented")
                        sortItem("Digged", value: "digged")
                    }
                }
            } label: {
                DropdownLabel(title: sortTitle)
            }
            .accessibilityIdentifier("feedSort")
            if model.query.tab == .entries && model.query.sort == "hot" {
                Menu {
                    ForEach([2, 6, 12, 24], id: \.self) { hours in
                        Button("\(hours) h") { model.select(sort: "hot", hotHours: hours) }
                    }
                } label: {
                    DropdownLabel(title: "\(model.query.hotHours) h", systemImage: "clock")
                }
                .accessibilityIdentifier("hotPeriod")
            }
            Spacer()
            Button { model.gallery.toggle() } label: {
                Image(systemName: model.gallery ? "list.bullet" : "square.grid.2x2")
                    .font(.subheadline.weight(.semibold))
                    .frame(width: 36, height: 34)
                    .background(WykopTheme.cardInset, in: RoundedRectangle(cornerRadius: 10, style: .continuous))
            }
            .buttonStyle(.plain)
            .accessibilityLabel(model.gallery ? "List view" : "Gallery view")
            .accessibilityIdentifier("feedGallery")
        }
        .padding(.top, 8)
    }

    private func sortItem(_ title: LocalizedStringKey, value: String) -> some View {
        Button(title) { model.select(sort: value) }
    }

    private var sortTitle: String {
        switch model.query.sort {
        case "hot": String(localized: "Hot")
        case "active": String(localized: "Active")
        case "commented": String(localized: "Commented")
        case "digged": String(localized: "Digged")
        default: String(localized: "Newest")
        }
    }

    private var hitStrip: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Label("Hits", systemImage: "flame.fill")
                    .font(.headline)
                    .labelStyle(.titleAndIcon)
                    .foregroundStyle(WykopTheme.hotOrange)
                Spacer()
                Button { router.navigate(.hits, in: .links) } label: {
                    HStack(spacing: 2) {
                        Text("See all")
                        Image(systemName: "chevron.right").font(.caption.weight(.semibold))
                    }
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                }
                .buttonStyle(.plain)
            }
            ScrollView(.horizontal) {
                LazyHStack(spacing: 10) {
                    ForEach(model.hits) { item in
                        HitTile(resource: item) { router.navigate(.link(item.sourceID), in: .links) }
                    }
                }
            }
            .scrollIndicators(.hidden)
        }
    }

    @ViewBuilder private var rows: some View {
        ForEach(model.items) { item in
            ResourceListRow(item: item, tab: model.query.tab, dependencies: dependencies)
                .onAppear {
                    if item.id == model.items.last?.id { model.loadNext() }
                }
        }
    }

    @ViewBuilder private var pageFooter: some View {
        if model.nextLoading { ProgressView("Loading…").padding() }
        if model.nextError {
            Button("Retry next page") { model.retry() }.buttonStyle(.bordered)
        }
    }
}

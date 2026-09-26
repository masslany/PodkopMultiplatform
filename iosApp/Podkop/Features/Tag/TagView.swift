import SwiftUI

struct TagView: View {
    @State private var model: TagModel
    let tab: AppTab
    let dependencies: AppDependencies
    private var session: SessionModel { dependencies.session }

    init(tag: String, tab: AppTab, dependencies: AppDependencies) {
        _model = State(initialValue: TagModel(tag: tag, isLoggedIn: dependencies.session.isLoggedIn,
                                              loader: dependencies.tagLoader,
                                              updates: dependencies.resourceUpdates))
        self.tab = tab
        self.dependencies = dependencies
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                header
                controls
                if model.gallery { gallery } else {
                    PagedResourceRows(pager: model.pager, tab: tab, dependencies: dependencies,
                                      emptyTitle: .commonNoResults)
                }
            }
            .padding(.horizontal, 12)
            .padding(.bottom, 20)
            .frame(maxWidth: 900)
            .frame(maxWidth: .infinity)
        }
        .refreshable { await model.refresh() }
        .navigationTitle("#" + model.tag)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button { model.gallery.toggle() } label: {
                    Image(systemName: model.gallery ? "list.bullet" : "square.grid.2x2")
                }
                .accessibilityLabel(model.gallery ? .commonListView : .commonGalleryView)
                .accessibilityIdentifier("tagGallery")
            }
        }
        .task { model.start() }
        .onDisappear { model.stop() }
        .onChange(of: session.revision) { _, _ in model.setSession(session.isLoggedIn) }
        .onChange(of: dependencies.resourceUpdates.revision) { _, _ in
            model.reconcile(dependencies.resourceUpdates)
        }
        .alert(.commonCouldNotCompleteAction,
               isPresented: Binding(get: { model.actionFailed }, set: { if !$0 { model.dismissActionFailure() } })) {
            Button(.commonOk, role: .cancel) {}
        }
    }

    /// Android's tag header: a full-width 160 pt banner, then the tag name with its actions.
    /// Like Android, the tag description is not shown.
    private var header: some View {
        VStack(alignment: .leading, spacing: 12) {
            if let banner = model.details?.bannerURL {
                RemoteImage(url: banner, maxDimension: 1200) { WykopTheme.cardInset }
                    .frame(height: 160)
                    .frame(maxWidth: .infinity)
                    .padding(.horizontal, -12)
                    .accessibilityHidden(true)
            }
            HStack(alignment: .center, spacing: 8) {
                Text(verbatim: "#\(model.tag)").font(.title2.bold())
                    .frame(maxWidth: .infinity, alignment: .leading)
                if model.isLoggedIn, let details = model.details { actions(details) }
            }
            .padding(.top, model.details?.bannerURL == nil ? 8 : 0)
            if model.details == nil, model.detailsFailed {
                HStack {
                    Label(.tagCouldNotLoadTag, systemImage: "exclamationmark.triangle")
                        .font(.subheadline)
                    Spacer()
                    Button(.commonRetry) { Task { await model.refresh() } }
                }
            }
        }
    }

    @ViewBuilder private func actions(_ details: TagDetails) -> some View {
        Button { model.toggle(.blacklist) } label: {
            Image(systemName: details.blacklisted ? "lock.fill" : "lock.open")
        }
        .buttonStyle(.bordered)
        .disabled(model.pending.contains(.blacklist))
        .accessibilityLabel(details.blacklisted ? .tagUnblockTag : .tagBlockTag)
        .accessibilityIdentifier("tagBlacklist")
        if details.observed {
            Button { model.toggle(.notifications) } label: {
                Image(systemName: details.notificationsEnabled ? "bell.fill" : "bell.slash")
            }
            .buttonStyle(.bordered)
            .disabled(model.pending.contains(.notifications))
            .accessibilityLabel(details.notificationsEnabled
                                ? .tagDisableTagNotifications : .tagEnableTagNotifications)
            .accessibilityIdentifier("tagNotifications")
        }
        Button { model.toggle(.observe) } label: {
            if model.pending.contains(.observe) { ProgressView() }
            else { Text(details.observed ? .commonObserving : .commonObserve) }
        }
        .buttonStyle(.borderedProminent)
        .tint(details.observed ? .secondary : ContentTokens.brand)
        .disabled(model.pending.contains(.observe))
        .accessibilityIdentifier("tagObserve")
    }

    private var controls: some View {
        AdaptiveControlRow {
            Menu {
                Button(.commonAll) { model.select(sort: .all) }
                Button(.commonBest) { model.select(sort: .best) }
            } label: {
                DropdownLabel(title: model.sort == .all ? String(localized: .commonAll) : String(localized: .commonBest))
            }
            .accessibilityIdentifier("tagSort")
            Menu {
                ForEach(TagModel.Kind.allCases, id: \.self) { kind in
                    Button(title(for: kind)) { model.select(kind: kind) }
                }
            } label: {
                DropdownLabel(title: title(for: model.kind))
            }
            .accessibilityIdentifier("tagType")
        }
    }

    @ViewBuilder private var gallery: some View {
        let items = model.galleryItems
        if model.pager.phase == .loaded && items.isEmpty {
            ContentUnavailableView(.tagNoImagesInEntries, systemImage: "photo")
        } else if model.pager.phase != .loaded {
            PagedResourceRows(pager: model.pager, tab: tab, dependencies: dependencies)
        } else {
            TagGallery(items: items,
                       open: { dependencies.router.navigate(.entry($0.sourceID), in: tab) },
                       // The gallery is a subset of the stream, so page on its own last cell.
                       loadMore: { model.pager.loadNext() })
            PagerFooter(pager: model.pager)
        }
    }

    private func title(for kind: TagModel.Kind) -> String {
        switch kind {
        case .all: String(localized: .commonEverything)
        case .link: String(localized: .commonLinks)
        case .entry: String(localized: .commonEntries)
        }
    }
}

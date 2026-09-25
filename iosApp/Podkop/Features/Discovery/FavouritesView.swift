import SwiftUI

struct FavouritesView: View {
    @State private var model: FavouritesModel
    let tab: AppTab
    let dependencies: AppDependencies

    init(tab: AppTab, dependencies: AppDependencies) {
        _model = State(initialValue: FavouritesModel(loader: dependencies.collectionLoader,
                                                     isLoggedIn: dependencies.session.isLoggedIn,
                                                     updates: dependencies.resourceUpdates))
        self.tab = tab
        self.dependencies = dependencies
    }

    var body: some View {
        CollectionScroll(refresh: model.refresh) {
            HStack {
                Menu {
                    ForEach(FavouritesModel.Kind.allCases, id: \.self) { kind in
                        Button(title(for: kind)) { model.select(kind: kind) }
                    }
                } label: {
                    DropdownLabel(title: title(for: model.kind))
                }
                .accessibilityIdentifier("favouritesType")
                Menu {
                    Button(.commonNewest) { model.select(sort: .newest) }
                    Button(.commonOldest) { model.select(sort: .oldest) }
                } label: {
                    DropdownLabel(title: model.sort == .newest ? String(localized: .commonNewest) : String(localized: .commonOldest))
                }
                .accessibilityIdentifier("favouritesSort")
            }
        } rows: {
            PagedResourceRows(pager: model.pager, tab: tab, dependencies: dependencies,
                              emptyTitle: emptyTitle)
        }
        .navigationTitle(.commonFavorites)
        .task { model.start() }
        .onDisappear { model.stop() }
        .onChange(of: dependencies.session.revision) { _, _ in
            model.setSession(dependencies.session.isLoggedIn)
        }
        .onChange(of: dependencies.resourceUpdates.revision) { _, _ in
            model.reconcile(dependencies.resourceUpdates)
        }
    }

    private var emptyTitle: LocalizedStringResource {
        switch model.kind {
        case .all: .discoveryNoFavoritesYet
        case .link: .discoveryNoFavoriteLinks
        case .entry: .discoveryNoFavoriteEntries
        case .linkComment: .discoveryNoFavoriteLinkComments
        case .entryComment: .discoveryNoFavoriteEntryComments
        }
    }

    private func title(for kind: FavouritesModel.Kind) -> String {
        switch kind {
        case .all: String(localized: .commonEverything)
        case .link: String(localized: .commonLinks)
        case .entry: String(localized: .commonEntries)
        case .linkComment: String(localized: .discoveryLinkComments)
        case .entryComment: String(localized: .discoveryEntryComments)
        }
    }
}

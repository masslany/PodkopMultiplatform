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
                    Button("Newest") { model.select(sort: .newest) }
                    Button("Oldest") { model.select(sort: .oldest) }
                } label: {
                    DropdownLabel(title: model.sort == .newest ? String(localized: "Newest") : String(localized: "Oldest"))
                }
                .accessibilityIdentifier("favouritesSort")
            }
        } rows: {
            PagedResourceRows(pager: model.pager, tab: tab, dependencies: dependencies,
                              emptyTitle: emptyTitle)
        }
        .navigationTitle("Favorites")
        .task { model.start() }
        .onDisappear { model.stop() }
        .onChange(of: dependencies.session.revision) { _, _ in
            model.setSession(dependencies.session.isLoggedIn)
        }
        .onChange(of: dependencies.resourceUpdates.revision) { _, _ in
            model.reconcile(dependencies.resourceUpdates)
        }
    }

    private var emptyTitle: LocalizedStringKey {
        switch model.kind {
        case .all: "No favorites yet"
        case .link: "No favorite links"
        case .entry: "No favorite entries"
        case .linkComment: "No favorite link comments"
        case .entryComment: "No favorite entry comments"
        }
    }

    private func title(for kind: FavouritesModel.Kind) -> String {
        switch kind {
        case .all: String(localized: "Everything")
        case .link: String(localized: "Links")
        case .entry: String(localized: "Entries")
        case .linkComment: String(localized: "Link comments")
        case .entryComment: String(localized: "Entry comments")
        }
    }
}

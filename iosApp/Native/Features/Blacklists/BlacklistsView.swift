import SwiftUI

struct NativeBlacklistsView: View {
    @State private var model: BlacklistsModel
    @State private var removal: BlacklistEntry?
    let tab: AppTab
    let dependencies: AppDependencies

    init(tab: AppTab, dependencies: AppDependencies) {
        _model = State(initialValue: BlacklistsModel(loader: dependencies.blacklistsLoader,
                                                     suggester: dependencies.searchSuggesting))
        self.tab = tab
        self.dependencies = dependencies
    }

    var body: some View {
        let current = model.current
        List {
            Section {
                Text("If you don't want to see some content, you can blacklist users, tags and domains.")
                    .font(.subheadline).foregroundStyle(.secondary)
                Picker("Category", selection: $model.selected) {
                    ForEach(BlacklistCategory.allCases, id: \.self) { category in
                        Text(title(for: category)).tag(category)
                    }
                }
                .pickerStyle(.segmented)
                .accessibilityIdentifier("blacklistCategory")
            }
            Section {
                addForm(current)
                suggestions(current)
            }
            Section {
                entries(current)
            }
        }
        .refreshable { await model.refresh() }
        .navigationTitle("Blacklists")
        .task { model.start() }
        .onDisappear { model.stop() }
        .alert(removal.map { String(localized: "Remove \($0.label) from the blacklist?") } ?? "",
               isPresented: Binding(get: { removal != nil }, set: { if !$0 { removal = nil } }),
               presenting: removal) { entry in
            Button("Remove", role: .destructive) { model.categories[entry.category]?.remove(entry) }
            Button("Cancel", role: .cancel) {}
        }
        .alert("Could not complete this action. Try again.",
               isPresented: Binding(get: { current.failed }, set: { if !$0 { current.dismissFailure() } })) {
            Button("OK", role: .cancel) {}
        }
    }

    private func addForm(_ category: BlacklistCategoryModel) -> some View {
        HStack {
            TextField(placeholder(for: category.category), text: Binding(
                get: { category.input }, set: { category.input = $0 }
            ))
            .textInputAutocapitalization(.never)
            .autocorrectionDisabled()
            .keyboardType(category.category == .domains ? .URL : .default)
            .submitLabel(.done)
            .onSubmit { category.submit() }
            .accessibilityIdentifier("blacklistInput")
            if category.busy {
                ProgressView()
            } else {
                Button("Add") { category.submit() }
                    .disabled(!category.canSubmit)
                    .accessibilityIdentifier("blacklistAdd")
            }
        }
    }

    @ViewBuilder private func suggestions(_ category: BlacklistCategoryModel) -> some View {
        switch category.suggestionStatus {
        case .hidden: EmptyView()
        case .loading: ProgressView().frame(maxWidth: .infinity)
        case .failed:
            HStack {
                Text("Could not load suggestions.").foregroundStyle(.secondary)
                Spacer()
                Button("Retry") { category.retrySuggestions() }
            }
        case .loaded where category.suggestions.isEmpty:
            Text("No results").foregroundStyle(.secondary)
        case .loaded:
            ForEach(category.suggestions) { suggestion in
                Button { category.submit(suggestion.value) } label: {
                    switch suggestion {
                    case .user(let user):
                        UserIdentityRow(username: user.username, color: user.color, gender: user.gender)
                    case .tag(let tag):
                        VStack(alignment: .leading) {
                            Text("#\(tag.name)").font(.body.bold())
                            Text("Followers: \(tag.followers)").font(.caption).foregroundStyle(.secondary)
                        }
                    }
                }
                .foregroundStyle(.primary)
                .disabled(category.busy)
                .accessibilityHint("Adds to the blacklist")
            }
        }
    }

    @ViewBuilder private func entries(_ category: BlacklistCategoryModel) -> some View {
        let pager = category.pager
        switch pager.phase {
        case .idle, .loading:
            ProgressView("Loading…").frame(maxWidth: .infinity)
        case .failed:
            HStack {
                Text("Could not load content").foregroundStyle(.secondary)
                Spacer()
                Button("Retry") { pager.retry() }
            }
        case .loaded where pager.items.isEmpty:
            Text(emptyMessage(for: category.category)).foregroundStyle(.secondary)
        case .loaded:
            ForEach(pager.items) { entry in
                HStack {
                    Button { open(entry) } label: {
                        if entry.category == .users {
                            UserIdentityRow(username: entry.value, color: entry.color, gender: entry.gender)
                        } else {
                            Text(entry.label)
                        }
                    }
                    .foregroundStyle(.primary)
                    .disabled(entry.category == .domains)
                    Spacer()
                    Button { removal = entry } label: { Image(systemName: "trash") }
                        .buttonStyle(.borderless)
                        .disabled(category.busy)
                        .accessibilityLabel(String(localized: "Remove \(entry.label)"))
                }
                .onAppear { pager.loadNextIfNeeded(after: entry) }
            }
            PagerFooter(pager: pager)
        }
    }

    private func open(_ entry: BlacklistEntry) {
        switch entry.category {
        case .users: dependencies.router.navigate(.user(entry.value), in: tab)
        case .tags: dependencies.router.navigate(.tag(entry.value), in: tab)
        case .domains: break
        }
    }

    private func title(for category: BlacklistCategory) -> String {
        let count = model.categories[category]?.total
        let name = switch category {
        case .users: String(localized: "Profiles")
        case .tags: String(localized: "Tags")
        case .domains: String(localized: "Domains")
        }
        return count.map { "\(name) (\($0))" } ?? name
    }

    private func emptyMessage(for category: BlacklistCategory) -> String {
        switch category {
        case .users: String(localized: "You have no blacklisted users yet.")
        case .tags: String(localized: "You have no blacklisted tags yet.")
        case .domains: String(localized: "You have no blacklisted domains yet.")
        }
    }

    private func placeholder(for category: BlacklistCategory) -> String {
        switch category {
        case .users: String(localized: "Username")
        case .tags: String(localized: "Tag")
        case .domains: String(localized: "Domain, e.g. example.com")
        }
    }
}

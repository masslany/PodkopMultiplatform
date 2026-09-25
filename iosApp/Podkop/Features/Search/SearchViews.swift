import SwiftUI

struct SearchView: View {
    @State private var model: SearchModel
    let tab: AppTab
    let dependencies: AppDependencies
    private var router: AppRouter { dependencies.router }
    private var session: SessionModel { dependencies.session }

    init(tab: AppTab, dependencies: AppDependencies) {
        _model = State(initialValue: SearchModel(service: dependencies.searchSuggesting,
                                                       isLoggedIn: dependencies.session.isLoggedIn))
        self.tab = tab
        self.dependencies = dependencies
    }

    var body: some View {
        List {
            Section {
                Button {
                    router.navigate(.advancedSearch(model.normalizedQuery), in: tab)
                } label: {
                    Label("Advanced search", systemImage: "slider.horizontal.3")
                }
                .accessibilityIdentifier("advancedSearch")
            }
            if model.normalizedQuery.isEmpty {
                Section { Text("Search for tags and users.").foregroundStyle(.secondary) }
            } else if model.normalizedQuery.count < model.minimumQueryLength {
                Section {
                    Text("Type at least \(model.minimumQueryLength) characters.").foregroundStyle(.secondary)
                }
            } else {
                tagSection
                if model.isLoggedIn { userSection }
            }
        }
        .searchable(text: $model.query, placement: .navigationBarDrawer(displayMode: .always),
                    prompt: Text("Search tags and users"))
        .autocorrectionDisabled()
        .textInputAutocapitalization(.never)
        .navigationTitle("Search")
        .onChange(of: session.isLoggedIn) { _, value in model.setSession(value) }
        .onAppear { model.resume() }
        .onDisappear { model.stop() }
    }

    private var tagSection: some View {
        Section("Tags") {
            switch model.tagsStatus {
            case .idle, .loading: ProgressView().frame(maxWidth: .infinity)
            case .failed: retryRow("Could not load tags.") { model.retryTags() }
            case .loaded where model.tags.isEmpty: Text("No results").foregroundStyle(.secondary)
            case .loaded:
                ForEach(model.tags) { tag in
                    Button { router.navigate(.tag(tag.name), in: tab) } label: {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("#\(tag.name)").font(.body.bold())
                            Text("Followers: \(tag.followers)").font(.caption).foregroundStyle(.secondary)
                        }
                    }
                    .foregroundStyle(.primary)
                }
            }
        }
    }

    private var userSection: some View {
        Section("Users") {
            switch model.usersStatus {
            case .idle, .loading: ProgressView().frame(maxWidth: .infinity)
            case .failed: retryRow("Could not load users.") { model.retryUsers() }
            case .loaded where model.users.isEmpty: Text("No results").foregroundStyle(.secondary)
            case .loaded:
                ForEach(model.users) { user in
                    Button { router.navigate(.user(user.username), in: tab) } label: {
                        UserIdentityRow(username: user.username, color: user.color, gender: user.gender,
                                        avatarURL: user.avatarURL)
                    }
                }
            }
        }
    }

    private func retryRow(_ message: LocalizedStringKey, retry: @escaping () -> Void) -> some View {
        HStack {
            Text(message).foregroundStyle(.secondary)
            Spacer()
            Button("Retry", action: retry)
        }
    }
}

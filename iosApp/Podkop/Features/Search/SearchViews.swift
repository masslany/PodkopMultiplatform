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
                    Label(.searchAdvancedSearch, systemImage: "slider.horizontal.3")
                }
                .accessibilityIdentifier("advancedSearch")
            }
            if model.normalizedQuery.isEmpty {
                Section { Text(.searchSearchTagsUsers).foregroundStyle(.secondary) }
            } else if model.normalizedQuery.count < model.minimumQueryLength {
                Section {
                    Text(.searchTypeLeastCharacters(model.minimumQueryLength)).foregroundStyle(.secondary)
                }
            } else {
                tagSection
                if model.isLoggedIn { userSection }
            }
        }
        .searchable(text: $model.query, placement: .navigationBarDrawer(displayMode: .always),
                    prompt: Text(.searchSearchTagsUsers2))
        .autocorrectionDisabled()
        .textInputAutocapitalization(.never)
        .navigationTitle(.commonSearch)
        .onChange(of: session.isLoggedIn) { _, value in model.setSession(value) }
        .onAppear { model.resume() }
        .onDisappear { model.stop() }
    }

    private var tagSection: some View {
        Section(.commonTags) {
            switch model.tagsStatus {
            case .idle, .loading: ProgressView().frame(maxWidth: .infinity)
            case .failed: retryRow(.searchCouldNotLoadTags) { model.retryTags() }
            case .loaded where model.tags.isEmpty: Text(.commonNoResults).foregroundStyle(.secondary)
            case .loaded:
                ForEach(model.tags) { tag in
                    Button { router.navigate(.tag(tag.name), in: tab) } label: {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(verbatim: "#\(tag.name)").font(.body.bold())
                            Text(.commonFollowers(tag.followers)).font(.caption).foregroundStyle(.secondary)
                        }
                    }
                    .foregroundStyle(.primary)
                }
            }
        }
    }

    private var userSection: some View {
        Section(.commonUsers) {
            switch model.usersStatus {
            case .idle, .loading: ProgressView().frame(maxWidth: .infinity)
            case .failed: retryRow(.searchCouldNotLoadUsers) { model.retryUsers() }
            case .loaded where model.users.isEmpty: Text(.commonNoResults).foregroundStyle(.secondary)
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

    private func retryRow(_ message: LocalizedStringResource, retry: @escaping () -> Void) -> some View {
        HStack {
            Text(message).foregroundStyle(.secondary)
            Spacer()
            Button(.commonRetry, action: retry)
        }
    }
}

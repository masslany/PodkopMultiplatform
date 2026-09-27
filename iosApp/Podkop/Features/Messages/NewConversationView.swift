import SwiftUI

struct NewConversationView: View {
    @State private var model: NewConversationModel
    let tab: AppTab
    let dependencies: AppDependencies

    init(tab: AppTab, dependencies: AppDependencies) {
        _model = State(initialValue: NewConversationModel(suggester: dependencies.searchSuggesting,
                                                          loader: dependencies.messagesLoader))
        self.tab = tab
        self.dependencies = dependencies
    }

    var body: some View {
        PodkopList {
            Section {
                TextField(String(localized: .commonUsername), text: $model.username)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                    .accessibilityIdentifier("newConversationUsername")
            } footer: {
                Text(.messagesTypeUsernameChooseOne)
            }
            Section {
                if model.query.count < NewConversationModel.minimumQueryLength && model.status == .hidden {
                    Text(.messagesSuggestionsAppearAfterLeast(NewConversationModel.minimumQueryLength))
                        .foregroundStyle(.secondary)
                }
                switch model.status {
                case .hidden: EmptyView()
                case .loading: ProgressView().frame(maxWidth: .infinity)
                case .failed:
                    HStack {
                        Text(.messagesCouldNotLoadUser).foregroundStyle(.secondary)
                        Spacer()
                        Button(.commonRetry) { model.retry() }
                    }
                case .loaded where model.suggestions.isEmpty:
                    Text(.commonNoResults).foregroundStyle(.secondary)
                case .loaded:
                    ForEach(model.suggestions) { user in
                        Button { open(user.username) } label: {
                            UserIdentityRow(username: user.username, color: user.color, gender: user.gender,
                                        avatarURL: user.avatarURL)
                        }
                    }
                }
            }
        }
        .navigationTitle(.messagesNewConversation)
        .onDisappear { model.stop() }
    }

    /// Replaces this screen with the conversation, as Android does.
    private func open(_ username: String) {
        let router = dependencies.router
        var path = router.paths[tab, default: []]
        if path.last == .newConversation { path.removeLast() }
        path.append(.conversation(username))
        router.replacePath(path, for: tab)
    }
}

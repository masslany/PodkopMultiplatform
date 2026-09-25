import SwiftUI

struct InboxView: View {
    @State private var model: InboxModel
    let tab: AppTab
    let dependencies: AppDependencies

    init(tab: AppTab, dependencies: AppDependencies) {
        _model = State(initialValue: InboxModel(loader: dependencies.messagesLoader))
        self.tab = tab
        self.dependencies = dependencies
    }

    var body: some View {
        List {
            let pager = model.pager
            switch pager.phase {
            case .idle, .loading:
                ProgressView(.commonLoading).frame(maxWidth: .infinity)
            case .failed:
                ContentUnavailableView {
                    Label(.commonCouldNotLoadContent, systemImage: "wifi.exclamationmark")
                } actions: {
                    Button(.commonRetry) { pager.retry() }
                }
            case .loaded where pager.items.isEmpty:
                ContentUnavailableView(.messagesHaveNoConversationsYet, systemImage: "envelope")
            case .loaded:
                if pager.refreshError {
                    Label(.commonCouldNotRefresh, systemImage: "exclamationmark.triangle")
                }
                ForEach(pager.items) { conversation in
                    Button {
                        dependencies.router.navigate(.conversation(conversation.username), in: tab)
                    } label: {
                        HStack(alignment: .top) {
                            UserIdentityRow(username: conversation.username, color: conversation.color,
                                            gender: conversation.gender,
                                            detail: conversation.lastMessage.map { String($0.prefix(140)) },
                                            avatarURL: conversation.avatarURL)
                            Spacer()
                            VStack(alignment: .trailing, spacing: 4) {
                                if let date = conversation.lastMessageAt {
                                    Text(date.formatted(.relative(presentation: .named))).font(.caption).foregroundStyle(.secondary)
                                }
                                if conversation.unread {
                                    Circle().fill(.red).frame(width: 9, height: 9).accessibilityLabel(.commonUnread)
                                }
                            }
                        }
                    }
                    .foregroundStyle(.primary)
                    .onAppear { pager.loadNextIfNeeded(after: conversation) }
                }
                PagerFooter(pager: pager)
            }
        }
        .refreshable { await model.refresh() }
        .navigationTitle(.commonMessages)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button { dependencies.router.navigate(.newConversation, in: tab) } label: {
                    Image(systemName: "square.and.pencil")
                }
                .accessibilityLabel(.messagesNewConversation)
                .accessibilityIdentifier("newConversation")
            }
        }
        .task { model.start() }
        .onDisappear { model.stop() }
    }
}

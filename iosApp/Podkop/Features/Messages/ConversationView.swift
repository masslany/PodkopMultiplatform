import SwiftUI

struct ConversationView: View {
    @State private var model: ConversationModel
    @State private var confirmLeave = false
    @State private var selection = NSRange(location: 0, length: 0)
    @Environment(\.dismiss) private var dismiss
    let tab: AppTab
    let dependencies: AppDependencies

    init(username: String, tab: AppTab, dependencies: AppDependencies) {
        _model = State(initialValue: ConversationModel(username: username, loader: dependencies.messagesLoader,
                                                       media: dependencies.composerMedia))
        self.tab = tab
        self.dependencies = dependencies
    }

    private var active: Bool {
        dependencies.isForeground && dependencies.session.isLoggedIn
    }

    var body: some View {
        VStack(spacing: 0) {
            switch model.phase {
            case .loading where model.messages.isEmpty:
                ProgressView(.commonLoading).frame(maxWidth: .infinity, maxHeight: .infinity)
            case .failed:
                ContentUnavailableView {
                    Label(.messagesCouldNotLoadConversation, systemImage: "exclamationmark.bubble")
                } actions: {
                    Button(.commonRetry) { model.retry() }
                }
                .frame(maxHeight: .infinity)
            default:
                messages
            }
            Divider()
            composer
        }
        .toolbar(.hidden, for: .tabBar)
        .navigationTitle(model.username)
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(model.isDirty || model.sending)
        .toolbar {
            if model.isDirty || model.sending {
                ToolbarItem(placement: .topBarLeading) {
                    Button { confirmLeave = true } label: { Label(.commonBack, systemImage: "chevron.backward") }
                        .disabled(model.sending)
                }
            }
            ToolbarItem(placement: .topBarTrailing) {
                Button { dependencies.router.navigate(.user(model.username), in: tab) } label: {
                    Image(systemName: "person.crop.circle")
                }
                .accessibilityLabel(.commonProfile)
            }
        }
        .alert(.commonDiscardChanges, isPresented: $confirmLeave) {
            Button(.commonDiscard, role: .destructive) {
                model.discardDraft()
                dismiss()
            }
            Button(.commonKeepWriting, role: .cancel) {}
        }
        .onAppear { if active { model.becameVisible() } }
        .onDisappear { model.becameHidden() }
        .onChange(of: model.text) { _, text in
            let length = text.utf16.count
            if selection.location > length || selection.length > length - selection.location {
                selection = NSRange(location: length, length: 0)
            }
        }
        .onChange(of: active) { _, value in
            if value { model.becameVisible() } else { model.becameHidden() }
        }
    }

    private var messages: some View {
        ScrollViewReader { proxy in
            ScrollView {
                LazyVStack(spacing: 8) {
                    if model.hasOlder || model.olderLoading || model.olderFailed {
                        Group {
                            if model.olderFailed {
                                Button(.messagesRetryOlderMessages) { model.retryOlder() }
                            } else {
                                ProgressView()
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .onAppear { model.loadOlder() }
                    }
                    if model.messages.isEmpty {
                        Text(.messagesNoMessagesInConversation).foregroundStyle(.secondary).padding()
                    }
                    ForEach(model.messages) { message in
                        MessageBubble(message: message, tab: tab, dependencies: dependencies)
                            .id(message.id)
                    }
                }
                .padding(12)
            }
            .refreshable { await model.refresh() }
            .onChange(of: model.scrollToLatest) { _, _ in
                if let last = model.messages.last { proxy.scrollTo(last.id, anchor: .bottom) }
            }
            .onChange(of: model.anchorAfterPrepend) { _, anchor in
                guard let anchor else { return }
                proxy.scrollTo(anchor, anchor: .top)
                model.anchorRestored()
            }
            .onAppear {
                if let last = model.messages.last { proxy.scrollTo(last.id, anchor: .bottom) }
            }
        }
    }

    private var composer: some View {
        VStack(alignment: .leading, spacing: 8) {
            if model.sendFailed {
                Label(model.outcomeUnknown
                      ? .messagesSendingStatusUnclearCheck
                      : .messagesCouldNotSendMessage,
                      systemImage: "exclamationmark.triangle")
                    .font(.caption).foregroundStyle(.red)
                if model.outcomeUnknown {
                    Button(.commonICheckedAllowRetry) { model.acknowledgeUnknownOutcome() }.font(.caption)
                }
            }
            ComposerAttachmentStatus(attachment: model.attachment, disabled: model.sending)
            MessageFormattingBar(text: $model.text, selection: $selection, disabled: model.sending)
            ZStack(alignment: .topLeading) {
                if model.text.isEmpty {
                    Text(.messagesWriteMessage)
                        .foregroundStyle(.secondary)
                        .padding(.horizontal, 12).padding(.vertical, 12)
                        .allowsHitTesting(false)
                }
                ComposerEditor(text: $model.text, selection: $selection)
                    .disabled(model.sending)
                    .accessibilityIdentifier("messageInput")
                    .accessibilityLabel(.messagesWriteMessage)
            }
            .frame(height: 110)
            .background(WykopTheme.background, in: RoundedRectangle(cornerRadius: 12))
            .overlay(RoundedRectangle(cornerRadius: 12).strokeBorder(Color.primary.opacity(0.4), lineWidth: 1.5))
            AdaptiveControlRow {
                Toggle("18+", isOn: $model.adult)
                    .wykopSwitch()
                    .fixedSize()
                    .accessibilityLabel(.commonAdultContent)
                ComposerAttachmentControls(attachment: model.attachment, disabled: model.sending, compact: true)
            } trailing: {
                Button { model.send() } label: {
                    Group {
                        if model.sending { ProgressView() }
                        else { Text(.commonSend).font(.subheadline.weight(.semibold)) }
                    }
                    .frame(minWidth: 64, minHeight: 32)
                }
                .buttonStyle(.borderedProminent)
                .buttonBorderShape(.capsule)
                .tint(.primary)
                .foregroundStyle(WykopTheme.background)
                .disabled(!model.canSend)
                .accessibilityLabel(.commonSend)
                .accessibilityIdentifier("messageSend")
            }

        }
        .padding(12)
        .background(WykopTheme.card)
    }
}

private struct MessageBubble: View {
    @Environment(\.openURL) private var openURL
    let message: Message
    let tab: AppTab
    let dependencies: AppDependencies
    @State private var adultRevealed = false

    var body: some View {
        HStack {
            if !message.incoming { Spacer(minLength: 40) }
            VStack(alignment: .leading, spacing: 6) {
                if message.adult && !adultRevealed {
                    Button(.commonShowAdultContent) { adultRevealed = true }.buttonStyle(.bordered)
                } else {
                    if let content = message.content, !content.isEmpty {
                        RichContent(
                            source: content, deletion: nil, muted: false,
                            onProfile: { dependencies.router.navigate(.user($0), in: tab) },
                            onTag: { dependencies.router.navigate(.tag($0), in: tab) },
                            onURL: { openURL($0) }
                        )
                    }
                    if let photo = message.photo {
                        MediaView(photo: photo, bytes: nil, autoplay: dependencies.session.autoplayGifs,
                                        foreground: dependencies.isForeground)
                    }
                    if let raw = message.embedURL, let url = URL(string: raw) {
                        Button(.commonOpenLink) { openURL(url) }.font(.caption)
                    }
                }
                Text(message.createdAt.formatted(.relative(presentation: .named))).font(.caption2).foregroundStyle(.secondary)
            }
            .padding(10)
            .background(message.incoming ? AnyShapeStyle(WykopTheme.card) : AnyShapeStyle(ContentTokens.brand.opacity(0.15)),
                        in: RoundedRectangle(cornerRadius: 14))
            .accessibilityElement(children: .contain)
            .accessibilityIdentifier("message-\(message.key)")
            if message.incoming { Spacer(minLength: 40) }
        }
    }
}

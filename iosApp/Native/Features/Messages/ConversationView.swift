import SwiftUI

struct NativeConversationView: View {
    @State private var model: ConversationModel
    @State private var confirmLeave = false
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
                ProgressView("Loading…").frame(maxWidth: .infinity, maxHeight: .infinity)
            case .failed:
                ContentUnavailableView {
                    Label("Could not load the conversation.", systemImage: "exclamationmark.bubble")
                } actions: {
                    Button("Retry") { model.retry() }
                }
                .frame(maxHeight: .infinity)
            default:
                messages
            }
            Divider()
            composer
        }
        .navigationTitle(model.username)
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(model.isDirty || model.sending)
        .toolbar {
            if model.isDirty || model.sending {
                ToolbarItem(placement: .topBarLeading) {
                    Button { confirmLeave = true } label: { Label("Back", systemImage: "chevron.backward") }
                        .disabled(model.sending)
                }
            }
            ToolbarItem(placement: .topBarTrailing) {
                Button { dependencies.router.navigate(.user(model.username), in: tab) } label: {
                    Image(systemName: "person.crop.circle")
                }
                .accessibilityLabel("Profile")
            }
        }
        .alert("Discard your changes?", isPresented: $confirmLeave) {
            Button("Discard", role: .destructive) {
                model.discardDraft()
                dismiss()
            }
            Button("Keep writing", role: .cancel) {}
        }
        .onAppear { if active { model.becameVisible() } }
        .onDisappear { model.becameHidden() }
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
                                Button("Retry older messages") { model.retryOlder() }
                            } else {
                                ProgressView()
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .onAppear { model.loadOlder() }
                    }
                    if model.messages.isEmpty {
                        Text("No messages in this conversation").foregroundStyle(.secondary).padding()
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
                      ? "Sending status is unclear. Check the conversation before sending again."
                      : "Could not send. Your message is kept here.",
                      systemImage: "exclamationmark.triangle")
                    .font(.caption).foregroundStyle(.red)
                if model.outcomeUnknown {
                    Button("I checked; allow retry") { model.acknowledgeUnknownOutcome() }.font(.caption)
                }
            }
            ComposerAttachmentStatus(attachment: model.attachment, disabled: model.sending)
            HStack(alignment: .bottom, spacing: 8) {
                TextField("Write a message", text: $model.text, axis: .vertical)
                    .lineLimit(1...6)
                    .textFieldStyle(.roundedBorder)
                    .disabled(model.sending)
                    .accessibilityIdentifier("messageInput")
                Button { model.send() } label: {
                    if model.sending { ProgressView() } else { Image(systemName: "paperplane.fill") }
                }
                .buttonStyle(.borderedProminent)
                .disabled(!model.canSend)
                .accessibilityLabel("Send")
                .accessibilityIdentifier("messageSend")
            }
            HStack {
                ComposerAttachmentControls(attachment: model.attachment, disabled: model.sending)
                Spacer()
                Toggle("Adult content", isOn: $model.adult).fixedSize()
            }
            .font(.caption)
        }
        .padding(12)
        .background(.bar)
    }
}

private struct MessageBubble: View {
    @Environment(\.openURL) private var openURL
    let message: NativeMessage
    let tab: AppTab
    let dependencies: AppDependencies
    @State private var adultRevealed = false

    var body: some View {
        HStack {
            if !message.incoming { Spacer(minLength: 40) }
            VStack(alignment: .leading, spacing: 6) {
                if message.adult && !adultRevealed {
                    Button("Show adult content") { adultRevealed = true }.buttonStyle(.bordered)
                } else {
                    if let content = message.content, !content.isEmpty {
                        NativeRichContent(
                            source: content, deletion: nil, muted: false,
                            onProfile: { dependencies.router.navigate(.user($0), in: tab) },
                            onTag: { dependencies.router.navigate(.tag($0), in: tab) },
                            onURL: { openURL($0) }
                        )
                    }
                    if let photo = message.photo {
                        NativeMediaView(photo: photo, bytes: nil, autoplay: dependencies.session.autoplayGifs,
                                        foreground: dependencies.isForeground)
                    }
                    if let raw = message.embedURL, let url = URL(string: raw) {
                        Button("Open link") { openURL(url) }.font(.caption)
                    }
                }
                Text(message.createdAt.formatted(.relative(presentation: .named))).font(.caption2).foregroundStyle(.secondary)
            }
            .padding(10)
            .background(message.incoming ? AnyShapeStyle(.regularMaterial) : AnyShapeStyle(ContentTokens.brand.opacity(0.15)),
                        in: RoundedRectangle(cornerRadius: 14))
            .accessibilityElement(children: .contain)
            .accessibilityIdentifier("message-\(message.key)")
            if message.incoming { Spacer(minLength: 40) }
        }
    }
}

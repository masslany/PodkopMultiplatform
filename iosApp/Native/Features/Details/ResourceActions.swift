import SwiftUI
import Observation
import PodkopShared

enum ResourceLinkBuilder {
    static func url(for resource: NativeResource, root: NativeResource,
                    parentCommentID: Int? = nil) -> URL? {
        let base: String
        switch resource.kind {
        case .entry:
            base = "https://wykop.pl/wpis/\(resource.sourceID)"
        case .entryComment:
            base = "https://wykop.pl/wpis/\(root.sourceID)/#\(resource.sourceID)"
        case .link:
            guard !root.slug.isEmpty else { return nil }
            base = "https://wykop.pl/link/\(root.sourceID)/\(root.slug)"
        case .linkComment:
            guard !root.slug.isEmpty else { return nil }
            let prefix = "https://wykop.pl/link/\(root.sourceID)/\(root.slug)/komentarz/"
            if let parentCommentID, parentCommentID != resource.sourceID {
                base = "\(prefix)\(parentCommentID)#\(resource.sourceID)"
            } else {
                base = "\(prefix)\(resource.sourceID)"
            }
        case .unknown:
            return nil
        }
        return URL(string: base)
    }
}

struct VoterTarget: Identifiable {
    let kind: String
    let rootID: Int
    let commentID: Int?
    let side: String
    var id: String { "\(kind):\(rootID):\(commentID ?? 0):\(side)" }
}

struct NativeResourceActionsSheet: View {
    let resource: NativeResource
    let root: NativeResource
    let parent: NativeResource?
    let dependencies: AppDependencies
    let delete: () -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var voterTarget: VoterTarget?
    @State private var screenshot = false
    @State private var textSelection = false
    @State private var confirmDelete = false

    var body: some View {
        NavigationStack {
            List {
                if resource.kind == .link {
                    voterButton("Show upvoters", side: "up")
                    voterButton("Show downvoters", side: "down")
                } else if resource.kind == .entry || resource.kind == .entryComment {
                    voterButton("Show voters", side: "up")
                }
                if resource.kind != .link {
                    Button("Share as screenshot") { screenshot = true }
                }
                if let url = ResourceLinkBuilder.url(for: resource, root: root,
                                                     parentCommentID: parent?.sourceID) {
                    Button("Copy link") {
                        UIPasteboard.general.url = url
                        dismiss()
                    }
                }
                if resource.kind != .link && !resource.body.isEmpty {
                    Button("Copy text") {
                        UIPasteboard.general.string = resource.body
                        dismiss()
                    }
                    Button("Select text") { textSelection = true }
                }
                #if DEBUG
                if resource.editable {
                    Button("Edit") {
                        let intent: ComposerIntent
                        switch resource.kind {
                        case .entry: intent = .editEntry(resource.sourceID)
                        case .entryComment:
                            intent = .editEntryComment(entryID: root.sourceID,
                                                       commentID: resource.sourceID)
                        case .linkComment:
                            intent = .editLinkComment(linkID: root.sourceID,
                                                      commentID: resource.sourceID)
                        default: return
                        }
                        dismiss()
                        dependencies.router.presentComposer(intent)
                    }
                }
                #endif
                if resource.deletable && (resource.kind == .entry || resource.kind == .entryComment) {
                    Button("Delete", role: .destructive) { confirmDelete = true }
                }
            }
            .navigationTitle("Actions")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") { dismiss() }
                }
            }
        }
        .presentationDetents([.medium, .large])
        .sheet(item: $voterTarget) { NativeVotersSheet(target: $0, dependencies: dependencies) }
        .sheet(isPresented: $screenshot) {
            NativeScreenshotPreview(resource: resource, parent: parent)
        }
        .sheet(isPresented: $textSelection) {
            NavigationStack {
                ScrollView {
                    Text(resource.body).textSelection(.enabled).frame(maxWidth: .infinity, alignment: .leading)
                        .padding()
                }
                .navigationTitle("Select text")
                .toolbar {
                    ToolbarItem(placement: .topBarTrailing) {
                        Button("Done") { textSelection = false }
                    }
                }
            }
        }
        .confirmationDialog("Delete this content?", isPresented: $confirmDelete) {
            Button("Delete", role: .destructive) { delete(); dismiss() }
            Button("Cancel", role: .cancel) {}
        }
    }

    private func voterButton(_ title: LocalizedStringKey, side: String) -> some View {
        Button(title) {
            voterTarget = VoterTarget(kind: resource.kind.rawValue,
                                      rootID: resource.kind == .link || resource.kind == .entry
                                          ? resource.sourceID : root.sourceID,
                                      commentID: resource.kind == .entryComment ? resource.sourceID : nil,
                                      side: side)
        }
    }
}

struct NativeScreenshotPreview: View {
    let resource: NativeResource
    let parent: NativeResource?
    @Environment(\.dismiss) private var dismiss
    @State private var includeParent = true
    @State private var image: UIImage?

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    if parent != nil {
                        Toggle("Include parent", isOn: $includeParent)
                    }
                    if let image {
                        Image(uiImage: image).resizable().scaledToFit()
                            .accessibilityLabel("Screenshot preview")
                        ShareLink(item: Image(uiImage: image), preview: SharePreview("Screenshot")) {
                            Label("Share screenshot", systemImage: "square.and.arrow.up")
                        }
                        .buttonStyle(.borderedProminent)
                    } else {
                        ContentUnavailableView {
                            Label("Could not create screenshot", systemImage: "photo")
                        } actions: {
                            Button("Retry") { render() }
                        }
                    }
                }
                .padding()
            }
            .navigationTitle("Screenshot preview")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) { Button("Done") { dismiss() } }
            }
        }
        .onAppear { render() }
        .onChange(of: includeParent) { _, _ in render() }
    }

    private func render() {
        image = NativeScreenshotSurface.render(resource: resource,
                                               parent: includeParent ? parent : nil)
    }
}

struct NativeVoter: Identifiable {
    let username: String
    let avatarURL: String
    let verified: Bool
    let reason: String?
    var id: String { username }
}

struct VoterPage {
    let items: [NativeVoter]
    let total: Int?
}

@MainActor protocol VoterLoading {
    func load(target: VoterTarget, page: Int) async throws -> VoterPage
}

@MainActor
final class SharedVoterLoader: VoterLoading {
    private let client: PodkopClient
    private let adapter: BridgeAdapter

    init(client: PodkopClient, adapter: BridgeAdapter) {
        self.client = client
        self.adapter = adapter
    }

    func load(target: VoterTarget, page: Int) async throws -> VoterPage {
        let value: IOSVoterPage = try await adapter.call {
            self.client.voters.load(kind: target.kind, rootId: Int32(target.rootID),
                                    commentId: target.commentID.map { KotlinInt(int: Int32($0)) },
                                    side: target.side, page: Int32(page), completion: $0)
        }
        return VoterPage(items: value.items.map {
            NativeVoter(username: $0.username, avatarURL: $0.avatarUrl,
                        verified: $0.verified, reason: $0.reason)
        }, total: value.total?.intValue)
    }
}

#if DEBUG
@MainActor
final class FixtureVoterLoader: VoterLoading {
    func load(target: VoterTarget, page: Int) async throws -> VoterPage {
        VoterPage(items: page == 1
            ? [NativeVoter(username: "Ewa-Żółw", avatarURL: "", verified: true, reason: nil)] : [],
                  total: 1)
    }
}
#endif

@MainActor @Observable
final class VoterModel {
    private(set) var voters: [NativeVoter] = []
    private(set) var loading = false
    private(set) var failed = false
    private(set) var exhausted = false
    private var page = 1
    private var task: Task<Void, Never>?
    let target: VoterTarget
    private let loader: VoterLoading

    init(target: VoterTarget, loader: VoterLoading) {
        self.target = target
        self.loader = loader
    }

    func load() {
        guard !loading, !exhausted else { return }
        loading = true
        failed = false
        let requestedPage = page
        task = Task { [weak self] in
            guard let self else { return }
            do {
                let value = try await loader.load(target: target, page: requestedPage)
                guard !Task.isCancelled else { return }
                let fresh = value.items.filter { item in !voters.contains { $0.username == item.username } }
                voters.append(contentsOf: fresh)
                page += 1
                exhausted = value.items.isEmpty || fresh.isEmpty ||
                    value.total.map { voters.count >= $0 } == true
            } catch is CancellationError {
                return
            } catch {
                failed = true
            }
            loading = false
            task = nil
        }
    }

    func stop() { task?.cancel(); task = nil; loading = false }
}

struct NativeVotersSheet: View {
    let target: VoterTarget
    let dependencies: AppDependencies
    @State private var model: VoterModel
    @Environment(\.dismiss) private var dismiss

    init(target: VoterTarget, dependencies: AppDependencies) {
        self.target = target
        self.dependencies = dependencies
        _model = State(initialValue: VoterModel(target: target, loader: dependencies.voterLoader))
    }

    var body: some View {
        NavigationStack {
            List {
                ForEach(model.voters) { voter in
                    Button { dismiss(); dependencies.router.navigate(.user(voter.username)) } label: {
                        HStack {
                            Circle().fill(.blue.opacity(0.15)).frame(width: 32, height: 32)
                                .overlay(Text(String(voter.username.prefix(1)).uppercased()))
                            Text(voter.username)
                            if voter.verified { Image(systemName: "checkmark.seal.fill") }
                            if let reason = voter.reason { Text(reason).foregroundStyle(.secondary) }
                        }
                    }
                    .onAppear { if voter.username == model.voters.last?.username { model.load() } }
                }
                if model.loading { ProgressView() }
                if model.failed { Button("Retry") { model.load() } }
                if model.exhausted && model.voters.isEmpty { Text("Nothing here yet") }
            }
            .navigationTitle("Voters")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) { Button("Done") { dismiss() } }
            }
        }
        .task { model.load() }
        .onDisappear { model.stop() }
    }
}

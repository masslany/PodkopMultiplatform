import SwiftUI
import Observation
import PodkopShared

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
                        dependencies.router.presentComposer(intent, seed: resource)
                    }
                }
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

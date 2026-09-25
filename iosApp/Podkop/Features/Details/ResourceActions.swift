import SwiftUI
import Observation
import PodkopShared

struct ResourceActionsSheet: View {
    let resource: Resource
    let root: Resource
    let parent: Resource?
    let dependencies: AppDependencies
    let delete: () -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var voterTarget: VoterTarget?
    @State private var screenshot = false
    @State private var textSelection = false
    @State private var confirmDelete = false
    @State private var contentHeight: CGFloat = 320

    /// Android's actions bottom sheet: neutral rows with a leading glyph, no title bar.
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                if resource.kind == .link {
                    voterRow(.detailsShowUpvoters, systemImage: "plus", side: "up")
                    voterRow(.detailsShowDownvoters, systemImage: "minus", side: "down")
                } else if resource.kind == .entry || resource.kind == .entryComment {
                    voterRow(.detailsShowVoters, systemImage: "plus", side: "up")
                }
                if resource.kind != .link {
                    row(.detailsShareScreenshot, systemImage: "square.and.arrow.up") { screenshot = true }
                }
                if let url = ResourceLinkBuilder.url(for: resource, root: root,
                                                     parentCommentID: parent?.sourceID) {
                    row(.detailsCopyLink, systemImage: "link") {
                        UIPasteboard.general.url = url
                        dismiss()
                    }
                }
                if resource.kind != .link && !resource.body.isEmpty {
                    row(.detailsCopyText, systemImage: "doc.on.doc") {
                        UIPasteboard.general.string = resource.body
                        dismiss()
                    }
                    row(.detailsSelectText, systemImage: "character.cursor.ibeam") { textSelection = true }
                }
                if resource.editable {
                    row(.commonEdit, systemImage: "pencil") {
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
                    row(.commonDelete, systemImage: "trash", destructive: true) { confirmDelete = true }
                }
            }
            .padding(.top, 20)
            .padding(.bottom, 8)
            .onGeometryChange(for: CGFloat.self) { $0.size.height } action: { contentHeight = $0 }
        }
        .scrollBounceBehavior(.basedOnSize)
        .presentationDragIndicator(.visible)
        .presentationBackground(WykopTheme.card)
        .accessibilityIdentifier("resourceActions")
        // Sized to its rows, like Android's bottom sheet.
        .presentationDetents([.height(contentHeight + 24)])
        .sheet(item: $voterTarget) { VotersSheet(target: $0, dependencies: dependencies) }
        .sheet(isPresented: $screenshot) {
            ScreenshotPreview(resource: resource, parent: parent)
        }
        .sheet(isPresented: $textSelection) {
            NavigationStack {
                ScrollView {
                    Text(resource.body).textSelection(.enabled).frame(maxWidth: .infinity, alignment: .leading)
                        .padding()
                }
                .navigationTitle(.detailsSelectText)
                .toolbar {
                    ToolbarItem(placement: .topBarTrailing) {
                        Button(.detailsDone) { textSelection = false }
                    }
                }
            }
        }
        .confirmationDialog(.detailsDeleteContent, isPresented: $confirmDelete) {
            Button(.commonDelete, role: .destructive) { delete(); dismiss() }
            Button(.commonCancel, role: .cancel) {}
        }
    }

    private func row(_ title: LocalizedStringResource, systemImage: String, destructive: Bool = false,
                     action: @escaping () -> Void) -> some View {
        Button(action: action) {
            HStack(spacing: 20) {
                Image(systemName: systemImage)
                    .font(.system(size: 20))
                    .frame(width: 28)
                Text(title).font(.body)
                Spacer(minLength: 0)
            }
            .foregroundStyle(destructive ? WykopTheme.voteNegative : .primary)
            .padding(.horizontal, 24)
            .frame(minHeight: 56)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }

    private func voterRow(_ title: LocalizedStringResource, systemImage: String, side: String) -> some View {
        row(title, systemImage: systemImage) {
            voterTarget = VoterTarget(kind: resource.kind.rawValue,
                                      rootID: resource.kind == .link || resource.kind == .entry
                                          ? resource.sourceID : root.sourceID,
                                      commentID: resource.kind == .entryComment ? resource.sourceID : nil,
                                      side: side)
        }
    }
}

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
    @State private var report = false
    @State private var textSelection = false
    @State private var confirmDelete = false
    @State private var confirmBlock = false
    @State private var blocking = false
    @State private var blockedAuthor: String?
    @State private var contentHeight: CGFloat = 320

    /// Android's actions bottom sheet: neutral rows with a leading glyph, no title bar.
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                if resource.kind == .link {
                    voterRow(.detailsShowLinkUpvoters, systemImage: "plus", side: "up")
                    voterRow(.detailsShowLinkDownvoters, systemImage: "minus", side: "down")
                } else if resource.kind == .entry || resource.kind == .entryComment {
                    voterRow(.detailsShowVoters, systemImage: "plus", side: "up")
                }
                if resource.kind != .link {
                    row(.detailsShareScreenshot, systemImage: "square.and.arrow.up") { screenshot = true }
                }
                if contentURL != nil {
                    row(.reportTitle, systemImage: "flag") { report = true }
                }
                if let author = blockableAuthor {
                    row(.detailsBlockAuthor(author), systemImage: "lock") { confirmBlock = true }
                        .disabled(blocking)
                        .accessibilityIdentifier("blockAuthor")
                }
                if let url = contentURL {
                    row(.detailsCopyLink, systemImage: "link") {
                        UIPasteboard.general.url = url
                        dependencies.router.banner = String(localized: .detailsSnackbarLinkCopied)
                        dismiss()
                    }
                }
                if resource.kind != .link && !resource.body.isEmpty {
                    row(.detailsCopyText, systemImage: "doc.on.doc") {
                        UIPasteboard.general.string = resource.body
                        dependencies.router.banner = String(localized: .detailsSnackbarTextCopied)
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
        .presentationBackground(PodkopTheme.card)
        .accessibilityIdentifier("resourceActions")
        // Sized to its rows, like Android's bottom sheet.
        .presentationDetents([.height(contentHeight + 24)])
        .sheet(item: $voterTarget) {
            VotersSheet(target: $0, dependencies: dependencies) { username in
                // Close both sheets, then open the profile in the screen underneath.
                voterTarget = nil
                dismiss()
                dependencies.router.navigate(.user(username))
            }
        }
        // The author's content goes once the sheets close, so a report still opens over the page it came from.
        .onDisappear {
            guard let blockedAuthor else { return }
            dependencies.resourceUpdates.block(blockedAuthor)
            dependencies.router.banner = String(localized: .detailsSnackbarAuthorBlocked(blockedAuthor))
        }
        .sheet(isPresented: $report, onDismiss: { if blockedAuthor != nil { dismiss() } }) {
            if let url = contentURL {
                WebsiteReportView(contentURL: url,
                                  missingCommentID: resource.kind == .linkComment && root.slug.isEmpty
                                      ? resource.sourceID : nil)
            }
        }
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
        .confirmationDialog(resource.kind == .entry ? .detailsDialogTitleDeleteEntry : .detailsDialogTitleDeleteEntryComment,
                            isPresented: $confirmDelete, titleVisibility: .visible) {
            Button(.commonDelete, role: .destructive) { delete(); dismiss() }
            Button(.commonCancel, role: .cancel) {}
        }
        .confirmationDialog(Text(.detailsBlockAuthorTitle(blockableAuthor ?? "")),
                            isPresented: $confirmBlock, titleVisibility: .visible) {
            Button(.detailsBlockAndReport) { block(report: true) }
            Button(.detailsBlock, role: .destructive) { block(report: false) }
            Button(.commonCancel, role: .cancel) {}
        } message: {
            Text(.detailsBlockAuthorMessage)
        }
    }

    private var contentURL: URL? {
        ResourceLinkBuilder.url(for: resource, root: root, parentCommentID: parent?.sourceID)
    }

    /// App Store guideline 1.2 wants blocking on the content itself, not only on profiles.
    private var blockableAuthor: String? {
        guard dependencies.session.isLoggedIn, resource.deletion == nil, !resource.blacklisted,
              let me = dependencies.session.username, let author = resource.author?.name,
              author.caseInsensitiveCompare(me) != .orderedSame else { return nil }
        return author
    }

    private func block(report: Bool) {
        guard let author = blockableAuthor, !blocking else { return }
        blocking = true
        Task {
            do {
                try await dependencies.profileLoader.setBlacklisted(author, true)
                blockedAuthor = author
                if report { self.report = true } else { dismiss() }
            } catch {
                blocking = false
                if !(error is CancellationError) {
                    dependencies.router.banner = String(localized: .commonCouldNotCompleteAction)
                }
            }
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
            .foregroundStyle(destructive ? PodkopTheme.voteNegative : .primary)
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

extension ResourceKind {
    /// Android's snackbar after a delete succeeds.
    var deletedMessage: String {
        String(localized: self == .entry ? .detailsSnackbarEntryDeleted : .detailsSnackbarEntryCommentDeleted)
    }
}

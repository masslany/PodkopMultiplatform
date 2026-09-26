import SwiftUI
import UIKit
import PhotosUI
import UniformTypeIdentifiers

struct ComposerView: View {
    @State private var model: ComposerModel
    let dependencies: AppDependencies
    @State private var showDiscard = false

    init(intent: ComposerIntent, seed: Resource?, dependencies: AppDependencies) {
        _model = State(initialValue: ComposerModel(intent: intent, seed: seed,
                            submitter: dependencies.composerSubmitter,
                            updates: dependencies.resourceUpdates,
                            media: dependencies.composerMedia))
        self.dependencies = dependencies
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    if let target = model.intent.target.replyTarget {
                        Text(.composerReplyTo(target)).font(.subheadline).foregroundStyle(.secondary)
                    }
                    formatBar
                    ComposerEditor(text: $model.text, selection: $model.selection)
                        .frame(minHeight: 230)
                        .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 12))
                        .accessibilityIdentifier("composerEditor")
                    Toggle(.commonAdultContent, isOn: $model.adult).wykopSwitch()
                    ComposerAttachmentControls(attachment: model.attachment, disabled: model.submitting)
                    ComposerAttachmentStatus(attachment: model.attachment, disabled: model.submitting)
                    if model.failed {
                        Label(model.outcomeUnknown
                              ? .composerSubmissionStatusUnclearCheck
                              : .composerCouldNotSendText,
                              systemImage: "exclamationmark.triangle")
                            .foregroundStyle(.red)
                    }
                    if model.outcomeUnknown {
                        Button(.commonICheckedAllowRetry) { model.acknowledgeUnknownOutcome() }
                    }
                    Button { model.submit() } label: {
                        if model.submitting { ProgressView().frame(maxWidth: .infinity) }
                        else { Text(model.intent.target.isEdit ? .composerSaveChanges : .commonSend)
                                .frame(maxWidth: .infinity) }
                    }
                    .buttonStyle(.borderedProminent)
                    .disabled(!model.canSubmit)
                    .accessibilityIdentifier("composerSubmit")
                }
                .padding()
                .frame(maxWidth: 760)
                .frame(maxWidth: .infinity)
            }
            .navigationTitle(model.intent.target.isEdit ? .commonEdit : .commonWritePost)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button(.commonCancel) {
                        if model.isDirty { showDiscard = true }
                        else { model.discard(); dependencies.router.dismissSheet() }
                    }
                    .disabled(model.submitting || model.mediaUploading)
                }
            }
        }
        .interactiveDismissDisabled(model.isDirty || model.submitting || model.mediaUploading)
        .alert(.commonDiscardChanges, isPresented: $showDiscard) {
            Button(.commonDiscard, role: .destructive) {
                model.discard()
                dependencies.router.dismissSheet()
            }
            Button(.commonKeepWriting, role: .cancel) {}
        }
        .onChange(of: model.submittedResource) { _, value in
            if value != nil { dependencies.router.dismissSheet() }
        }
    }

    private var formatBar: some View {
        ScrollView(.horizontal) {
            HStack(spacing: 8) {
                formatButton(.composerBold, symbol: "bold", prefix: "**", suffix: "**", placeholder: "bold")
                formatButton(.composerItalic, symbol: "italic", prefix: "__", suffix: "__", placeholder: "italic")
                formatButton(.composerCode, symbol: "chevron.left.forwardslash.chevron.right",
                             prefix: "`", suffix: "`", placeholder: "code")
                formatButton(.commonLink, symbol: "link", prefix: "[", suffix: "](url)",
                             placeholder: "description")
                formatButton(.composerQuote, symbol: "text.quote", prefix: ">", suffix: "", placeholder: "quote")
                Button { model.insertSpoilerAtLineStart() } label: {
                    Label(.composerSpoiler, systemImage: "eye.slash")
                }
                .buttonStyle(.bordered)
                .disabled(model.submitting)
            }
        }
    }

    private func formatButton(_ title: LocalizedStringResource, symbol: String,
                              prefix: String, suffix: String, placeholder: String) -> some View {
        Button { model.insert(prefix: prefix, suffix: suffix, placeholder: placeholder) } label: {
            Label(title, systemImage: symbol)
        }
        .buttonStyle(.bordered)
        .disabled(model.submitting)
    }
}

struct ComposerEditor: UIViewRepresentable {
    @Binding var text: String
    @Binding var selection: NSRange

    func makeUIView(context: Context) -> UITextView {
        let view = UITextView()
        view.delegate = context.coordinator
        view.font = .preferredFont(forTextStyle: .body)
        view.adjustsFontForContentSizeCategory = true
        view.backgroundColor = .clear
        view.textContainerInset = UIEdgeInsets(top: 12, left: 8, bottom: 12, right: 8)
        view.text = text
        view.selectedRange = selection
        return view
    }

    func updateUIView(_ view: UITextView, context: Context) {
        context.coordinator.parent = self
        if view.text != text { view.text = text }
        let length = (view.text as NSString).length
        if selection.location <= length,
           selection.length <= length - selection.location,
           view.selectedRange != selection {
            view.selectedRange = selection
        }
    }

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    final class Coordinator: NSObject, UITextViewDelegate {
        var parent: ComposerEditor
        init(_ parent: ComposerEditor) { self.parent = parent }
        func textViewDidChange(_ textView: UITextView) {
            parent.text = textView.text
            parent.selection = textView.selectedRange
        }
        func textViewDidChangeSelection(_ textView: UITextView) {
            parent.selection = textView.selectedRange
        }
    }
}

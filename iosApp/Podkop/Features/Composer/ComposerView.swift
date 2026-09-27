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
                    ComposerFormattingBar(text: $model.text, selection: $model.selection, disabled: model.submitting)
                    ComposerEditor(text: $model.text, selection: $model.selection)
                        .frame(minHeight: 230)
                        .overlay(alignment: .topLeading) {
                            // Matches UITextView's inset (8) plus its line fragment padding (5).
                            if model.text.isEmpty {
                                Text(model.intent.hint)
                                    .font(.body)
                                    .foregroundStyle(.tertiary)
                                    .padding(.top, 12)
                                    .padding(.leading, 13)
                                    .allowsHitTesting(false)
                                    .accessibilityHidden(true)
                            }
                        }
                        .background(PodkopTheme.background, in: RoundedRectangle(cornerRadius: 12))
                        .overlay(RoundedRectangle(cornerRadius: 12).strokeBorder(Color.primary.opacity(0.4), lineWidth: 1.5))
                        .disabled(model.submitting)
                        .accessibilityIdentifier("composerEditor")
                    AdaptiveControlRow {
                        Toggle("18+", isOn: $model.adult).podkopSwitch().fixedSize()
                            .accessibilityLabel(.commonAdultContent)
                            .disabled(model.submitting)
                    } trailing: {
                        ComposerAttachmentControls(attachment: model.attachment, disabled: model.submitting, compact: true)
                    }
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
                }
                .padding()
                .frame(maxWidth: 760)
                .frame(maxWidth: .infinity)
            }
            .safeAreaInset(edge: .bottom) {
                submitButton.padding(16).background(PodkopTheme.card)
            }
            .scrollDismissesKeyboard(.interactively)
            .background(PodkopTheme.card)
            .background(KeyboardDismissArea())
            .navigationTitle(model.intent.title)
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

    private var submitButton: some View {
        Button { model.submit() } label: {
            if model.submitting { ProgressView().frame(maxWidth: .infinity) }
            else { Text(model.intent.target.isEdit ? .composerSaveChanges : .commonSend)
                    .frame(maxWidth: .infinity) }
        }
        .buttonStyle(.borderedProminent)
        .buttonBorderShape(.capsule)
        .tint(.primary)
        .foregroundStyle(PodkopTheme.background)
        .controlSize(.large)
        .disabled(!model.canSubmit)
        .accessibilityIdentifier("composerSubmit")
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
        view.isEditable = context.environment.isEnabled
        view.textContainerInset = UIEdgeInsets(top: 12, left: 8, bottom: 12, right: 8)
        view.text = text
        view.selectedRange = selection
        return view
    }

    func updateUIView(_ view: UITextView, context: Context) {
        context.coordinator.parent = self
        view.isEditable = context.environment.isEnabled
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

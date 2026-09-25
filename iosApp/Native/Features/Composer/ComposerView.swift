import SwiftUI
import UIKit
import PhotosUI
import UniformTypeIdentifiers

struct NativeComposerView: View {
    @State private var model: ComposerModel
    let dependencies: AppDependencies
    @State private var showDiscard = false
    @State private var showPhotoURL = false
    @State private var photoURLInput = ""
    @State private var selectedPhoto: PhotosPickerItem?

    init(intent: ComposerIntent, seed: NativeResource?, dependencies: AppDependencies) {
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
                        Text("Reply to \(target)").font(.subheadline).foregroundStyle(.secondary)
                    }
                    formatBar
                    NativeComposerEditor(text: $model.text, selection: $model.selection)
                        .frame(minHeight: 230)
                        .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 12))
                        .accessibilityIdentifier("composerEditor")
                    Toggle("Adult content", isOn: $model.adult)
                    HStack {
                        PhotosPicker(selection: $selectedPhoto, matching: .images) {
                            Label("Choose photo", systemImage: "photo.on.rectangle")
                        }
                        Button("Photo URL") { showPhotoURL = true }
                    }
                    .disabled(model.submitting || model.mediaUploading)
                    if model.mediaUploading { ProgressView("Uploading photo…") }
                    if model.photoKey != nil {
                        HStack {
                            Label("Attached photo", systemImage: "photo")
                                .foregroundStyle(.secondary)
                            Button("Remove", role: .destructive) { model.removePhoto() }
                                .disabled(model.submitting || model.mediaUploading)
                        }
                    }
                    if model.mediaFailed {
                        Label("Could not attach photo. Try again.", systemImage: "exclamationmark.triangle")
                            .foregroundStyle(.red)
                    }
                    if model.failed {
                        Label(model.outcomeUnknown
                              ? "Submission status is unclear. Check your content before sending again."
                              : "Could not send. Your text is saved here.",
                              systemImage: "exclamationmark.triangle")
                            .foregroundStyle(.red)
                    }
                    if model.outcomeUnknown {
                        Button("I checked; allow retry") { model.acknowledgeUnknownOutcome() }
                    }
                    Button { model.submit() } label: {
                        if model.submitting { ProgressView().frame(maxWidth: .infinity) }
                        else { Text(model.intent.target.isEdit ? "Save changes" : "Send")
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
            .navigationTitle(model.intent.target.isEdit ? "Edit" : "Write a post")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Cancel") {
                        if model.isDirty { showDiscard = true }
                        else { model.discard(); dependencies.router.dismissSheet() }
                    }
                    .disabled(model.submitting || model.mediaUploading)
                }
            }
        }
        .interactiveDismissDisabled(model.isDirty || model.submitting || model.mediaUploading)
        .alert("Discard your changes?", isPresented: $showDiscard) {
            Button("Discard", role: .destructive) {
                model.discard()
                dependencies.router.dismissSheet()
            }
            Button("Keep writing", role: .cancel) {}
        }
        .alert("Photo URL", isPresented: $showPhotoURL) {
            TextField("https://example.com/photo.jpg", text: $photoURLInput)
            Button("Attach") { model.attachURL(photoURLInput) }
            Button("Cancel", role: .cancel) {}
        }
        .onChange(of: selectedPhoto) { _, item in
            guard let item else { return }
            Task {
                do {
                    guard let loaded = try await item.loadTransferable(type: Data.self) else {
                        model.photoSelectionFailed()
                        return
                    }
                    let type = item.supportedContentTypes.first { $0.conforms(to: .image) }
                    let originalMime = type?.preferredMIMEType ?? "image/jpeg"
                    if originalMime == "image/heic" || originalMime == "image/heif" {
                        guard let jpeg = UIImage(data: loaded)?.jpegData(compressionQuality: 0.85) else {
                            model.photoSelectionFailed()
                            return
                        }
                        model.attachDevice(jpeg, fileName: "photo.jpg", mimeType: "image/jpeg")
                    } else {
                        let ext = type?.preferredFilenameExtension ?? "jpg"
                        model.attachDevice(loaded, fileName: "photo.\(ext)", mimeType: originalMime)
                    }
                } catch { model.photoSelectionFailed() }
                selectedPhoto = nil
            }
        }
        .onChange(of: model.submittedResource) { _, value in
            if value != nil { dependencies.router.dismissSheet() }
        }
    }

    private var formatBar: some View {
        ScrollView(.horizontal) {
            HStack(spacing: 8) {
                formatButton("Bold", symbol: "bold", prefix: "**", suffix: "**", placeholder: "bold")
                formatButton("Italic", symbol: "italic", prefix: "__", suffix: "__", placeholder: "italic")
                formatButton("Code", symbol: "chevron.left.forwardslash.chevron.right",
                             prefix: "`", suffix: "`", placeholder: "code")
                formatButton("Link", symbol: "link", prefix: "[", suffix: "](url)",
                             placeholder: "description")
                formatButton("Quote", symbol: "text.quote", prefix: ">", suffix: "", placeholder: "quote")
                Button { model.insertSpoilerAtLineStart() } label: {
                    Label("Spoiler", systemImage: "eye.slash")
                }
                .buttonStyle(.bordered)
                .disabled(model.submitting)
            }
        }
    }

    private func formatButton(_ title: LocalizedStringKey, symbol: String,
                              prefix: String, suffix: String, placeholder: String) -> some View {
        Button { model.insert(prefix: prefix, suffix: suffix, placeholder: placeholder) } label: {
            Label(title, systemImage: symbol)
        }
        .buttonStyle(.bordered)
        .disabled(model.submitting)
    }
}

struct NativeComposerEditor: UIViewRepresentable {
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
        var parent: NativeComposerEditor
        init(_ parent: NativeComposerEditor) { self.parent = parent }
        func textViewDidChange(_ textView: UITextView) {
            parent.text = textView.text
            parent.selection = textView.selectedRange
        }
        func textViewDidChangeSelection(_ textView: UITextView) {
            parent.selection = textView.selectedRange
        }
    }
}

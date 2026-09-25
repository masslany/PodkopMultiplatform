import SwiftUI
import PhotosUI
import UniformTypeIdentifiers

struct NativeLinkSubmissionView: View {
    @State private var model: LinkSubmissionModel
    let dependencies: AppDependencies
    @Environment(\.scenePhase) private var scenePhase
    @State private var deleteKey: String?

    init(dependencies: AppDependencies) {
        _model = State(initialValue: LinkSubmissionModel(service: dependencies.linkDrafting,
                                                        media: dependencies.linkDraftMedia))
        self.dependencies = dependencies
    }

    @State private var showPhotoURL = false
    @State private var photoURLInput = ""
    @State private var selectedPhoto: PhotosPickerItem?

    var body: some View {
        Form {
            switch model.stage {
            case .start: startSections
            case .similar: similarSections
            case .draft: draftSections
            }
            if model.busy { ProgressView("Loading…") }
            if model.failed {
                Label(model.outcomeUnknown
                      ? "Request status is unclear. Check saved drafts or published links before trying again."
                      : "Could not complete this action. Try again.",
                      systemImage: "exclamationmark.triangle")
                    .foregroundStyle(.red)
            }
            if model.outcomeUnknown {
                Button("I checked; allow retry") { model.acknowledgeUnknownOutcome() }
            }
        }
        .navigationTitle("Add link")
        .navigationBarBackButtonHidden(model.stage != .start)
        .toolbar {
            if model.stage != .start {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Back") {
                        if model.stage == .draft { model.saveAndBack() }
                        else { model.backToStart() }
                    }
                    .disabled(model.busy)
                }
            }
        }
        .confirmationDialog("Delete saved draft?", isPresented: Binding(
            get: { deleteKey != nil }, set: { if !$0 { deleteKey = nil } }
        )) {
            Button("Delete", role: .destructive) {
                if let deleteKey { model.deleteDraft(deleteKey) }
                deleteKey = nil
            }
            Button("Cancel", role: .cancel) { deleteKey = nil }
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
                    let mime = type?.preferredMIMEType ?? "image/jpeg"
                    if mime == "image/heic" || mime == "image/heif" {
                        guard let jpeg = UIImage(data: loaded)?.jpegData(compressionQuality: 0.85) else {
                            model.photoSelectionFailed()
                            return
                        }
                        model.attachDevice(jpeg, fileName: "photo.jpg", mimeType: "image/jpeg")
                    } else {
                        model.attachDevice(loaded,
                            fileName: "photo.\(type?.preferredFilenameExtension ?? "jpg")", mimeType: mime)
                    }
                } catch { model.photoSelectionFailed() }
                selectedPhoto = nil
            }
        }
        .task { model.start() }
        .onDisappear { model.saveOnExit() }
        .onChange(of: scenePhase) { _, value in
            if value == .background { model.saveOnExit() }
        }
        .onChange(of: model.published) { _, value in
            if value {
                dependencies.resourceUpdates.publishNewResource()
                dependencies.router.banner = String(localized: "Link published")
                dependencies.router.selectedTab = .links
                dependencies.router.replacePath([], for: .links)
            }
        }
    }

    @ViewBuilder private var startSections: some View {
        Section("Link URL") {
            TextField("https://example.com/article", text: $model.url)
                .keyboardType(.URL)
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()
            Button("Continue") { model.checkURL() }
                .disabled(model.busy || model.outcomeUnknown ||
                          model.url.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
        }
        Section("Saved drafts") {
            if model.drafts.isEmpty {
                Text("No saved drafts").foregroundStyle(.secondary)
            } else {
                ForEach(model.drafts) { draft in
                    HStack {
                        Button {
                            model.openDraft(draft.key)
                        } label: {
                            VStack(alignment: .leading) {
                                Text(draft.title.isEmpty ? draft.url : draft.title)
                                Text(draft.url).font(.caption).foregroundStyle(.secondary)
                            }
                        }
                        Spacer()
                        Button(role: .destructive) { deleteKey = draft.key } label: {
                            Image(systemName: "trash")
                        }
                    }
                    .disabled(model.busy)
                }
            }
        }
    }

    @ViewBuilder private var similarSections: some View {
        Section {
            if model.duplicate { Text("This link may already have been submitted.") }
            if !model.similar.isEmpty { Text("Similar links") }
            ForEach(model.similar) { link in
                VStack(alignment: .leading) {
                    Text(link.title)
                    Text(link.sourceLabel ?? link.sourceURL ?? "")
                        .font(.caption).foregroundStyle(.secondary)
                }
            }
            Button("Continue anyway") { model.continueDespiteSimilar() }
                .disabled(model.busy)
            Button("Change URL") { model.backToStart() }
        }
    }

    @ViewBuilder private var draftSections: some View {
        Section("Source") { Text(model.url).textSelection(.enabled) }
        Section("Details") {
            TextField("Title", text: $model.title)
            TextField("Description", text: $model.description, axis: .vertical)
                .lineLimit(3...6)
            TextField("Tags, separated by commas", text: $model.tagsText)
                .textInputAutocapitalization(.never)
            ForEach(model.tagSuggestions, id: \.self) { tag in
                Button("#\(tag)") { model.selectTag(tag) }
            }
            Toggle("Adult content", isOn: $model.adult)
        }
        if !model.suggestedImages.isEmpty {
            Section("Suggested image") {
                Picker("Image", selection: Binding(
                    get: { model.selectedImageIndex },
                    set: { model.selectImage($0) }
                )) {
                    Text("None").tag(Int?.none)
                    ForEach(model.suggestedImages.indices, id: \.self) { index in
                        Text("Image \(index + 1)").tag(Optional(index))
                    }
                }
                .disabled(model.busy || model.mediaUploading)
                if model.imageSaving { ProgressView("Saving image choice…") }
            }
        }
        Section("Photo") {
            PhotosPicker(selection: $selectedPhoto, matching: .images) {
                Label("Choose photo", systemImage: "photo.on.rectangle")
            }
            Button("Photo URL") { showPhotoURL = true }
            if model.mediaUploading { ProgressView("Uploading photo…") }
            if model.photoKey != nil {
                HStack {
                    Text("Attached photo").foregroundStyle(.secondary)
                    Spacer()
                    Button("Remove", role: .destructive) { model.removePhoto() }
                }
            }
            if model.mediaFailed {
                Text("Could not attach photo. Try again.").foregroundStyle(.red)
            }
        }
        .disabled(model.busy || model.mediaUploading)
        Section {
            Button("Publish") { model.publish() }
                .disabled(!model.canPublish)
            Button("Save draft") { model.saveAndBack() }
                .disabled(model.busy || model.mediaUploading || model.imageSaving)
        }
    }
}

import SwiftUI
import PhotosUI
import UniformTypeIdentifiers

struct LinkSubmissionView: View {
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
        PodkopList {
            switch model.stage {
            case .start: startSections
            case .similar: similarSections
            case .draft: draftSections
            }
            if model.busy { ProgressView(.commonLoading) }
            if model.failed {
                Label(model.outcomeUnknown
                      ? .composerRequestStatusUnclearCheck
                      : .commonCouldNotCompleteAction,
                      systemImage: "exclamationmark.triangle")
                    .foregroundStyle(.red)
            }
            if model.outcomeUnknown {
                Button(.commonICheckedAllowRetry) { model.acknowledgeUnknownOutcome() }
            }
        }
        .navigationTitle(.commonAddLink)
        .navigationBarBackButtonHidden(model.stage != .start)
        .toolbar {
            if model.stage != .start {
                ToolbarItem(placement: .topBarLeading) {
                    Button(.commonBack) {
                        if model.stage == .draft { model.saveAndBack() }
                        else { model.backToStart() }
                    }
                    .disabled(model.busy)
                }
            }
        }
        .confirmationDialog(.composerDeleteSavedDraft, isPresented: Binding(
            get: { deleteKey != nil }, set: { if !$0 { deleteKey = nil } }
        )) {
            Button(.commonDelete, role: .destructive) {
                if let deleteKey { model.deleteDraft(deleteKey) }
                deleteKey = nil
            }
            Button(.commonCancel, role: .cancel) { deleteKey = nil }
        }
        .alert(.composerPhotoURL, isPresented: $showPhotoURL) {
            TextField(String("https://example.com/photo.jpg"), text: $photoURLInput)
            Button(.composerAttach) { model.attachURL(photoURLInput) }
            Button(.commonCancel, role: .cancel) {}
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
                dependencies.router.banner = String(localized: .composerLinkPublished)
                dependencies.router.selectedTab = .links
                dependencies.router.replacePath([], for: .links)
            }
        }
    }

    @ViewBuilder private var startSections: some View {
        Section(.composerLinkURL) {
            TextField(String("https://example.com/article"), text: $model.url)
                .keyboardType(.URL)
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()
            Button(.composerContinue) { model.checkURL() }
                .disabled(model.busy || model.outcomeUnknown ||
                          model.url.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
        }
        Section(.composerSavedDrafts) {
            if model.drafts.isEmpty {
                Text(.composerNoSavedDrafts).foregroundStyle(.secondary)
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
            if model.duplicate { Text(.composerLinkMayAlreadyHave) }
            if !model.similar.isEmpty { Text(.composerSimilarLinks) }
            ForEach(model.similar) { link in
                VStack(alignment: .leading) {
                    Text(link.title)
                    Text(link.sourceLabel ?? link.sourceURL ?? "")
                        .font(.caption).foregroundStyle(.secondary)
                }
            }
            Button(.composerContinueAnyway) { model.continueDespiteSimilar() }
                .disabled(model.busy)
            Button(.composerChangeURL) { model.backToStart() }
        }
    }

    @ViewBuilder private var draftSections: some View {
        Section(.composerSource) { Text(model.url).textSelection(.enabled) }
        Section(.composerDetails) {
            TextField(String(localized: .composerTitle), text: $model.title)
            TextField(String(localized: .composerDescription), text: $model.description, axis: .vertical)
                .lineLimit(3...6)
            TextField(String(localized: .composerTagsSeparatedCommas), text: $model.tagsText)
                .textInputAutocapitalization(.never)
            ForEach(model.tagSuggestions, id: \.self) { tag in
                Button("#" + tag) { model.selectTag(tag) }
            }
            Toggle(.commonAdultContent, isOn: $model.adult).podkopSwitch()
        }
        if !model.suggestedImages.isEmpty {
            Section(.composerSuggestedImage) {
                Picker(.commonImage, selection: Binding(
                    get: { model.selectedImageIndex },
                    set: { model.selectImage($0) }
                )) {
                    Text(.composerNoImage).tag(Int?.none)
                    ForEach(model.suggestedImages.indices, id: \.self) { index in
                        Text(.composerImageNumber(index + 1)).tag(Optional(index))
                    }
                }
                .disabled(model.busy || model.mediaUploading)
                if model.imageSaving { ProgressView(.composerSavingImageChoice) }
            }
        }
        Section(.composerPhoto) {
            PhotosPicker(selection: $selectedPhoto, matching: .images) {
                Label(.composerChoosePhoto, systemImage: "photo.on.rectangle")
            }
            Button(.composerPhotoURL) { showPhotoURL = true }
            if model.mediaUploading { ProgressView(.composerUploadingPhoto) }
            if model.photoKey != nil {
                HStack {
                    Text(.composerAttachedPhoto).foregroundStyle(.secondary)
                    Spacer()
                    Button(.commonRemove, role: .destructive) { model.removePhoto() }
                }
            }
            if model.mediaFailed {
                Text(.composerCouldNotAttachPhoto).foregroundStyle(.red)
            }
        }
        .disabled(model.busy || model.mediaUploading)
        Section {
            Button(.composerPublish) { model.publish() }
                .disabled(!model.canPublish)
            Button(.composerSaveDraft) { model.saveAndBack() }
                .disabled(model.busy || model.mediaUploading || model.imageSaving)
        }
    }
}

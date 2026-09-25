import Foundation
import Observation
import PodkopShared

struct NativeLinkDraft: Identifiable, Hashable {
    let key: String
    let url: String
    let title: String
    let description: String
    let tags: [String]
    let adult: Bool
    let photoKey: String?
    let photoURL: String?
    let suggestedImages: [String]
    let selectedImageIndex: Int?
    var id: String { key }

    init(_ value: IOSLinkDraft) {
        key = value.key
        url = value.url
        title = value.title
        description = value.description_
        tags = value.tags
        adult = value.adult
        photoKey = value.photoKey
        photoURL = value.photoUrl
        suggestedImages = value.suggestedImages
        selectedImageIndex = value.selectedImageIndex?.intValue
    }
}

struct NativeLinkCheck {
    let key: String
    let duplicate: Bool
    let similar: [NativeResource]
}

struct LinkDraftValues {
    let title: String
    let description: String?
    let tags: [String]
    let photoKey: String?
    let adult: Bool
    let selectedImageIndex: Int?
}

@MainActor protocol LinkDrafting {
    func check(_ url: String) async throws -> NativeLinkCheck
    func list() async throws -> [NativeLinkDraft]
    func get(_ key: String) async throws -> NativeLinkDraft
    func suggest(_ query: String) async throws -> [String]
    func save(_ key: String, values: LinkDraftValues) async throws
    func publish(_ key: String, values: LinkDraftValues) async throws
    func delete(_ key: String) async throws
}

@MainActor
final class SharedLinkDrafting: LinkDrafting {
    private let client: PodkopClient
    private let adapter: BridgeAdapter
    init(client: PodkopClient, adapter: BridgeAdapter) {
        self.client = client
        self.adapter = adapter
    }

    func check(_ url: String) async throws -> NativeLinkCheck {
        let value: IOSLinkDraftCheck = try await adapter.call {
            self.client.linkDrafts.check(url: url, completion: $0)
        }
        return NativeLinkCheck(key: value.key, duplicate: value.duplicate,
                               similar: value.similar.map(NativeResource.init))
    }
    func list() async throws -> [NativeLinkDraft] {
        let value: [IOSLinkDraft] = try await adapter.call {
            self.client.linkDrafts.list(completion: $0)
        }
        return value.map(NativeLinkDraft.init)
    }
    func get(_ key: String) async throws -> NativeLinkDraft {
        let value: IOSLinkDraft = try await adapter.call {
            self.client.linkDrafts.get(key: key, completion: $0)
        }
        return NativeLinkDraft(value)
    }
    func suggest(_ query: String) async throws -> [String] {
        try await adapter.call { self.client.linkDrafts.suggest(query: query, completion: $0) }
    }
    func save(_ key: String, values: LinkDraftValues) async throws {
        let _: IOSSuccess = try await adapter.call {
            self.client.linkDrafts.save(key: key, title: values.title,
                description: values.description, tags: values.tags, photoKey: values.photoKey,
                adult: values.adult,
                selectedImageIndex: values.selectedImageIndex.map { KotlinInt(int: Int32($0)) },
                completion: $0)
        }
    }
    func publish(_ key: String, values: LinkDraftValues) async throws {
        let _: IOSSuccess = try await adapter.call {
            self.client.linkDrafts.publish(key: key, title: values.title,
                description: values.description, tags: values.tags, photoKey: values.photoKey,
                adult: values.adult,
                selectedImageIndex: values.selectedImageIndex.map { KotlinInt(int: Int32($0)) },
                completion: $0)
        }
    }
    func delete(_ key: String) async throws {
        let _: IOSSuccess = try await adapter.call {
            self.client.linkDrafts.delete(key: key, completion: $0)
        }
    }
}

@MainActor @Observable
final class LinkSubmissionModel {
    enum Stage { case start, similar, draft }
    var stage: Stage = .start
    var url = ""
    var title = ""
    var description = ""
    var tagsText = "" { didSet { requestTagSuggestions() } }
    private(set) var tagSuggestions: [String] = []
    var adult = false
    private(set) var photoKey: String?
    private(set) var photoURL: String?
    private(set) var mediaUploading = false
    private(set) var mediaFailed = false
    private(set) var drafts: [NativeLinkDraft] = []
    private(set) var similar: [NativeResource] = []
    private(set) var duplicate = false
    private(set) var suggestedImages: [String] = []
    private(set) var selectedImageIndex: Int?
    private(set) var imageSaving = false
    private(set) var busy = false
    private(set) var failed = false
    private(set) var outcomeUnknown = false
    private(set) var published = false
    private(set) var currentKey: String?
    private let service: LinkDrafting
    private let media: ComposerMediaHandling?
    private var ownedPhotoKey: String?
    private var mediaGeneration = 0
    private var pendingKey: String?
    private var generation = 0
    private var started = false
    private var suggestionTask: Task<Void, Never>?
    private var suggestionGeneration = 0
    private var confirmedImageIndex: Int?

    init(service: LinkDrafting, media: ComposerMediaHandling? = nil) {
        self.service = service
        self.media = media
    }

    func start() {
        guard !started else { return }
        started = true
        reloadDrafts()
    }

    func reloadDrafts() {
        Task { [weak self] in
            guard let self else { return }
            do { drafts = try await service.list() }
            catch { failed = true }
        }
    }

    func checkURL() {
        guard !busy, !outcomeUnknown,
              let parsed = URL(string: url.trimmingCharacters(in: .whitespacesAndNewlines)),
              ["http", "https"].contains(parsed.scheme?.lowercased() ?? "") else {
            failed = true
            return
        }
        generation += 1
        let request = generation
        busy = true
        failed = false
        Task { [weak self] in
            guard let self else { return }
            do {
                let result = try await service.check(parsed.absoluteString)
                guard request == generation else { return }
                similar = result.similar
                duplicate = result.duplicate
                if result.duplicate || !result.similar.isEmpty {
                    pendingKey = result.key
                    stage = .similar
                } else {
                    openDraft(result.key)
                }
            } catch {
                if request == generation {
                    failed = true
                    if let failure = error as? BridgeFailure {
                        outcomeUnknown = failure.category == "unknown" || failure.category == "server"
                        if outcomeUnknown { reloadDrafts() }
                    }
                }
            }
            if request == generation { busy = false }
        }
    }

    func continueDespiteSimilar() {
        guard let pendingKey else { return }
        self.pendingKey = nil
        openDraft(pendingKey)
    }

    func backToStart() {
        generation += 1
        stage = .start
        pendingKey = nil
        currentKey = nil
        reloadDrafts()
    }

    func openDraft(_ key: String) {
        generation += 1
        let request = generation
        busy = true
        failed = false
        Task { [weak self] in
            guard let self else { return }
            do {
                let draft = try await service.get(key)
                guard request == generation else { return }
                currentKey = key
                url = draft.url
                title = draft.title
                description = draft.description
                tagsText = draft.tags.joined(separator: ", ")
                adult = draft.adult
                photoKey = draft.photoKey
                photoURL = draft.photoURL
                ownedPhotoKey = nil
                suggestedImages = draft.suggestedImages
                selectedImageIndex = draft.selectedImageIndex
                confirmedImageIndex = draft.selectedImageIndex
                stage = .draft
            } catch {
                if request == generation { failed = true }
            }
            if request == generation { busy = false }
        }
    }

    var normalizedTags: [String] {
        var seen = Set<String>()
        return tagsText.split(whereSeparator: { $0 == "," || $0.isWhitespace })
            .map { $0.trimmingCharacters(in: .whitespacesAndNewlines)
                .replacingOccurrences(of: "^#", with: "", options: .regularExpression)
                .lowercased() }
            .filter { !$0.isEmpty && seen.insert($0).inserted }
    }

    private func requestTagSuggestions() {
        suggestionGeneration += 1
        suggestionTask?.cancel()
        if tagsText.last.map({ $0 == "," || $0.isWhitespace }) == true {
            tagSuggestions = []
            return
        }
        let query = String(tagsText.split(whereSeparator: { $0 == "," || $0.isWhitespace })
            .last ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
            .replacingOccurrences(of: "^#", with: "", options: .regularExpression)
            .lowercased()
        guard query.count >= 2 else { tagSuggestions = []; return }
        let request = suggestionGeneration
        suggestionTask = Task { [weak self] in
            guard let self else { return }
            try? await Task.sleep(for: .milliseconds(350))
            guard !Task.isCancelled else { return }
            do {
                let values = try await service.suggest(query)
                guard request == suggestionGeneration, !Task.isCancelled else { return }
                let existing = Set(normalizedTags.dropLast())
                tagSuggestions = Array(Set(values.map { $0.lowercased() }))
                    .filter { !existing.contains($0) }.sorted()
            } catch {
                if request == suggestionGeneration { tagSuggestions = [] }
            }
        }
    }

    func selectTag(_ value: String) {
        let previous = tagsText.split(whereSeparator: { $0 == "," || $0.isWhitespace })
            .dropLast().map(String.init)
        tagsText = (previous + [value]).joined(separator: ", ") + ", "
        tagSuggestions = []
    }

    var canPublish: Bool {
        !busy && !mediaUploading && !imageSaving && !outcomeUnknown && currentKey != nil && !title.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty &&
            !normalizedTags.isEmpty
    }

    private var values: LinkDraftValues {
        let trimmedDescription = description.trimmingCharacters(in: .whitespacesAndNewlines)
        return LinkDraftValues(title: title.trimmingCharacters(in: .whitespacesAndNewlines),
            description: trimmedDescription.isEmpty ? nil : trimmedDescription,
            tags: normalizedTags, photoKey: photoKey, adult: adult,
            selectedImageIndex: selectedImageIndex)
    }

    func saveAndBack() {
        guard let key = currentKey, !busy, !mediaUploading, !imageSaving else { return }
        busy = true
        failed = false
        Task { [weak self] in
            guard let self else { return }
            do {
                try await service.save(key, values: values)
                ownedPhotoKey = nil
                busy = false
                backToStart()
            } catch {
                busy = false
                failed = true
            }
        }
    }

    func saveOnExit() {
        guard stage == .draft, let key = currentKey, !busy, !mediaUploading, !imageSaving else { return }
        busy = true
        let snapshot = values
        Task { [weak self] in
            guard let self else { return }
            do {
                try await service.save(key, values: snapshot)
                ownedPhotoKey = nil
            } catch { failed = true }
            busy = false
        }
    }

    func publish() {
        guard canPublish, let key = currentKey else { return }
        busy = true
        failed = false
        let submitted = values
        Task { [weak self] in
            guard let self else { return }
            do {
                try await service.publish(key, values: submitted)
                ownedPhotoKey = nil
                published = true
                busy = false
                backToStart()
            } catch {
                busy = false
                failed = true
                if let failure = error as? BridgeFailure {
                    outcomeUnknown = failure.category == "unknown" || failure.category == "server"
                }
            }
        }
    }

    func acknowledgeUnknownOutcome() { outcomeUnknown = false }

    func selectImage(_ index: Int?) {
        guard !busy, !mediaUploading, currentKey != nil,
              index.map({ suggestedImages.indices.contains($0) }) ?? true else { return }
        selectedImageIndex = index
        guard !imageSaving else { return }
        imageSaving = true
        Task { [weak self] in
            guard let self else { return }
            while let key = currentKey, selectedImageIndex != confirmedImageIndex {
                let target = selectedImageIndex
                let snapshot = values
                do {
                    try await service.save(key, values: snapshot)
                    confirmedImageIndex = target
                } catch {
                    if selectedImageIndex == target {
                        selectedImageIndex = confirmedImageIndex
                        failed = true
                    }
                }
            }
            imageSaving = false
        }
    }

    func attachURL(_ rawURL: String) {
        guard let media, !busy, !mediaUploading,
              let url = URL(string: rawURL.trimmingCharacters(in: .whitespacesAndNewlines)),
              ["http", "https"].contains(url.scheme?.lowercased() ?? "") else {
            mediaFailed = true
            return
        }
        upload(using: { try await media.uploadURL(url.absoluteString) })
    }

    func attachDevice(_ data: Data, fileName: String, mimeType: String) {
        guard let media, !busy, !mediaUploading else { return }
        upload(using: { try await media.uploadDevice(data, fileName: fileName, mimeType: mimeType) })
    }

    private func upload(using operation: @escaping () async throws -> ComposerPhoto) {
        mediaGeneration += 1
        let request = mediaGeneration
        mediaUploading = true
        mediaFailed = false
        Task { [weak self] in
            guard let self else { return }
            do {
                let photo = try await operation()
                guard request == mediaGeneration else {
                    if let media { try? await media.delete(photo.key) }
                    return
                }
                let oldOwned = ownedPhotoKey
                photoKey = photo.key
                photoURL = photo.url
                ownedPhotoKey = photo.key
                if let oldOwned, oldOwned != photo.key,
                   let media { try? await media.delete(oldOwned) }
            } catch {
                if request == mediaGeneration { mediaFailed = true }
            }
            if request == mediaGeneration { mediaUploading = false }
        }
    }

    func removePhoto() {
        guard !busy, !mediaUploading else { return }
        let owned = ownedPhotoKey
        ownedPhotoKey = nil
        photoKey = nil
        photoURL = nil
        if let owned, let media { Task { try? await media.delete(owned) } }
    }

    func photoSelectionFailed() { mediaFailed = true }

    func deleteDraft(_ key: String) {
        guard !busy else { return }
        busy = true
        failed = false
        Task { [weak self] in
            guard let self else { return }
            do {
                try await service.delete(key)
                drafts.removeAll { $0.key == key }
            } catch { failed = true }
            busy = false
        }
    }
}

#if DEBUG
@MainActor
final class FixtureLinkDrafting: LinkDrafting {
    func check(_ url: String) async throws -> NativeLinkCheck {
        NativeLinkCheck(key: "fixture-draft", duplicate: false, similar: [])
    }
    func list() async throws -> [NativeLinkDraft] { [] }
    func get(_ key: String) async throws -> NativeLinkDraft {
        NativeLinkDraft(IOSLinkDraft(key: key, url: "https://example.com", title: "",
            description: "", tags: [], adult: false, photoKey: nil, photoUrl: nil,
            suggestedImages: [], selectedImageIndex: nil))
    }
    func suggest(_ query: String) async throws -> [String] { [] }
    func save(_ key: String, values: LinkDraftValues) async throws {}
    func publish(_ key: String, values: LinkDraftValues) async throws {}
    func delete(_ key: String) async throws {}
}
#endif

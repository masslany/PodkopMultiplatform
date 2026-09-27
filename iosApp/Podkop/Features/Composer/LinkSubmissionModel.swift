import Foundation
import Observation
import PodkopShared

@MainActor @Observable
final class LinkSubmissionModel {
    enum Stage { case start, similar, draft }
    var stage: Stage = .start
    var url = ""
    var title = ""
    var description = ""
    /// Committed tags, shown as removable chips (Android's `tags`).
    private(set) var tags: [String] = []
    /// What is being typed; a comma, space or Return turns it into chips, like Android.
    var tagInput = "" {
        didSet {
            guard tagInput != oldValue else { return }
            let trailing = String(tagInput.reversed().prefix(while: { !Self.isTagSeparator($0) }).reversed())
            if trailing != tagInput {
                tags = Self.merge(tags, Self.normalize(String(tagInput.dropLast(trailing.count))))
                tagInput = trailing
                return
            }
            requestTagSuggestions()
        }
    }
    private(set) var tagSuggestions: [String] = []
    var adult = false
    private(set) var photoKey: String?
    private(set) var photoURL: String?
    private(set) var mediaUploading = false
    private(set) var mediaFailed = false
    private(set) var drafts: [LinkDraft] = []
    private(set) var similar: [Resource] = []
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
                tags = Self.normalize(draft.tags.joined(separator: " "))
                tagInput = ""
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

    /// The chips plus anything still typed, as Android publishes them.
    var normalizedTags: [String] { Self.merge(tags, Self.normalize(tagInput)) }

    /// Return in the tag field (Android's `onPendingTagSubmitted`).
    func submitPendingTag() {
        let additions = Self.normalize(tagInput)
        guard !additions.isEmpty else { return }
        tags = Self.merge(tags, additions)
        tagInput = ""
        tagSuggestions = []
    }

    func removeTag(_ tag: String) {
        tags.removeAll { $0 == tag }
    }

    private static func isTagSeparator(_ character: Character) -> Bool {
        character == "," || character.isWhitespace
    }

    /// Android's `normalizeLinkTags`: split on separators, drop "#", lowercase, no blanks or repeats.
    static func normalize(_ raw: String) -> [String] {
        merge([], raw.split(whereSeparator: isTagSeparator)
            .map { String($0).trimmingCharacters(in: .whitespacesAndNewlines)
                .replacingOccurrences(of: "^#", with: "", options: .regularExpression)
                .lowercased() }
            .filter { !$0.isEmpty })
    }

    private static func merge(_ existing: [String], _ additions: [String]) -> [String] {
        var seen = Set(existing)
        return existing + additions.filter { seen.insert($0).inserted }
    }

    private func requestTagSuggestions() {
        suggestionGeneration += 1
        suggestionTask?.cancel()
        let query = Self.normalize(tagInput).last ?? ""
        guard query.count >= 2 else { tagSuggestions = []; return }
        let request = suggestionGeneration
        suggestionTask = Task { [weak self] in
            guard let self else { return }
            try? await Task.sleep(for: .milliseconds(350))
            guard !Task.isCancelled else { return }
            do {
                let values = try await service.suggest(query)
                guard request == suggestionGeneration, !Task.isCancelled else { return }
                let existing = Set(tags)
                tagSuggestions = Array(Set(values.map { $0.lowercased() }))
                    .filter { !existing.contains($0) }.sorted()
            } catch {
                if request == suggestionGeneration { tagSuggestions = [] }
            }
        }
    }

    func selectTag(_ value: String) {
        tags = Self.merge(tags, Self.normalize(value))
        tagInput = ""
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

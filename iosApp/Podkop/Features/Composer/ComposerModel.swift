import Foundation
import Observation
import PodkopShared

@MainActor @Observable
final class ComposerModel {
    let intent: ComposerIntent
    let seed: Resource?
    var text: String
    var adult: Bool
    var selection: NSRange
    let attachment: ComposerAttachment
    var photoKey: String? { attachment.photoKey }
    var photoURL: String? { attachment.photoURL }
    var mediaUploading: Bool { attachment.uploading }
    var mediaFailed: Bool { attachment.failed }
    private(set) var submitting = false
    private(set) var failed = false
    private(set) var outcomeUnknown = false
    private(set) var submittedResource: Resource?
    private let submitter: ComposerSubmitting
    private let updates: ResourceUpdates
    private let initialText: String
    private let initialAdult: Bool
    private let initialPhotoKey: String?

    init(intent: ComposerIntent, seed: Resource?, submitter: ComposerSubmitting,
         updates: ResourceUpdates, media: ComposerMediaHandling? = nil) {
        self.intent = intent
        self.seed = seed
        self.submitter = submitter
        self.updates = updates
        let initialText = seed?.body ?? ""
        let initialAdult = seed?.adult ?? false
        let initialPhotoKey = seed?.photo?.key
        text = initialText
        selection = NSRange(location: initialText.utf16.count, length: 0)
        adult = initialAdult
        attachment = ComposerAttachment(media: media, photoKey: initialPhotoKey, photoURL: seed?.photo?.url)
        self.initialText = initialText
        self.initialAdult = initialAdult
        self.initialPhotoKey = initialPhotoKey
    }

    var isDirty: Bool {
        text != initialText || adult != initialAdult || photoKey != initialPhotoKey
    }

    var canSubmit: Bool {
        !submitting && !mediaUploading && !outcomeUnknown && submittedResource == nil &&
            !text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }

    func attachURL(_ rawURL: String) {
        guard !submitting else { return }
        attachment.attachURL(rawURL)
    }

    func attachDevice(_ data: Data, fileName: String, mimeType: String) {
        guard !submitting else { return }
        attachment.attachDevice(data, fileName: fileName, mimeType: mimeType)
    }

    func removePhoto() {
        guard !submitting else { return }
        attachment.remove()
    }

    func photoSelectionFailed() { attachment.selectionFailed() }

    func discard() { attachment.discard() }

    func insert(prefix: String, suffix: String, placeholder: String) {
        let source = text as NSString
        guard selection.location <= source.length,
              selection.length <= source.length - selection.location else { return }
        let selected = source.substring(with: selection)
        let middle = selected.isEmpty ? placeholder : selected
        text = source.replacingCharacters(in: selection, with: prefix + middle + suffix)
        selection = NSRange(location: selection.location + prefix.utf16.count,
                            length: middle.utf16.count)
    }

    func insertSpoilerAtLineStart() {
        let source = text as NSString
        guard selection.location <= source.length else { return }
        let preceding = source.substring(to: selection.location) as NSString
        let newline = preceding.range(of: "\n", options: .backwards)
        let lineStart = newline.location == NSNotFound ? 0 : newline.location + 1
        if lineStart < source.length, source.substring(with: NSRange(location: lineStart, length: 1)) == "!" {
            return
        }
        text = source.replacingCharacters(in: NSRange(location: lineStart, length: 0), with: "!")
        selection = NSRange(location: selection.location + 1, length: selection.length)
    }

    func acknowledgeUnknownOutcome() { outcomeUnknown = false }

    func submit() {
        guard canSubmit else { return }
        submitting = true
        failed = false
        outcomeUnknown = false
        let revision = updates.sessionRevision
        let content = text.trimmingCharacters(in: .whitespacesAndNewlines)
        Task { [weak self] in
            guard let self else { return }
            do {
                let result = try await submitter.submit(intent: intent, text: content,
                                                        adult: adult, photoKey: photoKey)
                guard revision == updates.sessionRevision else { submitting = false; return }
                updates.publish(.replacement(result), for: ResourceIdentity(result))
                if let seed { updates.publish(.replacement(result), for: ResourceIdentity(seed)) }
                if case .createEntry = intent { updates.publishNewResource() }
                if result.kind == .entryComment || result.kind == .linkComment,
                   let rootID = intent.target.rootID {
                    let kind: ResourceKind = result.kind == .entryComment ? .entry : .link
                    updates.publish(.invalidated, for: ResourceIdentity(
                        Resource(sourceID: rootID, kind: kind, body: "")))
                }
                submittedResource = result
            } catch {
                guard revision == updates.sessionRevision else { submitting = false; return }
                failed = true
                if let failure = error as? BridgeFailure {
                    outcomeUnknown = failure.category == "unknown" || failure.category == "server"
                }
            }
            submitting = false
        }
    }
}

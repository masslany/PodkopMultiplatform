import XCTest
import PodkopShared
@testable import Podkop

@MainActor
private final class ControlledComposerSubmitter: ComposerSubmitting {
    var calls = 0
    var receivedText: String?
    var finish: ((Result<Resource, Error>) -> Void)?

    func submit(intent: ComposerIntent, text: String, adult: Bool,
                photoKey: String?) async throws -> Resource {
        calls += 1
        receivedText = text
        return try await withCheckedThrowingContinuation { continuation in
            finish = { continuation.resume(with: $0) }
        }
    }
}

@MainActor
private final class ControlledComposerMedia: ComposerMediaHandling {
    var nextKey = "owned-1"
    var deleted: [String] = []
    func uploadURL(_ url: String) async throws -> ComposerPhoto {
        ComposerPhoto(key: nextKey, url: url, mimeType: "image/jpeg")
    }
    func uploadDevice(_ data: Data, fileName: String, mimeType: String) async throws -> ComposerPhoto {
        ComposerPhoto(key: nextKey, url: "", mimeType: mimeType)
    }
    func delete(_ key: String) async throws { deleted.append(key) }
}

@MainActor
private final class LinkDraftFixture: LinkDrafting {
    var checks = 0
    var saves = 0
    var publishes = 0
    var failSave = false
    var deferSaves = false
    var saveFinishes: [((Result<Void, Error>) -> Void)] = []
    var images: [String] = []
    var checkResult = LinkCheck(key: "draft-1", duplicate: true, similar: [])
    func check(_ url: String) async throws -> LinkCheck { checks += 1; return checkResult }
    func list() async throws -> [LinkDraft] { [] }
    func get(_ key: String) async throws -> LinkDraft {
        LinkDraft(IOSLinkDraft(key: key, url: "https://example.com", title: "Initial",
            description: "", tags: ["science"], adult: false, photoKey: nil,
            photoUrl: nil, suggestedImages: images, selectedImageIndex: nil))
    }
    func suggest(_ query: String) async throws -> [String] { [] }
    func save(_ key: String, values: LinkDraftValues) async throws {
        saves += 1
        if deferSaves {
            try await withCheckedThrowingContinuation { continuation in
                saveFinishes.append { continuation.resume(with: $0) }
            }
        }
        if failSave { throw BridgeFailure(category: "server", code: "503") }
    }
    func publish(_ key: String, values: LinkDraftValues) async throws { publishes += 1 }
    func delete(_ key: String) async throws {}
}

@MainActor
final class ComposerTests: XCTestCase {
    private func settle() async { for _ in 0..<30 { await Task.yield() } }

    func testSelectedUnicodeTextIsWrappedAndRetainsSelection() {
        let model = ComposerModel(intent: .createEntry, seed: nil,
                                  submitter: FixtureComposerSubmitter(), updates: ResourceUpdates())
        model.text = "A 👩🏽‍💻 B"
        let source = model.text as NSString
        let selected = source.range(of: "👩🏽‍💻")
        model.selection = selected
        model.insert(prefix: "**", suffix: "**", placeholder: "bold")
        XCTAssertEqual(model.text, "A **👩🏽‍💻** B")
        XCTAssertEqual((model.text as NSString).substring(with: model.selection), "👩🏽‍💻")
        model.insertSpoilerAtLineStart()
        XCTAssertEqual(model.text, "!A **👩🏽‍💻** B")
        model.insertSpoilerAtLineStart()
        XCTAssertEqual(model.text, "!A **👩🏽‍💻** B")
    }

    func testSubmitUsesTrimmedTextOnlyOnceAndPublishesConfirmedResource() async {
        let submitter = ControlledComposerSubmitter()
        let updates = ResourceUpdates()
        let seed = Resource(sourceID: 9, kind: .entry, body: "before", editable: true)
        let model = ComposerModel(intent: .editEntry(9), seed: seed,
                                  submitter: submitter, updates: updates)
        model.text = "  after  "
        model.submit()
        model.submit()
        await settle()
        XCTAssertEqual(submitter.calls, 1)
        XCTAssertEqual(submitter.receivedText, "after")
        XCTAssertEqual(updates.reconcile(seed)?.body, "before")
        let changed = Resource(sourceID: 9, kind: .entry, body: "after", editable: true)
        submitter.finish?(.success(changed))
        await settle()
        XCTAssertEqual(model.submittedResource?.body, "after")
        XCTAssertEqual(updates.reconcile(seed)?.body, "after")
    }

    func testUnknownOutcomeRequiresExplicitRetryDecision() async {
        let submitter = ControlledComposerSubmitter()
        let model = ComposerModel(intent: .createEntry, seed: nil,
                                  submitter: submitter, updates: ResourceUpdates())
        model.text = "body"
        model.submit()
        await settle()
        submitter.finish?(.failure(BridgeFailure(category: "unknown", code: nil)))
        await settle()
        XCTAssertTrue(model.outcomeUnknown)
        XCTAssertFalse(model.canSubmit)
        model.acknowledgeUnknownOutcome()
        XCTAssertTrue(model.canSubmit)
    }

    func testPhotoReplacementOnlyDeletesFlowOwnedUploads() async {
        let media = ControlledComposerMedia()
        let seed = Resource(sourceID: 9, kind: .entry, body: "before",
                                  photo: Photo(url: "https://example.com/old.jpg",
                                                     width: 1, height: 1, mimeType: "image/jpeg",
                                                     key: "server-photo"))
        let model = ComposerModel(intent: .editEntry(9), seed: seed,
                                  submitter: FixtureComposerSubmitter(), updates: ResourceUpdates(),
                                  media: media)
        model.attachURL("https://example.com/new.jpg")
        await settle()
        XCTAssertEqual(model.photoKey, "owned-1")
        XCTAssertTrue(media.deleted.isEmpty)
        media.nextKey = "owned-2"
        model.attachURL("https://example.com/other.jpg")
        await settle()
        XCTAssertEqual(model.photoKey, "owned-2")
        XCTAssertEqual(media.deleted, ["owned-1"])
        model.discard()
        await settle()
        XCTAssertEqual(media.deleted, ["owned-1", "owned-2"])
        XCTAssertFalse(media.deleted.contains("server-photo"))
    }

    func testLinkDraftDuplicateRequiresContinueAndTagsNormalize() async {
        let service = LinkDraftFixture()
        let model = LinkSubmissionModel(service: service)
        model.url = "https://example.com"
        model.checkURL()
        await settle()
        XCTAssertEqual(model.stage, .similar)
        XCTAssertEqual(service.checks, 1)
        model.continueDespiteSimilar()
        await settle()
        XCTAssertEqual(model.stage, .draft)
        model.tagsText = "#Science,  Nauka science"
        XCTAssertEqual(model.normalizedTags, ["science", "nauka"])
        XCTAssertTrue(model.canPublish)
        model.publish()
        await settle()
        XCTAssertEqual(service.publishes, 1)
        XCTAssertTrue(model.published)
    }

    func testFailedDraftSaveRetainsFieldsForRetry() async {
        let service = LinkDraftFixture()
        let model = LinkSubmissionModel(service: service)
        model.openDraft("draft-1")
        await settle()
        model.title = "Edited title"
        service.failSave = true
        model.saveAndBack()
        await settle()
        XCTAssertEqual(model.stage, .draft)
        XCTAssertEqual(model.title, "Edited title")
        XCTAssertTrue(model.failed)
        service.failSave = false
        model.saveAndBack()
        await settle()
        XCTAssertEqual(model.stage, .start)
        XCTAssertEqual(service.saves, 2)
    }

    func testSuggestedImageChangesSaveInOrder() async {
        let service = LinkDraftFixture()
        service.images = ["first", "second"]
        service.deferSaves = true
        let model = LinkSubmissionModel(service: service)
        model.openDraft("draft-1")
        await settle()
        model.selectImage(0)
        await settle()
        model.selectImage(1)
        await settle()
        XCTAssertEqual(service.saves, 1)
        XCTAssertTrue(model.imageSaving)
        service.saveFinishes[0](.success(()))
        await settle()
        XCTAssertEqual(service.saves, 2)
        service.saveFinishes[1](.success(()))
        await settle()
        XCTAssertEqual(model.selectedImageIndex, 1)
        XCTAssertFalse(model.imageSaving)
    }
}

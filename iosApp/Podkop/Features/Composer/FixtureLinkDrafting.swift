import Foundation
import Observation
import PodkopShared

#if DEBUG
@MainActor
final class FixtureLinkDrafting: LinkDrafting {
    func check(_ url: String) async throws -> LinkCheck {
        LinkCheck(key: "fixture-draft", duplicate: false, similar: [])
    }
    func list() async throws -> [LinkDraft] { [] }
    func get(_ key: String) async throws -> LinkDraft {
        LinkDraft(IOSLinkDraft(key: key, url: "https://example.com", title: "",
            description: "", tags: [], adult: false, photoKey: nil, photoUrl: nil,
            suggestedImages: [], selectedImageIndex: nil))
    }
    func suggest(_ query: String) async throws -> [String] { [] }
    func save(_ key: String, values: LinkDraftValues) async throws {}
    func publish(_ key: String, values: LinkDraftValues) async throws {}
    func delete(_ key: String) async throws {}
}
#endif

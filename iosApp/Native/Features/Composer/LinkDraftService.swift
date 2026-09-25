import Foundation
import Observation
import PodkopShared

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

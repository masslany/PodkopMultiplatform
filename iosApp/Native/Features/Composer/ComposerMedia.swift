import Foundation
import PodkopShared

struct ComposerPhoto: Equatable {
    let key: String
    let url: String
    let mimeType: String
}

@MainActor protocol ComposerMediaHandling {
    func uploadURL(_ url: String) async throws -> ComposerPhoto
    func uploadDevice(_ data: Data, fileName: String, mimeType: String) async throws -> ComposerPhoto
    func delete(_ key: String) async throws
}

enum ComposerMediaError: Error { case fileTooLarge }

@MainActor
final class SharedComposerMedia: ComposerMediaHandling {
    private let client: PodkopClient
    private let adapter: BridgeAdapter
    private let forLink: Bool

    init(client: PodkopClient, adapter: BridgeAdapter, forLink: Bool = false) {
        self.client = client
        self.adapter = adapter
        self.forLink = forLink
    }

    func uploadURL(_ url: String) async throws -> ComposerPhoto {
        let value: IOSUploadedPhoto = try await adapter.call {
            self.client.media.uploadUrl(url: url, forLink: self.forLink, completion: $0)
        }
        return ComposerPhoto(key: value.key, url: value.url, mimeType: value.mimeType)
    }

    func uploadDevice(_ data: Data, fileName: String, mimeType: String) async throws -> ComposerPhoto {
        guard !data.isEmpty, data.count <= 20 * 1024 * 1024 else {
            throw ComposerMediaError.fileTooLarge
        }
        let value: IOSUploadedPhoto = try await adapter.call {
            self.client.media.uploadDevice(data: data, fileName: fileName,
                                           mimeType: mimeType, forLink: self.forLink, completion: $0)
        }
        return ComposerPhoto(key: value.key, url: value.url, mimeType: value.mimeType)
    }

    func delete(_ key: String) async throws {
        let _: IOSSuccess = try await adapter.call { self.client.media.delete(key: key, completion: $0) }
    }
}

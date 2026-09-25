import Foundation
import Observation
import PodkopShared

@MainActor protocol ComposerSubmitting {
    func submit(intent: ComposerIntent, text: String, adult: Bool,
                photoKey: String?) async throws -> NativeResource
}

@MainActor
final class SharedComposerSubmitter: ComposerSubmitting {
    private let client: PodkopClient
    private let adapter: BridgeAdapter

    init(client: PodkopClient, adapter: BridgeAdapter) {
        self.client = client
        self.adapter = adapter
    }

    func submit(intent: ComposerIntent, text: String, adult: Bool,
                photoKey: String?) async throws -> NativeResource {
        let target = intent.target
        let value: IOSResource = try await adapter.call {
            self.client.composer.submit(kind: target.kind,
                                        rootId: target.rootID.map { KotlinInt(int: Int32($0)) },
                                        commentId: target.commentID.map { KotlinInt(int: Int32($0)) },
                                        content: text, adult: adult, photoKey: photoKey,
                                        completion: $0)
        }
        return NativeResource(value)
    }
}

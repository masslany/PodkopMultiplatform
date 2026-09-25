import PodkopShared

@MainActor protocol VoterLoading {
    func load(target: VoterTarget, page: Int) async throws -> VoterPage
}

@MainActor
final class SharedVoterLoader: VoterLoading {
    private let client: PodkopClient
    private let adapter: BridgeAdapter

    init(client: PodkopClient, adapter: BridgeAdapter) {
        self.client = client
        self.adapter = adapter
    }

    func load(target: VoterTarget, page: Int) async throws -> VoterPage {
        let value: IOSVoterPage = try await adapter.call {
            self.client.voters.load(kind: target.kind, rootId: Int32(target.rootID),
                                    commentId: target.commentID.map { KotlinInt(int: Int32($0)) },
                                    side: target.side, page: Int32(page), completion: $0)
        }
        return VoterPage(items: value.items.map {
            Voter(username: $0.username, avatarURL: $0.avatarUrl,
                        verified: $0.verified, reason: $0.reason)
        }, total: value.total?.intValue)
    }
}

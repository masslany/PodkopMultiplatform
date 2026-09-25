import PodkopShared

#if DEBUG
@MainActor
final class FixtureVoterLoader: VoterLoading {
    func load(target: VoterTarget, page: Int) async throws -> VoterPage {
        VoterPage(items: page == 1
            ? [NativeVoter(username: "Ewa-Żółw", avatarURL: "", verified: true, reason: nil)] : [],
                  total: 1)
    }
}
#endif

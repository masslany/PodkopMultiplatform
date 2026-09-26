import PodkopShared

#if DEBUG
@MainActor
final class FixtureVoterLoader: VoterLoading {
    func load(target: VoterTarget, page: Int) async throws -> VoterPage {
        VoterPage(items: page == 1
            ? [Voter(username: "Ewa-Żółw", avatarURL: "", verified: true, reason: nil, color: "green", gender: "female")] : [],
                  total: 1)
    }
}
#endif

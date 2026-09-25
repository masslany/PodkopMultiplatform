import Foundation
import Observation
import PodkopShared

#if DEBUG
@MainActor
final class FixtureSearchSuggesting: SearchSuggesting {
    func tags(_ query: String) async throws -> [TagSuggestion] {
        [TagSuggestion(name: "technologia", followers: 42)]
    }
    func users(_ query: String) async throws -> [UserSuggestion] {
        [UserSuggestion(username: "Ewa-Żółw", avatarURL: "", color: "green", gender: "female")]
    }
}
#endif

import Foundation
import Observation
import PodkopShared

#if DEBUG
@MainActor
final class FixtureSearchSuggesting: SearchSuggesting {
    func tags(_ query: String) async throws -> [NativeTagSuggestion] {
        [NativeTagSuggestion(name: "technologia", followers: 42)]
    }
    func users(_ query: String) async throws -> [NativeUserSuggestion] {
        [NativeUserSuggestion(username: "Ewa-Żółw", avatarURL: "", color: "green", gender: "female")]
    }
}
#endif

import Foundation
import Observation
import PodkopShared

struct NativeTagSuggestion: Identifiable, Equatable {
    let name: String
    let followers: Int
    var id: String { name }
}

struct NativeUserSuggestion: Identifiable, Equatable {
    let username: String
    let avatarURL: String
    let color: String
    let gender: String
    var id: String { username }
}

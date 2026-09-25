import Foundation

struct NativeVoter: Identifiable {
    let username: String
    let avatarURL: String
    let verified: Bool
    let reason: String?
    var id: String { username }
}

struct VoterPage {
    let items: [NativeVoter]
    let total: Int?
}

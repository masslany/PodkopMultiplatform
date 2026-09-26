import Foundation

struct Voter: Identifiable {
    let username: String
    let avatarURL: String
    let verified: Bool
    let reason: String?
    var color: String? = nil
    var gender: String? = nil
    var id: String { username }
}

struct VoterPage {
    let items: [Voter]
    let total: Int?
}

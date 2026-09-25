import Foundation

struct VoterTarget: Identifiable {
    let kind: String
    let rootID: Int
    let commentID: Int?
    let side: String
    var id: String { "\(kind):\(rootID):\(commentID ?? 0):\(side)" }
}

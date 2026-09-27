import Foundation

/// Why a link is buried (Android's `VoteReasonType`); the raw values are the API's names.
enum VoteReason: String, CaseIterable, Identifiable {
    case duplicate, spam, fake, wrong, invalid

    var id: String { rawValue }

    var title: LocalizedStringResource {
        switch self {
        case .duplicate: .detailsVoteReasonDuplicate
        case .spam: .detailsVoteReasonSpam
        case .fake: .detailsVoteReasonFake
        case .wrong: .detailsVoteReasonWrong
        case .invalid: .detailsVoteReasonInvalid
        }
    }
}

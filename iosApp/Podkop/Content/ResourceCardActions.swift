import SwiftUI

struct ResourceActions {
    var open: (() -> Void)?
    var openAuthor: ((String) -> Void)?
    var openTag: ((String) -> Void)?
    var openURL: ((URL) -> Void)?
    var voteUp: (() -> Void)?
    var voteDown: (() -> Void)?
    /// Burying a link asks why; the reasons open as a menu on the bury button.
    var buryLink: ((VoteReason) -> Void)?
    var favourite: (() -> Void)?
    var comment: (() -> Void)?
    var menu: (() -> Void)?
    var surveyVote: ((Int) -> Void)?
    var loadTweet: ((String) async throws -> TweetPreview)?
    /// Set only while inline video playback is on in settings.
    var loadStreamable: ((String) async throws -> StreamableVideo)?
    /// A vote or favourite for this resource is waiting for the server.
    var pending = false

    static let none = ResourceActions()
}

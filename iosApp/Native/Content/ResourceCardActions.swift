import SwiftUI

struct ResourceActions {
    var open: (() -> Void)?
    var openAuthor: ((String) -> Void)?
    var openTag: ((String) -> Void)?
    var openURL: ((URL) -> Void)?
    var voteUp: (() -> Void)?
    var voteDown: (() -> Void)?
    var favourite: (() -> Void)?
    var comment: (() -> Void)?
    var menu: (() -> Void)?
    var surveyVote: ((Int) -> Void)?
    var loadTweet: ((String) async throws -> NativeTweetPreview)?
    /// A vote or favourite for this resource is waiting for the server.
    var pending = false

    static let none = ResourceActions()
}

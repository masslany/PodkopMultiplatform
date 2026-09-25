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

    static let none = ResourceActions()
}

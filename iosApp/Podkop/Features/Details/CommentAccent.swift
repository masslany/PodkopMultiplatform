import SwiftUI

/// Which accent line a link comment gets (Android's `resolveLinkCommentAccent`): the viewer's own
/// comments first, then the link's author, then a reply by the author of the comment it answers.
enum CommentAccent: Equatable {
    case currentUser, linkAuthor, parentAuthor

    static func resolve(author: String?, linkAuthor: String?, parentAuthor: String?,
                        currentUser: String?) -> CommentAccent? {
        guard let author, !author.isEmpty else { return nil }
        if let currentUser, !currentUser.isEmpty, author == currentUser { return .currentUser }
        if let linkAuthor, !linkAuthor.isEmpty, author == linkAuthor { return .linkAuthor }
        if let parentAuthor, !parentAuthor.isEmpty, author == parentAuthor { return .parentAuthor }
        return nil
    }

    var color: Color {
        switch self {
        case .currentUser: PodkopTheme.currentUserAccent
        case .linkAuthor: PodkopTheme.linkAuthorAccent
        case .parentAuthor: PodkopTheme.parentAuthorAccent
        }
    }
}

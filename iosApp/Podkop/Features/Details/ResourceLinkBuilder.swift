import Foundation

enum ResourceLinkBuilder {
    static func url(for resource: Resource, root: Resource,
                    parentCommentID: Int? = nil) -> URL? {
        let base: String
        switch resource.kind {
        case .entry:
            base = "https://wykop.pl/wpis/\(resource.sourceID)"
        case .entryComment:
            base = "https://wykop.pl/wpis/\(root.sourceID)/#\(resource.sourceID)"
        case .link:
            guard !root.slug.isEmpty else { return nil }
            base = "https://wykop.pl/link/\(root.sourceID)/\(root.slug)"
        case .linkComment:
            guard !root.slug.isEmpty else { return nil }
            let prefix = "https://wykop.pl/link/\(root.sourceID)/\(root.slug)/komentarz/"
            if let parentCommentID, parentCommentID != resource.sourceID {
                base = "\(prefix)\(parentCommentID)#\(resource.sourceID)"
            } else {
                base = "\(prefix)\(resource.sourceID)"
            }
        case .unknown:
            return nil
        }
        return URL(string: base)
    }
}

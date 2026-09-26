import Foundation

enum ResourceLinkBuilder {
    static func url(for resource: Resource, root: Resource,
                    parentCommentID: Int? = nil) -> URL? {
        let base: String
        switch resource.kind {
        case .entry:
            base = "https://wykop.pl/wpis/\(resource.sourceID)"
        case .entryComment:
            base = "https://wykop.pl/wpis/\(root.sourceID)/komentarz/\(resource.sourceID)"
        case .link:
            base = linkBase(root)
        case .linkComment:
            // Wykop redirects a slugless link, but rejects a slugless comment route.
            guard !root.slug.isEmpty else { return URL(string: linkBase(root)) }
            let prefix = "\(linkBase(root))/komentarz/"
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
    private static func linkBase(_ root: Resource) -> String {
        let base = "https://wykop.pl/link/\(root.sourceID)"
        return root.slug.isEmpty ? base : "\(base)/\(root.slug)"
    }
}

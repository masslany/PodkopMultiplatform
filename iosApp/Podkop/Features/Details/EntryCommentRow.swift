import SwiftUI

/// An entry comment in the entry detail (Android's `EntryDetailsScreenContent` rows): no card, a
/// 2 pt line down the left edge that runs on through the whole list, green on the viewer's own
/// comments, and an inset divider below every comment but the last.
struct EntryCommentRow: View {
    let comment: Resource
    let actions: ResourceActions
    let isOwn: Bool
    let isLast: Bool
    var autoplayGifs = false
    var isForeground = true

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            ResourceCard(resource: comment, actions: actions, style: .embedded,
                         autoplayGifs: autoplayGifs, isForeground: isForeground)
                .padding(.leading, 18)
                .padding(.top, 16)
            if !isLast {
                Divider().overlay(PodkopTheme.separator)
                    .padding(.leading, 18)
                    .padding(.top, 16)
            } else {
                Color.clear.frame(height: 16)
            }
        }
        .background(alignment: .leading) {
            Rectangle()
                .fill(isOwn ? PodkopTheme.currentUserAccent : PodkopTheme.separator)
                .frame(width: 2)
                .accessibilityHidden(true)
        }
        .padding(.horizontal, 16)
    }
}

/// A comment in a threaded entry, joined to its parent by a reply connector
/// (Android's `EntryThreadRow`).
struct EntryThreadCommentRow: View {
    let comment: Resource
    let actions: ResourceActions
    let depth: Int
    let connectors: ThreadConnectors
    /// A divider closes the thread when the next row starts a new one.
    let endsThread: Bool
    let isLast: Bool
    var autoplayGifs = false
    var isForeground = true
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    var body: some View {
        // ResourceCard's comment avatar: 32 pt, 48 pt at accessibility sizes, 16 pt below the row top.
        let avatar: CGFloat = dynamicTypeSize.isAccessibilitySize ? 48 : 32
        EntryThreadRowFrame(depth: depth, connectors: connectors, avatarSize: avatar,
                            elbowY: 16 + avatar / 2, endsThread: endsThread, isLast: isLast) {
            ResourceCard(resource: comment, actions: actions, style: .embedded,
                         autoplayGifs: autoplayGifs, isForeground: isForeground,
                         indentsBodyUnderAuthor: true)
                .padding(.top, 16)
        }
    }
}

/// Android's "Pokaż więcej odpowiedzi (N)" row under a thread branch with unloaded replies.
struct EntryThreadMoreRow: View {
    let title: String
    let loading: Bool
    let depth: Int
    let connectors: ThreadConnectors
    let endsThread: Bool
    let isLast: Bool
    let action: () -> Void
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    var body: some View {
        EntryThreadRowFrame(depth: depth, connectors: connectors,
                            avatarSize: dynamicTypeSize.isAccessibilitySize ? 48 : 32,
                            elbowY: 31, endsThread: endsThread, isLast: isLast) {
            ThreadMoreButton(title: title, loading: loading, action: action)
                .fixedSize()
                .padding(.top, 12)
        }
    }
}

/// Lays a thread row out one indent step per level and draws its connectors behind it.
private struct EntryThreadRowFrame<Content: View>: View {
    let depth: Int
    let connectors: ThreadConnectors
    let avatarSize: CGFloat
    let elbowY: CGFloat
    let endsThread: Bool
    let isLast: Bool
    @ViewBuilder let content: Content

    var body: some View {
        let geometry = ThreadGeometry(avatarSize: avatarSize)
        VStack(alignment: .leading, spacing: 0) {
            content
            if endsThread {
                Divider().overlay(PodkopTheme.separator).padding(.top, 16)
            } else if isLast {
                Color.clear.frame(height: 16)
            }
        }
        .padding(.leading, geometry.contentStart(depth))
        .padding(.trailing, 16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background {
            ThreadConnectorShape(geometry: geometry, depth: depth, connectors: connectors, elbowY: elbowY)
                .stroke(PodkopTheme.separator, lineWidth: 1.5)
                .accessibilityHidden(true)
        }
    }
}

private struct ThreadGeometry {
    let avatarSize: CGFloat
    /// One step puts a reply's avatar just right of its parent's line, with room for the elbow.
    var indent: CGFloat { avatarSize / 2 + 10 }

    func contentStart(_ depth: Int) -> CGFloat { 16 + CGFloat(depth) * indent }
    func column(_ level: Int) -> CGFloat { contentStart(level) + avatarSize / 2 }
}

/// Lines under ancestor avatars that run on to later siblings, a rounded elbow from the parent's
/// line into this row, and a line down from this row's own avatar when it has replies.
private struct ThreadConnectorShape: Shape {
    let geometry: ThreadGeometry
    let depth: Int
    let connectors: ThreadConnectors
    let elbowY: CGFloat

    func path(in rect: CGRect) -> Path {
        var path = Path()
        func vertical(_ x: CGFloat, from y: CGFloat) {
            path.move(to: CGPoint(x: x, y: y))
            path.addLine(to: CGPoint(x: x, y: rect.maxY))
        }
        for (level, runsOn) in connectors.ancestorLines.enumerated() where runsOn {
            vertical(geometry.column(level), from: 0)
        }
        if depth > 0 {
            let x = geometry.column(depth - 1)
            let radius: CGFloat = 8
            path.move(to: CGPoint(x: x, y: 0))
            path.addLine(to: CGPoint(x: x, y: elbowY - radius))
            path.addQuadCurve(to: CGPoint(x: x + radius, y: elbowY), control: CGPoint(x: x, y: elbowY))
            path.addLine(to: CGPoint(x: geometry.contentStart(depth) - 2, y: elbowY))
            if connectors.continuesBelow { vertical(x, from: elbowY - radius) }
        }
        if connectors.hasReplies {
            // Below the avatar and its 2 pt gender bar.
            vertical(geometry.column(depth), from: 16 + geometry.avatarSize + 6)
        }
        return path
    }
}

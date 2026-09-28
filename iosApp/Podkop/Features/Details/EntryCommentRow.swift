import SwiftUI

/// An entry comment in the entry detail (Android's `EntryDetailsScreenContent` rows): no card, a
/// 2 pt line down the left edge that runs on through the whole list, green on the viewer's own
/// comments, and an inset divider below every comment but the last. Threaded comments are indented
/// by `depth`, with a guide line per ancestor level.
struct EntryCommentRow: View {
    let comment: Resource
    let actions: ResourceActions
    let isOwn: Bool
    let isLast: Bool
    var depth = 0
    /// Overrides the line colour, e.g. for the entry author's comments in a thread.
    var accent: Color?
    var autoplayGifs = false
    var isForeground = true

    var body: some View {
        EntryThreadIndent(depth: depth, line: isOwn ? PodkopTheme.currentUserAccent : accent ?? PodkopTheme.separator) {
            VStack(alignment: .leading, spacing: 0) {
                ResourceCard(resource: comment, actions: actions, style: .embedded,
                             autoplayGifs: autoplayGifs, isForeground: isForeground)
                    .padding(.leading, 18)
                    .padding(.top, 16)
                EntryCommentRowEnd(isLast: isLast)
            }
        }
    }
}

/// Android's "Pokaż więcej odpowiedzi (N)" row under a thread branch with unloaded replies.
struct EntryThreadMoreRow: View {
    let title: String
    let loading: Bool
    let depth: Int
    let isLast: Bool
    let action: () -> Void

    var body: some View {
        EntryThreadIndent(depth: depth, line: PodkopTheme.separator) {
            VStack(alignment: .leading, spacing: 0) {
                ThreadMoreButton(title: title, loading: loading, action: action)
                    .fixedSize()
                    .padding(.leading, 18)
                    .padding(.top, 12)
                EntryCommentRowEnd(isLast: isLast)
            }
        }
    }
}

private struct EntryCommentRowEnd: View {
    let isLast: Bool

    var body: some View {
        if !isLast {
            Divider().overlay(PodkopTheme.separator)
                .padding(.leading, 18)
                .padding(.top, 16)
        } else {
            Color.clear.frame(height: 16)
        }
    }
}

/// The row's own 2 pt line, pushed right by `depth` levels that each keep a guide line, so every
/// branch reads as one continuous column (Android's `EntryThreadRow`).
private struct EntryThreadIndent<Content: View>: View {
    static var levelWidth: CGFloat { 12 }

    let depth: Int
    let line: Color
    @ViewBuilder let content: Content

    var body: some View {
        content
            .background(alignment: .leading) {
                Rectangle().fill(line).frame(width: 2).accessibilityHidden(true)
            }
            .padding(.leading, CGFloat(depth) * Self.levelWidth)
            .background(alignment: .leading) {
                HStack(spacing: Self.levelWidth - 2) {
                    ForEach(0..<depth, id: \.self) { _ in
                        Rectangle().fill(PodkopTheme.separator).frame(width: 2)
                    }
                }
                .accessibilityHidden(true)
            }
            .padding(.horizontal, 16)
    }
}

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

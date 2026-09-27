import SwiftUI

/// One card holding a resource and the comments under it, separated by hairlines and indented,
/// with an optional outlined button below (Android's entry card with inline comments and the link
/// detail comment card with its replies). Tapping empty space runs `open`.
struct ResourceThreadCard<Footer: View>: View {
    let root: Resource
    let rootActions: ResourceActions
    let children: [Resource]
    let childActions: (Resource) -> ResourceActions
    var autoplayGifs = false
    var isForeground = true
    var open: (() -> Void)?
    /// An optional accent line beside a comment, given the comment and the one it replies to
    /// (Android's `LinkCommentAccentLayout`).
    var accent: (Resource, Resource?) -> Color? = { _, _ in nil }
    @ViewBuilder var footer: () -> Footer

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            card(root, actions: rootActions, identified: false)
                .background(alignment: .leading) { accentLine(accent(root, nil)) }
            ForEach(children) { child in
                Divider().overlay(PodkopTheme.separator)
                card(child, actions: childActions(child))
                    .background(alignment: .leading) { accentLine(accent(child, root)) }
                    .padding(.leading, 16)
            }
            footer()
        }
        .podkopCard(padding: 14)
        .contentShape(Rectangle())
        .onTapGesture { open?() }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("resource-\(root.id)")
    }

    /// A 3 pt rounded line just left of the content, inset 6 pt at both ends.
    @ViewBuilder private func accentLine(_ color: Color?) -> some View {
        if let color {
            Capsule().fill(color)
                .frame(width: 3)
                .padding(.vertical, 6)
                .offset(x: -8)
                .accessibilityHidden(true)
        }
    }

    private func card(_ resource: Resource, actions: ResourceActions, identified: Bool = true) -> some View {
        ResourceCard(resource: resource, actions: actions, style: .embedded,
                           autoplayGifs: autoplayGifs, isForeground: isForeground, identified: identified)
    }
}

/// Wykop's centered outlined button under comment threads ("Pokaż komentarze (36)").
struct ThreadMoreButton: View {
    let title: String
    var loading = false
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                if loading { ProgressView().controlSize(.small) }
                Text(title).font(.subheadline.weight(.medium))
            }
            .foregroundStyle(.primary)
            .padding(.horizontal, 18).padding(.vertical, 9)
            .overlay(Capsule().strokeBorder(PodkopTheme.separator, lineWidth: 1))
            .contentShape(Capsule())
        }
        .buttonStyle(.plain)
        .disabled(loading)
        .frame(maxWidth: .infinity)
    }
}

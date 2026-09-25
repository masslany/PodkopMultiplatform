import SwiftUI

/// One card holding a resource and the comments under it, separated by hairlines and indented,
/// with an optional outlined button below (Android's entry card with inline comments and the link
/// detail comment card with its replies). Tapping empty space runs `open`.
struct ResourceThreadCard<Footer: View>: View {
    let root: NativeResource
    let rootActions: ResourceActions
    let children: [NativeResource]
    let childActions: (NativeResource) -> ResourceActions
    var autoplayGifs = false
    var isForeground = true
    var open: (() -> Void)?
    @ViewBuilder var footer: () -> Footer

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            card(root, actions: rootActions, identified: false)
            ForEach(children) { child in
                Divider().overlay(WykopTheme.separator)
                card(child, actions: childActions(child))
                    .padding(.leading, 16)
            }
            footer()
        }
        .wykopCard(padding: 14)
        .contentShape(Rectangle())
        .onTapGesture { open?() }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("resource-\(root.id)")
    }

    private func card(_ resource: NativeResource, actions: ResourceActions, identified: Bool = true) -> some View {
        NativeResourceCard(resource: resource, actions: actions, style: .embedded,
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
            .overlay(Capsule().strokeBorder(WykopTheme.separator, lineWidth: 1))
            .contentShape(Capsule())
        }
        .buttonStyle(.plain)
        .disabled(loading)
        .frame(maxWidth: .infinity)
    }
}

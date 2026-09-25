import SwiftUI

/// One entry of the More menu.
struct MoreItem: Identifiable {
    let id: String
    let title: LocalizedStringKey
    let symbol: String
    let tint: Color
    var badge = 0
    let action: () -> Void
}

/// A titled group of More entries on one card, with iOS Settings-style glyph tiles
/// (Android's More sections).
struct MoreMenuSection: View {
    let title: LocalizedStringKey
    let items: [MoreItem]

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.footnote.weight(.semibold))
                .foregroundStyle(.secondary)
                .textCase(.uppercase)
                .padding(.leading, 20)
                .accessibilityAddTraits(.isHeader)
            VStack(spacing: 0) {
                ForEach(Array(items.enumerated()), id: \.element.id) { index, item in
                    Button(action: item.action) { row(item) }
                        .buttonStyle(MoreRowStyle())
                        .accessibilityIdentifier("more-\(item.id)")
                        .accessibilityValue(item.badge > 0 ? String(localized: "Unread: \(item.badge)") : "")
                    if index < items.count - 1 {
                        Divider().overlay(WykopTheme.separator).padding(.leading, 60)
                    }
                }
            }
            .background(WykopTheme.card, in: RoundedRectangle(cornerRadius: 18, style: .continuous))
            .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
            .padding(.horizontal, 16)
        }
    }

    private func row(_ item: MoreItem) -> some View {
        HStack(spacing: 14) {
            Image(systemName: item.symbol)
                .font(.system(size: 15, weight: .semibold))
                .foregroundStyle(.white)
                .frame(width: 30, height: 30)
                .background(item.tint.gradient, in: RoundedRectangle(cornerRadius: 8, style: .continuous))
            Text(item.title)
                .font(.body)
                .foregroundStyle(.primary)
            Spacer(minLength: 8)
            if item.badge > 0 {
                Text(item.badge > 999 ? "999+" : "\(item.badge)")
                    .font(.footnote.weight(.bold).monospacedDigit())
                    .foregroundStyle(.white)
                    .padding(.horizontal, 8).padding(.vertical, 3)
                    .background(WykopTheme.voteNegative, in: Capsule())
            }
            Image(systemName: "chevron.right")
                .font(.footnote.weight(.semibold))
                .foregroundStyle(.tertiary)
        }
        .padding(.horizontal, 16)
        .frame(minHeight: 54)
        .contentShape(Rectangle())
    }
}

/// Highlights the pressed row like a system list cell.
private struct MoreRowStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .background(configuration.isPressed ? WykopTheme.cardInset : .clear)
    }
}

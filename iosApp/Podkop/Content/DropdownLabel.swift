import SwiftUI

/// The neutral filled label for sort and filter menus ("Najlepsze ▾" on Android).
struct DropdownLabel: View {
    let title: String
    var systemImage: String?

    var body: some View {
        HStack(spacing: 6) {
            if let systemImage { Image(systemName: systemImage).font(.footnote) }
            Text(title).font(.subheadline.weight(.semibold))
                .lineLimit(1)
                .minimumScaleFactor(0.8)
            Image(systemName: "chevron.down").font(.caption.weight(.bold)).foregroundStyle(.secondary)
        }
        .foregroundStyle(.primary)
        .padding(.horizontal, 12).padding(.vertical, 8)
        .background(WykopTheme.cardInset, in: RoundedRectangle(cornerRadius: 10, style: .continuous))
        .contentShape(RoundedRectangle(cornerRadius: 10))
    }
}

import SwiftUI

/// One achievement as Android shows it: an 80×40 tile in the badge's color with its icon, and on
/// tap a popover with the name, description, level/progress and the date it was earned.
struct AchievementBadge: View {
    let badge: Badge
    @Environment(\.colorScheme) private var colorScheme
    @Environment(\.mediaLoader) private var loader
    @State private var icon: UIImage?
    @State private var showsDetails = false

    var body: some View {
        Button { showsDetails = true } label: {
            ZStack {
                RoundedRectangle(cornerRadius: 12, style: .continuous).fill(tint)
                if let icon {
                    Image(uiImage: icon)
                        .resizable()
                        .scaledToFit()
                        .padding(8)
                } else if badge.iconURL.isEmpty {
                    Text(badge.label)
                        .font(.caption2.weight(.semibold))
                        .lineLimit(2)
                        .minimumScaleFactor(0.8)
                        .multilineTextAlignment(.center)
                        .foregroundStyle(.white)
                        .padding(6)
                }
            }
            .frame(width: 80, height: 40)
        }
        .buttonStyle(.plain)
        .accessibilityLabel(badge.label)
        .accessibilityHint(badge.description)
        .popover(isPresented: $showsDetails, arrowEdge: .bottom) {
            details
                .presentationCompactAdaptation(.popover)
        }
        .task(id: badge.iconURL) { await loadIcon() }
    }

    private var tint: Color {
        let dark = colorScheme == .dark && !badge.colorHexDark.isEmpty
        return Color(hex: dark ? badge.colorHexDark : badge.colorHex)
            ?? Color(hex: badge.colorHexDark) ?? .secondary
    }

    private var details: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(badge.label).font(.headline)
            if !badge.description.isEmpty {
                Text(badge.description).font(.subheadline).foregroundStyle(.secondary)
            }
            HStack(spacing: 10) {
                if let level = badge.level { Text(.profileLevel(level)) }
                if let progress = badge.progress { Text(.profileProgress(progress)) }
            }
            .font(.footnote)
            .foregroundStyle(.secondary)
            if let date = badge.achievedAt.flatMap(Dates.parse) {
                Text(.profileAchieved(date.formatted(.dateTime.day(.twoDigits).month(.twoDigits).year())))
                    .font(.footnote.weight(.medium))
                    .foregroundStyle(.secondary)
            }
        }
        .padding(16)
        .frame(width: 280, alignment: .leading)
        .fixedSize(horizontal: false, vertical: true)
    }

    private func loadIcon() async {
        guard icon == nil, !badge.iconURL.isEmpty, let loader,
              let data = try? await loader.bytes(for: badge.iconURL) else { return }
        icon = await SVGImageRenderer.shared.image(svg: data, key: badge.iconURL, pointSize: 48)
    }
}

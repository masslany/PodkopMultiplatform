import SwiftUI

/// Wykop avatar: a rounded square with the default person glyph as placeholder and, when the
/// gender is known, the colored bar underneath (Android's `Avatar` + `GenderIndicator`).
struct AvatarView: View {
    let url: String?
    let name: String
    var size: CGFloat = 36
    var gender: String?
    var showsGenderBar = true

    var body: some View {
        VStack(spacing: size >= 60 ? 6 : 4) {
            RemoteImage(url: url, maxDimension: Int(size * 3)) { placeholder }
                .frame(width: size, height: size)
                .clipShape(RoundedRectangle(cornerRadius: size * 0.22, style: .continuous))
            if showsGenderBar {
                Capsule()
                    .fill(genderColor(gender) ?? .clear)
                    .frame(width: size, height: size >= 60 ? 4 : 2)
            }
        }
        .accessibilityHidden(true)
    }

    private var placeholder: some View {
        RoundedRectangle(cornerRadius: size * 0.22, style: .continuous)
            .fill(WykopTheme.cardInset)
            .overlay(Image(systemName: "person").font(.system(size: size * 0.5, weight: .light))
                .foregroundStyle(.secondary))
    }
}

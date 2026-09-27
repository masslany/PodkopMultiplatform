import SwiftUI
import UIKit

/// The brand colors, taken from the Android `ColorsPalette`, plus the app's surfaces.
/// Surfaces follow the charcoal dark theme and iOS grouped backgrounds in light mode.
enum PodkopTheme {
    static let background = dynamic(light: 0xF2F2F5, dark: 0x1C1B1D)
    static let card = dynamic(light: 0xFFFFFF, dark: 0x28272B)
    static let cardInset = dynamic(light: 0xF0EFF3, dark: 0x323136)
    static let separator = dynamic(light: 0xDCDBE0, dark: 0x3C3B40)

    static let nameOrange = dynamic(light: 0xFF5100, dark: 0xFE5000)
    static let nameBurgundy = dynamic(light: 0xB00000, dark: 0xD20000)
    static let nameGreen = dynamic(light: 0x00A63D, dark: 0x00A33C)
    static let genderBlue = rgb(0x07B8F4)
    static let genderPink = dynamic(light: 0xFF3BD4, dark: 0xBF48A7)
    static let tagBlue = rgb(0x3D83CC)
    static let hotOrange = rgb(0xEF713F)
    static let adultRed = rgb(0xE86064)
    static let favouriteGold = rgb(0xFFC107)
    static let votePositive = rgb(0x74BD74)
    static let voteNegative = rgb(0xE7625A)
    /// Comment accent lines (Android's `linkAuthor`, `commentAuthor`, `currentUserAuthor`).
    static let linkAuthorAccent = rgb(0x3D83CC)
    static let parentAuthorAccent = dynamic(light: 0x3A3A3A, dark: 0xE5E5E5)
    static let currentUserAccent = nameGreen

    /// The "on" track of switches. The app's neutral tint is white in dark mode, which would
    /// hide the white thumb, so switches use the brand green, like iOS's own switches.
    static let switchOn = nameGreen

    static let cardRadius: CGFloat = 16
    static let smallRadius: CGFloat = 8

    private static func rgb(_ hex: UInt32) -> Color { Color(uiColor: uiColor(hex)) }

    private static func dynamic(light: UInt32, dark: UInt32) -> Color {
        Color(uiColor: UIColor { $0.userInterfaceStyle == .dark ? uiColor(dark) : uiColor(light) })
    }

    private static func uiColor(_ hex: UInt32) -> UIColor {
        UIColor(red: CGFloat((hex >> 16) & 0xFF) / 255, green: CGFloat((hex >> 8) & 0xFF) / 255,
                blue: CGFloat(hex & 0xFF) / 255, alpha: 1)
    }
}

extension View {
    /// A switch-style toggle with a visible "on" state in both light and dark mode.
    func podkopSwitch() -> some View {
        toggleStyle(.switch).tint(PodkopTheme.switchOn)
    }

    /// The standard card surface.
    func podkopCard(padding: CGFloat = 16) -> some View {
        self.padding(padding)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(PodkopTheme.card, in: RoundedRectangle(cornerRadius: PodkopTheme.cardRadius))
    }
}

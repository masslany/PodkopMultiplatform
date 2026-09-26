import SwiftUI
import UIKit

/// Wykop brand colors, taken from the Android `ColorsPalette`, plus the app's surfaces.
/// Surfaces follow Wykop's charcoal dark theme and iOS grouped backgrounds in light mode.
enum WykopTheme {
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
    /// The standard Wykop card surface.
    func wykopCard(padding: CGFloat = 16) -> some View {
        self.padding(padding)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(WykopTheme.card, in: RoundedRectangle(cornerRadius: WykopTheme.cardRadius))
    }
}

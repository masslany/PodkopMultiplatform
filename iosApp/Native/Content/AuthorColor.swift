import SwiftUI

/// Wykop name colors by account type (orange, burgundy, green); plain accounts use the text color.
func authorColor(_ name: String?) -> Color {
    switch name {
    case "orange": WykopTheme.nameOrange
    case "burgundy": WykopTheme.nameBurgundy
    case "green": WykopTheme.nameGreen
    default: .primary
    }
}

/// Wykop's gender bar color under an avatar; nil hides the bar.
func genderColor(_ gender: String?) -> Color? {
    switch gender {
    case "male": WykopTheme.genderBlue
    case "female": WykopTheme.genderPink
    default: nil
    }
}

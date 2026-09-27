import SwiftUI

/// Wykop name colors by account type (orange, burgundy, green); plain accounts use the text color.
func authorColor(_ name: String?) -> Color {
    switch name {
    case "orange": PodkopTheme.nameOrange
    case "burgundy": PodkopTheme.nameBurgundy
    case "green": PodkopTheme.nameGreen
    default: .primary
    }
}

/// Wykop's gender bar color under an avatar; nil hides the bar.
func genderColor(_ gender: String?) -> Color? {
    switch gender {
    case "male": PodkopTheme.genderBlue
    case "female": PodkopTheme.genderPink
    default: nil
    }
}

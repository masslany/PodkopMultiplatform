import SwiftUI

func authorColor(_ name: String?) -> Color {
    switch name {
    case "orange": .orange
    case "burgundy": Color(red: 0.55, green: 0.16, blue: 0.28)
    case "green": .green
    default: .primary
    }
}

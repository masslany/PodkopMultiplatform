import SwiftUI
import Observation
import PodkopShared

/// Mirrors the shared `ThemeOverride` names.
enum ThemeChoice: String, CaseIterable {
    case auto = "AUTO", light = "LIGHT", dark = "DARK"

    var colorScheme: ColorScheme? {
        switch self {
        case .auto: nil
        case .light: .light
        case .dark: .dark
        }
    }
}

struct NativeLibraryNotice: Identifiable, Equatable {
    let name: String
    let artifact: String
    let licenseName: String?
    let licenseURL: URL?
    let projectURL: URL?
    var id: String { artifact }
}

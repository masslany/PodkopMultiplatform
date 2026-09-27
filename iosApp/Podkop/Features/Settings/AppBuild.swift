import SwiftUI
import Observation
import PodkopShared

enum AppBuild {
    static var version: String {
        let info = Bundle.main.infoDictionary
        let short = info?["CFBundleShortVersionString"] as? String ?? "?"
        let build = info?["CFBundleVersion"] as? String ?? "?"
        return "\(short) (\(build))"
    }

    static var isDebug: Bool {
        #if DEBUG
        true
        #else
        false
        #endif
    }
}

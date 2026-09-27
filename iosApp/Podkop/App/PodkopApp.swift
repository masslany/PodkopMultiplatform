import SwiftUI
import PodkopShared

@main
struct PodkopApp: App {
    @Environment(\.scenePhase) private var scenePhase

    init() {
        // Background refresh handlers must be registered before launch finishes.
        AppDependencies.shared.messageNotifications.register()
    }

    var body: some Scene {
        WindowGroup { RootView(dependencies: .shared) }
            .onChange(of: scenePhase) { _, phase in
                // Leaving the app asks iOS for the next message check.
                if phase == .background { AppDependencies.shared.messageNotifications.schedule() }
            }
    }
}

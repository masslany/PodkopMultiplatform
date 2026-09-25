import SwiftUI
import PodkopShared
#if DEBUG
import UserNotifications
#endif

@main
struct PodkopApp: App {
    #if DEBUG
    init() { UNUserNotificationCenter.current().delegate = DebugNotificationDelegate.shared }
    #endif

    var body: some Scene {
        WindowGroup { RootView(dependencies: .shared) }
    }
}

#if DEBUG
private final class DebugNotificationDelegate: NSObject, UNUserNotificationCenterDelegate {
    static let shared = DebugNotificationDelegate()

    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification) async -> UNNotificationPresentationOptions {
        [.banner, .sound]
    }
}
#endif

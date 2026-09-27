import BackgroundTasks
import Foundation
import Observation
import PodkopShared
import UserNotifications

/// Private-message notifications, like Android's `PrivateMessagesBackgroundNotificationsController`:
/// while enabled and signed in, a background refresh checks the unread message count and posts a
/// notification when it has grown since last seen. iOS decides when the refresh runs (Android
/// polls every 15 minutes); 15 minutes is only the earliest time asked for.
@MainActor @Observable
final class MessageNotifications: NSObject {
    static let taskIdentifier = "pl.masslany.podkop.privateMessages"
    private static let enabledKey = "privateMessageNotificationsEnabled"
    private static let countKey = "privateMessagesLastObservedUnread"
    private static let interval: TimeInterval = 15 * 60

    private(set) var enabled: Bool
    /// Whether the system lets the app post notifications; the Settings footer explains a refusal.
    private(set) var systemAllowed = true
    private let defaults: UserDefaults
    private unowned let dependencies: AppDependencies

    init(dependencies: AppDependencies, defaults: UserDefaults = .standard) {
        self.dependencies = dependencies
        self.defaults = defaults
        enabled = defaults.bool(forKey: Self.enabledKey)
    }

    private var lastObservedCount: Int? {
        get { defaults.object(forKey: Self.countKey) as? Int }
        set { defaults.set(newValue, forKey: Self.countKey) }
    }

    /// Must run before the app finishes launching, so iOS can hand over a background refresh.
    func register() {
        UNUserNotificationCenter.current().delegate = self
        BGTaskScheduler.shared.register(forTaskWithIdentifier: Self.taskIdentifier, using: nil) { task in
            guard let task = task as? BGAppRefreshTask else { return task.setTaskCompleted(success: false) }
            Task { @MainActor in self.handle(task) }
        }
    }

    func refreshSystemPermission() async {
        let status = await UNUserNotificationCenter.current().notificationSettings().authorizationStatus
        systemAllowed = status == .authorized || status == .provisional || status == .notDetermined
    }

    /// The Settings switch: turning it on asks for permission first, as Android does.
    func setEnabled(_ value: Bool) async {
        guard value else {
            enabled = false
            defaults.set(false, forKey: Self.enabledKey)
            lastObservedCount = nil
            BGTaskScheduler.shared.cancel(taskRequestWithIdentifier: Self.taskIdentifier)
            return
        }
        let granted = (try? await UNUserNotificationCenter.current()
            .requestAuthorization(options: [.alert, .sound, .badge])) ?? false
        systemAllowed = granted
        guard granted else {
            dependencies.router.banner = String(localized: .settingsSettingsSnackbarNotificationsPermissionRequired)
            return
        }
        enabled = true
        defaults.set(true, forKey: Self.enabledKey)
        // Seed the count so messages already unread do not notify.
        lastObservedCount = try? await unreadCount()
        schedule()
    }

    /// The app keeps the count current while open, so returning to it never notifies about
    /// messages already seen.
    func observed(unread: Int) {
        guard enabled, dependencies.session.isLoggedIn else { return }
        lastObservedCount = unread
    }

    func loggedOut() {
        lastObservedCount = nil
        BGTaskScheduler.shared.cancel(taskRequestWithIdentifier: Self.taskIdentifier)
    }

    func schedule() {
        guard enabled, dependencies.session.isLoggedIn else { return }
        let request = BGAppRefreshTaskRequest(identifier: Self.taskIdentifier)
        request.earliestBeginDate = Date(timeIntervalSinceNow: Self.interval)
        try? BGTaskScheduler.shared.submit(request)
    }

    private func handle(_ task: BGAppRefreshTask) {
        let work = Task { @MainActor in
            let success = await poll()
            task.setTaskCompleted(success: success)
        }
        task.expirationHandler = { work.cancel() }
    }

    /// Android's `pollAndNotifyIfNeeded`.
    func poll() async -> Bool {
        guard enabled else { return true }
        dependencies.session.startIfNeeded()
        guard await waitForSession(), dependencies.session.isLoggedIn else {
            loggedOut()
            return true
        }
        defer { schedule() }
        guard let current = try? await unreadCount() else { return false }
        if let alert = Self.alert(previous: lastObservedCount, current: current), systemAllowed {
            await post(multiple: alert == .multiple)
        }
        lastObservedCount = current
        return true
    }

    enum Alert: Equatable { case single, multiple }

    /// Android's rule: notify only when the count grew since last seen, never on the first check.
    nonisolated static func alert(previous: Int?, current: Int) -> Alert? {
        guard let previous, current > previous else { return nil }
        return current - previous > 1 ? .multiple : .single
    }

    private func waitForSession() async -> Bool {
        for _ in 0..<50 where dependencies.session.phase == .initializing {
            try? await Task.sleep(for: .milliseconds(100))
        }
        return dependencies.session.phase == .ready
    }

    private func unreadCount() async throws -> Int {
        let status: IOSNotificationStatus = try await dependencies.adapter.call {
            self.dependencies.client.notifications.refreshStatus(completion: $0)
        }
        return Int(status.privateMessagesUnreadCount)
    }

    private func post(multiple: Bool) async {
        let content = UNMutableNotificationContent()
        content.title = String(localized: multiple ? .settingsPmNotificationTitlePlural : .settingsPmNotificationTitle)
        content.body = String(localized: .settingsPmNotificationBody)
        content.sound = .default
        content.userInfo = ["route": "messages"]
        // One notification replaced in place, like Android's fixed notification ID.
        let request = UNNotificationRequest(identifier: "privateMessages", content: content, trigger: nil)
        try? await UNUserNotificationCenter.current().add(request)
    }
}

extension MessageNotifications: UNUserNotificationCenterDelegate {
    nonisolated func userNotificationCenter(_ center: UNUserNotificationCenter,
                                            willPresent notification: UNNotification) async
        -> UNNotificationPresentationOptions {
        [.banner, .sound]
    }

    /// Tapping the notification opens the inbox, like Android's deep link.
    nonisolated func userNotificationCenter(_ center: UNUserNotificationCenter,
                                            didReceive response: UNNotificationResponse) async {
        guard response.notification.request.content.userInfo["route"] as? String == "messages" else { return }
        await MainActor.run { dependencies.ingress.accept(kind: "messages", id: nil) }
    }
}

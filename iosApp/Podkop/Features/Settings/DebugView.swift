import SwiftUI
import UserNotifications

#if DEBUG
struct DebugView: View {
    let tab: AppTab
    let dependencies: AppDependencies
    @State private var entryID = ""
    @State private var linkID = ""
    @State private var entryInvalid = false
    @State private var linkInvalid = false

    var body: some View {
        WykopList {
            Section(.commonOpenEntry) {
                TextField(String(localized: .settingsEntryID), text: $entryID).keyboardType(.numberPad)
                if entryInvalid { Text(.settingsInvalidEntryID).foregroundStyle(.red).font(.caption) }
                Button(.settingsOpen) { open(entryID, invalid: $entryInvalid) { .entry($0) } }
            }
            Section(.commonOpenLink) {
                TextField(String(localized: .settingsLinkID), text: $linkID).keyboardType(.numberPad)
                if linkInvalid { Text(.settingsInvalidLinkID).foregroundStyle(.red).font(.caption) }
                Button(.settingsOpen) { open(linkID, invalid: $linkInvalid) { .link($0) } }
            }
            Section {
                Button(.settingsShowTestNotification) {
                    Task {
                        do {
                            let center = UNUserNotificationCenter.current()
                            guard try await center.requestAuthorization(options: [.alert, .sound]) else {
                                dependencies.router.banner = String(localized: .settingsNotificationsDisabled)
                                return
                            }
                            let content = UNMutableNotificationContent()
                            content.title = String(localized: .settingsTestNotificationTitle)
                            content.body = String(localized: .settingsNotificationsWorking)
                            let request = UNNotificationRequest(identifier: UUID().uuidString,
                                content: content, trigger: UNTimeIntervalNotificationTrigger(timeInterval: 1, repeats: false))
                            try await center.add(request)
                        } catch {
                            dependencies.router.banner = String(localized: .commonCouldNotCompleteAction)
                        }
                    }
                }
                Button(.settingsShowTestBanner) {
                    dependencies.router.banner = String(localized: .commonCouldNotCompleteAction)
                }
            } footer: {
                Text(.settingsTestNotificationsLocal)
            }
        }
        .navigationTitle(.settingsDebugTools)
    }

    private func open(_ raw: String, invalid: Binding<Bool>, route: (Int) -> AppRoute) {
        guard let id = Int(raw.trimmingCharacters(in: .whitespaces)), id > 0 else {
            invalid.wrappedValue = true
            return
        }
        invalid.wrappedValue = false
        dependencies.router.navigate(route(id), in: tab)
    }
}
#endif

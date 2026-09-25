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
        Form {
            Section("Open entry") {
                TextField("Entry ID", text: $entryID).keyboardType(.numberPad)
                if entryInvalid { Text("Invalid entry ID").foregroundStyle(.red).font(.caption) }
                Button("Open") { open(entryID, invalid: $entryInvalid) { .entry($0) } }
            }
            Section("Open link") {
                TextField("Link ID", text: $linkID).keyboardType(.numberPad)
                if linkInvalid { Text("Invalid link ID").foregroundStyle(.red).font(.caption) }
                Button("Open") { open(linkID, invalid: $linkInvalid) { .link($0) } }
            }
            Section {
                Button("Show test notification") {
                    Task {
                        do {
                            let center = UNUserNotificationCenter.current()
                            guard try await center.requestAuthorization(options: [.alert, .sound]) else {
                                dependencies.router.banner = String(localized: "Notifications are disabled in Settings.")
                                return
                            }
                            let content = UNMutableNotificationContent()
                            content.title = String(localized: "Podkop test notification")
                            content.body = String(localized: "Notifications are working on this device.")
                            let request = UNNotificationRequest(identifier: UUID().uuidString,
                                content: content, trigger: UNTimeIntervalNotificationTrigger(timeInterval: 1, repeats: false))
                            try await center.add(request)
                        } catch {
                            dependencies.router.banner = String(localized: "Could not complete this action. Try again.")
                        }
                    }
                }
                Button("Show test banner") {
                    dependencies.router.banner = String(localized: "Could not complete this action. Try again.")
                }
            } footer: {
                Text("Test notifications are local. Background private-message delivery still needs a platform decision (D03).")
            }
        }
        .navigationTitle("Debug tools")
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

import SwiftUI

/// iOS only: App Store guideline 5.1.1(v) needs account deletion to start in the app. Accounts belong to the
/// website, which deletes them only after a password and an emailed confirmation, so this opens its page.
struct AccountDeletionView: View {
    static let url = URL(string: "https://wykop.pl/ustawienia/dane-osobowe/prawo-do-usuniecia-konta")!

    let signOut: () -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var website = false

    var body: some View {
        NavigationStack {
            PodkopList {
                Section { Text(.settingsDeleteAccountIntro) }
                Section {
                    Text(.settingsDeleteAccountSteps)
                    Button(.settingsDeleteAccountOpen) { website = true }
                        .accessibilityIdentifier("openAccountDeletion")
                } footer: {
                    Text(.settingsDeleteAccountNotice)
                }
                // Signing in while the website deletes the account cancels the deletion.
                Section {
                    Text(.settingsDeleteAccountSignOut)
                    Button(.settingsSignOut, role: .destructive) {
                        dismiss()
                        signOut()
                    }
                    .accessibilityIdentifier("accountDeletionSignOut")
                }
            }
            .navigationTitle(.settingsDeleteAccount)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button(.detailsDone) { dismiss() }
                }
            }
            .sheet(isPresented: $website) { SafariView(url: Self.url).ignoresSafeArea() }
        }
        .accessibilityIdentifier("accountDeletion")
    }
}

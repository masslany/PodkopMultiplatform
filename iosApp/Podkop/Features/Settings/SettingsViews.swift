import SwiftUI

struct SettingsView: View {
    @State private var model: SettingsModel
    @State private var confirmLogout = false
    let tab: AppTab
    let dependencies: AppDependencies
    private var session: SessionModel { dependencies.session }

    init(tab: AppTab, dependencies: AppDependencies) {
        _model = State(initialValue: SettingsModel(service: dependencies.settingsService))
        self.tab = tab
        self.dependencies = dependencies
    }

    var body: some View {
        Form {
            Section("Theme") {
                Picker("Theme", selection: Binding(get: { session.theme }, set: { model.setTheme($0) })) {
                    Text("Auto").tag(ThemeChoice.auto)
                    Text("Light").tag(ThemeChoice.light)
                    Text("Dark").tag(ThemeChoice.dark)
                }
                .pickerStyle(.segmented)
                .accessibilityIdentifier("settingsTheme")
            }
            Section("Media") {
                Toggle("Autoplay GIFs", isOn: Binding(get: { session.autoplayGifs }, set: { model.setAutoplay($0) }))
                    .accessibilityIdentifier("settingsAutoplay")
            }
            Section("Data") {
                Button("Clear cache") { model.clearCache() }
                    .accessibilityIdentifier("settingsClearCache")
                Button("Copy diagnostics") { model.copyDiagnostics(session: session) }
            }
            if session.isLoggedIn {
                Section("Account") {
                    Button("Manage blacklists") { dependencies.router.navigate(.blacklists, in: tab) }
                    Button("Sign out", role: .destructive) { confirmLogout = true }
                        .accessibilityIdentifier("settingsSignOut")
                }
            }
            #if DEBUG
            Section("Debug") {
                Button("Debug tools") { dependencies.router.navigate(.debug, in: tab) }
            }
            #endif
            Section {
                Button("About") { dependencies.router.navigate(.about, in: tab) }
            } footer: {
                Text("App version: \(AppBuild.version)")
            }
        }
        .navigationTitle("Settings")
        .alert("Are you sure you want to sign out?", isPresented: $confirmLogout) {
            Button("Sign out", role: .destructive) { Task { await session.logout() } }
            Button("Cancel", role: .cancel) {}
        }
        // Android confirms these with a snackbar; the app banner is the iOS equivalent.
        .onChange(of: model.confirmation) { _, message in
            guard let message else { return }
            dependencies.router.banner = message
            model.dismissMessages()
        }
        .onChange(of: model.failed) { _, failed in
            guard failed else { return }
            dependencies.router.banner = String(localized: "Could not complete this action. Try again.")
            model.dismissMessages()
        }
    }
}

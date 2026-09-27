import SwiftUI

struct SettingsView: View {
    @State private var model: SettingsModel
    @State private var confirmLogout = false
    let tab: AppTab
    let dependencies: AppDependencies
    private var session: SessionModel { dependencies.session }

    init(tab: AppTab, dependencies: AppDependencies) {
        _model = State(initialValue: SettingsModel(service: dependencies.settingsService, state: dependencies.session))
        self.tab = tab
        self.dependencies = dependencies
    }

    var body: some View {
        PodkopList {
            Section(.settingsTheme) {
                Picker(.settingsTheme, selection: Binding(get: { session.theme }, set: { model.setTheme($0) })) {
                    Text(.settingsAuto).tag(ThemeChoice.auto)
                    Text(.settingsLight).tag(ThemeChoice.light)
                    Text(.settingsDark).tag(ThemeChoice.dark)
                }
                .pickerStyle(.segmented)
                .accessibilityIdentifier("settingsTheme")
            }
            Section(.settingsMedia) {
                Toggle(.settingsAutoplayGIFs, isOn: Binding(get: { session.autoplayGifs }, set: { model.setAutoplay($0) }))
                    .podkopSwitch()
                    .accessibilityIdentifier("settingsAutoplay")
            }
            if session.isLoggedIn {
                // Android's private-message notifications switch, with its explanation below.
                Section {
                    Toggle(.settingsSettingsBodyPmNotificationsToggle, isOn: Binding(
                        get: { dependencies.messageNotifications.enabled },
                        set: { value in Task { await dependencies.messageNotifications.setEnabled(value) } }
                    ))
                    .podkopSwitch()
                    .accessibilityIdentifier("settingsMessageNotifications")
                } header: {
                    Text(.settingsSettingsHeadlineNotifications)
                } footer: {
                    Text(dependencies.messageNotifications.systemAllowed
                         ? .settingsSettingsBodyPmNotificationsEnabled
                         : .settingsSettingsBodyPmNotificationsDisabled)
                }
            }
            Section(.settingsData) {
                Button(.settingsClearCache) { model.clearCache() }
                    .accessibilityIdentifier("settingsClearCache")
                Button(.settingsCopyDiagnostics) { model.copyDiagnostics(session: session) }
            }
            if session.isLoggedIn {
                Section(.settingsAccount) {
                    Button(.settingsManageBlacklists) { dependencies.router.navigate(.blacklists, in: tab) }
                    Button(.settingsSignOut, role: .destructive) { confirmLogout = true }
                        .accessibilityIdentifier("settingsSignOut")
                }
            }
            #if DEBUG
            Section(.settingsDebug) {
                Button(.settingsDebugTools) { dependencies.router.navigate(.debug, in: tab) }
            }
            #endif
            Section {
                NavigationLink { PrivacyPolicyView() } label: { Text(.privacyTitle) }
                Button(.commonAbout) { dependencies.router.navigate(.about, in: tab) }
            } footer: {
                Text(.settingsAppVersion(AppBuild.version))
            }
        }
        .navigationTitle(.commonSettings)
        .task { await dependencies.messageNotifications.refreshSystemPermission() }
        .alert(.settingsSureWantSignOut, isPresented: $confirmLogout) {
            Button(.settingsSignOut, role: .destructive) { Task { await session.logout() } }
            Button(.commonCancel, role: .cancel) {}
        }
        // Android confirms these with a snackbar; the app banner is the iOS equivalent.
        .onChange(of: model.confirmation) { _, message in
            guard let message else { return }
            dependencies.router.banner = message
            model.dismissMessages()
        }
        .onChange(of: model.failed) { _, failed in
            guard failed else { return }
            dependencies.router.banner = String(localized: .commonCouldNotCompleteAction)
            model.dismissMessages()
        }
    }
}

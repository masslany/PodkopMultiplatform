import SwiftUI

struct NativeSettingsView: View {
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
        // Android confirms these with a snackbar; the app banner is the native equivalent.
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

struct NativeAboutView: View {
    let dependencies: AppDependencies
    @State private var libraries: [NativeLibraryNotice] = []

    var body: some View {
        List {
            Section {
                HStack(spacing: 14) {
                    Image("SplashIcon").resizable().frame(width: 56, height: 56)
                        .accessibilityHidden(true)
                    VStack(alignment: .leading) {
                        Text("Podkop").font(.title2.bold())
                        Text("App version: \(AppBuild.version)").foregroundStyle(.secondary)
                    }
                }
            }
            Section("Open source libraries") {
                if libraries.isEmpty {
                    Text("No libraries to show.").foregroundStyle(.secondary)
                }
                ForEach(libraries) { library in
                    VStack(alignment: .leading, spacing: 4) {
                        Text(library.name).font(.body.bold())
                        Text(library.artifact).font(.caption.monospaced()).foregroundStyle(.secondary)
                        Text(library.licenseName ?? String(localized: "The license was not found in the package metadata."))
                            .font(.caption)
                        HStack(spacing: 16) {
                            if let url = library.licenseURL { Link("Open license", destination: url) }
                            if let url = library.projectURL { Link("Open project page", destination: url) }
                        }
                        .font(.caption)
                        .buttonStyle(.borderless)
                    }
                    .padding(.vertical, 2)
                }
            }
        }
        .navigationTitle("About")
        .task { libraries = dependencies.settingsService.libraries() }
    }
}

#if DEBUG
struct NativeDebugView: View {
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
                Button("Show test banner") {
                    dependencies.router.banner = String(localized: "Could not complete this action. Try again.")
                }
            } footer: {
                Text("Android's test private-message notification has no iOS counterpart until background delivery is decided (D03).")
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

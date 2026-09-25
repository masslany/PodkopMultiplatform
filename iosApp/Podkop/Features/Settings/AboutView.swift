import SwiftUI

struct AboutView: View {
    let dependencies: AppDependencies
    @State private var libraries: [LibraryNotice] = []

    var body: some View {
        List {
            Section {
                HStack(spacing: 14) {
                    Image("SplashIcon").resizable().frame(width: 56, height: 56)
                        .accessibilityHidden(true)
                    VStack(alignment: .leading) {
                        Text(verbatim: "Podkop").font(.title2.bold())
                        Text(.settingsAppVersion(AppBuild.version)).foregroundStyle(.secondary)
                    }
                }
            }
            Section(.settingsOpenSourceLibraries) {
                if libraries.isEmpty {
                    Text(.settingsNoLibrariesShow).foregroundStyle(.secondary)
                }
                ForEach(libraries) { library in
                    VStack(alignment: .leading, spacing: 4) {
                        Text(library.name).font(.body.bold())
                        Text(library.artifact).font(.caption.monospaced()).foregroundStyle(.secondary)
                        Text(library.licenseName ?? String(localized: .settingsLicenseWasNotFound))
                            .font(.caption)
                        HStack(spacing: 16) {
                            if let url = library.licenseURL { Link(.settingsOpenLicense, destination: url) }
                            if let url = library.projectURL { Link(.settingsOpenProjectPage, destination: url) }
                        }
                        .font(.caption)
                        .buttonStyle(.borderless)
                    }
                    .padding(.vertical, 2)
                }
            }
        }
        .navigationTitle(.commonAbout)
        .task { libraries = dependencies.settingsService.libraries() }
    }
}

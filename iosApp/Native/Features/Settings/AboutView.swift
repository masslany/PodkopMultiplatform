import SwiftUI

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

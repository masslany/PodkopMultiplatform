import SwiftUI

struct ScreenshotPreview: View {
    let resource: Resource
    let parent: Resource?
    @Environment(\.dismiss) private var dismiss
    @Environment(\.mediaLoader) private var loader
    @State private var includeParent = true
    @State private var image: UIImage?
    @State private var photoBytes: [String: Data] = [:]
    @State private var message: String?

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    if parent != nil {
                        Toggle(.detailsIncludeParent, isOn: $includeParent)
                    }
                    if let image {
                        Image(uiImage: image).resizable().scaledToFit()
                            .accessibilityLabel(.detailsScreenshotPreview)
                        if let data = image.pngData() {
                            ImageExportControls(data: data) { message = $0 }
                        }
                        if let message {
                            Text(message).font(.footnote).foregroundStyle(.secondary)
                        }
                    } else {
                        ContentUnavailableView {
                            Label(.detailsCouldNotCreateScreenshot, systemImage: "photo")
                        } actions: {
                            Button(.commonRetry) { render() }
                        }
                    }
                }
                .padding()
            }
            .navigationTitle(.detailsScreenshotPreview)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) { Button(.detailsDone) { dismiss() } }
            }
        }
        .task { await loadPhotos(); render() }
        .onChange(of: includeParent) { _, _ in render() }
    }

    /// Fetches the photos shown in the screenshot so the rendered image includes them.
    private func loadPhotos() async {
        guard let loader else { return }
        for url in [resource.photo?.url, parent?.photo?.url].compactMap({ $0 }) where photoBytes[url] == nil {
            photoBytes[url] = try? await loader.bytes(for: url)
        }
    }

    private func render() {
        image = ScreenshotSurface.render(resource: resource, parent: includeParent ? parent : nil,
                                               photoBytes: photoBytes)
    }
}

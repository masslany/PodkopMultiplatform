import SwiftUI

struct ScreenshotPreview: View {
    let resource: Resource
    let parent: Resource?
    @Environment(\.colorScheme) private var colorScheme
    @Environment(\.dismiss) private var dismiss
    @Environment(\.mediaLoader) private var loader
    @State private var includeParent = true
    @State private var image: UIImage?
    @State private var photoBytes: [String: Data] = [:]
    @State private var message: String?
    @State private var loading = true

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    if parent != nil {
                        Toggle(.detailsIncludeParent, isOn: $includeParent).wykopSwitch()
                    }
                    if loading {
                        ProgressView(.commonLoading).frame(maxWidth: .infinity, minHeight: 180)
                    } else if let image {
                        Image(uiImage: image).resizable().scaledToFit()
                            .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
                            .overlay(RoundedRectangle(cornerRadius: 22).strokeBorder(WykopTheme.separator))
                            .accessibilityLabel(.detailsScreenshotPreview)
                        if let data = image.pngData() {
                            ImageExportControls(data: data, style: .screenshot) { message = $0 }
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
            .background(WykopTheme.background)
            .navigationTitle(.detailsScreenshotPreview)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) { Button(.detailsDone) { dismiss() } }
            }
        }
        .task { await loadPhotos(); render(); loading = false }
        .onChange(of: colorScheme) { _, _ in render() }
        .onChange(of: includeParent) { _, _ in render() }
    }

    /// Fetches the photos shown in the screenshot so the rendered image includes them.
    private func loadPhotos() async {
        guard let loader else { return }
        let resources = [resource, parent].compactMap { $0 }
        let urls = Set(resources.flatMap { [$0.photo?.url, $0.author?.avatarURL, $0.embed?.thumbnailURL] }
            .compactMap { $0 }.filter { !$0.isEmpty })
        for url in urls where photoBytes[url] == nil {
            photoBytes[url] = try? await loader.bytes(for: url)
        }
    }

    private func render() {
        image = ScreenshotSurface.render(resource: resource, parent: includeParent ? parent : nil,
                                               photoBytes: photoBytes, colorScheme: colorScheme)
    }
}

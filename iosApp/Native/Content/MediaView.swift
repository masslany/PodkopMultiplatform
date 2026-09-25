import SwiftUI
import UIKit

struct NativeMediaView: View {
    let photo: NativePhoto
    let bytes: Data?
    let autoplay: Bool
    let foreground: Bool
    @State private var playbackOverride: Bool?
    @State private var visible = true
    @State private var loadedBytes: Data?
    @State private var loadFailed = false
    @State private var viewerBytes: Data?
    @Environment(\.scenePhase) private var scenePhase
    @Environment(\.mediaLoader) private var loader

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            if let bytes = bytes ?? loadedBytes {
                NativeDecodedImage(bytes: bytes, cacheKey: photo.url,
                                   maxDimension: 1200,
                                   animated: visible && foreground && scenePhase == .active
                                       && (playbackOverride ?? autoplay))
                    .frame(maxWidth: .infinity)
                    .frame(height: displayHeight)
                    .background(WykopTheme.cardInset, in: RoundedRectangle(cornerRadius: WykopTheme.smallRadius, style: .continuous))
                    .contentShape(Rectangle())
                    .onTapGesture { viewerBytes = bytes }
                    .accessibilityAddTraits(.isButton)
                    .accessibilityHint("Opens the image")
                    .accessibilityIdentifier("mediaImage")
                if photo.isAnimated {
                    Button((playbackOverride ?? autoplay) ? "Pause animation" : "Play animation") {
                        playbackOverride = !(playbackOverride ?? autoplay)
                    }
                }
            } else if loader == nil || loadFailed {
                Label("Image unavailable", systemImage: "photo")
                    .frame(maxWidth: .infinity, minHeight: 120)
                    .background(WykopTheme.cardInset, in: RoundedRectangle(cornerRadius: WykopTheme.smallRadius, style: .continuous))
            } else {
                ProgressView()
                    .frame(maxWidth: .infinity)
                    .frame(height: displayHeight)
                    .background(WykopTheme.cardInset, in: RoundedRectangle(cornerRadius: WykopTheme.smallRadius, style: .continuous))
            }
        }
        .onAppear { visible = true }
        .onDisappear { visible = false }
        .fullScreenCover(isPresented: Binding(get: { viewerBytes != nil }, set: { if !$0 { viewerBytes = nil } })) {
            if let viewerBytes { NativeImageViewerScreen(bytes: viewerBytes, key: photo.url) }
        }
        .task(id: photo.url) {
            guard bytes == nil, loadedBytes == nil, let loader else { return }
            do { loadedBytes = try await loader.bytes(for: photo.url) }
            catch is CancellationError { return }
            catch { loadFailed = true }
        }
    }

    private var displayHeight: CGFloat {
        guard photo.width > 0, photo.height > 0 else { return 200 }
        return min(320, max(120, 320 * CGFloat(photo.height) / CGFloat(photo.width)))
    }
}

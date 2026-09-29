import SwiftUI
import AVKit
import PodkopShared

struct StreamableVideo: Hashable {
    let url: URL
    /// Width over height; nil when Streamable did not report dimensions.
    let aspectRatio: CGFloat?

    init(url: URL, aspectRatio: CGFloat?) {
        self.url = url
        self.aspectRatio = aspectRatio
    }

    init?(_ value: IOSStreamableVideo) {
        guard let url = URL(string: value.mp4Url) else { return nil }
        self.url = url
        aspectRatio = value.aspectRatio.map { CGFloat($0.floatValue) }
    }
}

/// Streamable embed played inside the post (Android's `StreamableEmbedContent`). The MP4 is
/// resolved on tap because Streamable signs it with a short expiry; the source link stays
/// available in every state as the escape hatch.
struct StreamableEmbed: View {
    private enum Phase: Equatable {
        case idle, loading, failed
        case ready(StreamableVideo)
    }

    let embed: Embed
    var thumbnailBytes: Data?
    let load: (String) async throws -> StreamableVideo
    var open: ((URL) -> Void)?
    @State private var phase = Phase.idle

    private var sourceURL: URL? { URL(string: embed.url) }
    private var sourceLabel: String {
        sourceURL?.host()?.replacingOccurrences(of: "www.", with: "") ?? embed.type.lowercased()
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            switch phase {
            case .ready(let video):
                InlineVideoPlayer(url: video.url) { phase = .failed }
                    .aspectRatio((video.aspectRatio ?? 16 / 9).clampedVideoAspectRatio, contentMode: .fit)
                    .background(.black)
                    .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
            default:
                Button(action: play) { preview }
                    .buttonStyle(.plain)
                    .disabled(phase == .loading)
                    .accessibilityLabel(Text(.contentEmbedVideoPlay))
                    .accessibilityIdentifier("streamablePlay")
            }
            if let sourceURL, let open {
                Button { open(sourceURL) } label: {
                    Label(String(localized: .contentEmbedVideoOpenSource(sourceLabel)),
                          systemImage: "arrow.up.right.square")
                }
                .font(.caption)
                .accessibilityIdentifier("streamableOpenSource")
            }
        }
        .onChange(of: embed.url) { phase = .idle }
    }

    private func play() {
        phase = .loading
        let url = embed.url
        Task {
            do {
                let video = try await load(url)
                if embed.url == url { phase = .ready(video) }
            } catch {
                if embed.url == url { phase = .failed }
            }
        }
    }

    private var preview: some View {
        // The fixed frame decides the size; a filled thumbnail would otherwise grow the card.
        Color.clear
            .aspectRatio(16 / 9, contentMode: .fit)
            .frame(maxWidth: .infinity)
            .overlay { thumbnail }
            .overlay { Color.black.opacity(0.15) }
            .overlay { badge }
            .overlay(alignment: .bottomLeading) { sourceBadge }
            .background(PodkopTheme.cardInset)
            .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
            .contentShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
    }

    private var badge: some View {
        VStack(spacing: 8) {
            ZStack {
                Circle().fill(.ultraThinMaterial)
                if phase == .loading {
                    ProgressView()
                } else {
                    Image(systemName: "play.fill").font(.title3)
                }
            }
            .frame(width: 50, height: 50)
            if phase == .failed {
                Text(.contentEmbedVideoError)
                    .font(.caption.weight(.medium))
                    .foregroundStyle(.white)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 3)
                    .background(.black.opacity(0.5), in: RoundedRectangle(cornerRadius: 4))
            }
        }
    }

    private var sourceBadge: some View {
        Text(verbatim: sourceLabel)
            .font(.caption2)
            .foregroundStyle(.white)
            .lineLimit(1)
            .padding(.horizontal, 8)
            .padding(.vertical, 3)
            .background(.black.opacity(0.3), in: RoundedRectangle(cornerRadius: 4))
            .padding(8)
    }

    @ViewBuilder private var thumbnail: some View {
        if let bytes = thumbnailBytes {
            DecodedImage(bytes: bytes, cacheKey: embed.thumbnailURL, maxDimension: 700)
                .scaledToFill()
        } else if !embed.thumbnailURL.isEmpty {
            RemoteImage(url: embed.thumbnailURL, maxDimension: 700) {
                Rectangle().fill(.quaternary)
            }
            .scaledToFill()
        }
    }
}

extension CGFloat {
    /// Very tall or very wide clips would take over the feed; the player letterboxes the rest.
    var clampedVideoAspectRatio: CGFloat { Swift.min(Swift.max(self, 4 / 5), 21 / 9) }
}

/// AVKit's controller, for its fullscreen button; SwiftUI's `VideoPlayer` has none.
/// Starts playing at once, pauses when another inline video starts, the row scrolls away or the
/// app leaves the foreground, and reports a stream that fails to load.
struct InlineVideoPlayer: UIViewControllerRepresentable {
    let url: URL
    let onError: () -> Void

    func makeCoordinator() -> Coordinator { Coordinator(onError: onError) }

    func makeUIViewController(context: Context) -> AVPlayerViewController {
        let controller = AVPlayerViewController()
        controller.allowsPictureInPicturePlayback = false
        controller.player = context.coordinator.start(url)
        return controller
    }

    func updateUIViewController(_ controller: AVPlayerViewController, context: Context) {
        context.coordinator.onError = onError
    }

    static func dismantleUIViewController(_ controller: AVPlayerViewController, coordinator: Coordinator) {
        coordinator.stop()
        controller.player = nil
    }

    @MainActor
    final class Coordinator {
        var onError: () -> Void
        private var player: AVPlayer?
        private var observations: [NSKeyValueObservation] = []
        private var background: NSObjectProtocol?

        init(onError: @escaping () -> Void) { self.onError = onError }

        func start(_ url: URL) -> AVPlayer {
            let player = AVPlayer(url: url)
            self.player = player
            // Sound even with the silent switch on, like other video players.
            try? AVAudioSession.sharedInstance().setCategory(.playback, mode: .moviePlayback)
            observations = [
                player.observe(\.timeControlStatus) { player, _ in
                    guard player.timeControlStatus == .playing else { return }
                    Task { @MainActor in InlineVideoPlayback.claim(player) }
                },
                player.observe(\.currentItem?.status) { [weak self] player, _ in
                    guard player.currentItem?.status == .failed else { return }
                    Task { @MainActor in self?.onError() }
                },
            ]
            background = NotificationCenter.default.addObserver(
                forName: UIApplication.didEnterBackgroundNotification, object: nil, queue: .main
            ) { [weak player] _ in
                MainActor.assumeIsolated { player?.pause() }
            }
            player.play()
            return player
        }

        func stop() {
            observations.removeAll()
            if let background { NotificationCenter.default.removeObserver(background) }
            if let player {
                player.pause()
                InlineVideoPlayback.release(player)
            }
            player = nil
        }
    }
}

/// Keeps a single inline video playing at a time across the whole app.
@MainActor
enum InlineVideoPlayback {
    private static weak var active: AVPlayer?

    static func claim(_ player: AVPlayer) {
        if let active, active !== player { active.pause() }
        active = player
    }

    static func release(_ player: AVPlayer) {
        if active === player { active = nil }
    }
}

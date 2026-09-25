import SwiftUI
import PodkopShared

struct NativeTweetPreview: Hashable {
    let authorName: String
    let handle: String
    let avatarURL: String?
    let text: String
    let replies: Int
    let reposts: Int
    let likes: Int
    let mediaThumbnailURL: String?
    let mediaAspectRatio: Float?

    init(_ value: IOSTweetPreview) {
        authorName = value.authorName
        handle = value.authorHandle
        avatarURL = value.avatarUrl
        text = value.text
        replies = Int(value.replyCount)
        reposts = Int(value.retweetCount)
        likes = Int(value.likeCount)
        mediaThumbnailURL = value.mediaThumbnailUrl
        mediaAspectRatio = value.mediaAspectRatio?.floatValue
    }
}

struct NativeEmbedCard: View {
    let embed: NativeEmbed
    var thumbnailBytes: Data?
    var loadTweet: ((String) async throws -> NativeTweetPreview)?
    var open: ((URL) -> Void)?
    @State private var tweet: NativeTweetPreview?
    @State private var failed = false

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            if let tweet {
                Text(tweet.authorName).font(.subheadline.bold())
                Text(tweet.handle).font(.caption).foregroundStyle(.secondary)
                Text(tweet.text).font(.subheadline)
                if let bytes = thumbnailBytes, let url = tweet.mediaThumbnailURL {
                    NativeDecodedImage(bytes: bytes, cacheKey: url, maxDimension: 700)
                        .frame(height: 180)
                } else if let url = tweet.mediaThumbnailURL {
                    thumbnail(url)
                }
                Text("\(tweet.replies) ↩ · \(tweet.reposts) ↻ · \(tweet.likes) ♥")
                    .font(.caption).foregroundStyle(.secondary)
            } else {
                Label(failed ? "Preview unavailable" : embed.type.capitalized,
                      systemImage: failed ? "exclamationmark.triangle" : "play.rectangle")
                if let bytes = thumbnailBytes {
                    NativeDecodedImage(bytes: bytes, cacheKey: embed.thumbnailURL, maxDimension: 700)
                        .frame(maxHeight: 220)
                } else if !embed.thumbnailURL.isEmpty {
                    thumbnail(embed.thumbnailURL)
                }
            }
            if let url = URL(string: embed.url), let open {
                Button("Open source") { open(url) }.font(.caption)
            }
        }
        .padding(10)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(WykopTheme.cardInset, in: RoundedRectangle(cornerRadius: WykopTheme.smallRadius, style: .continuous))
        .task(id: embed.url) {
            guard embed.type.lowercased() == "twitter", let loadTweet else { return }
            do { tweet = try await loadTweet(embed.url) }
            catch is CancellationError { return }
            catch { failed = true }
        }
    }

    private func thumbnail(_ url: String) -> some View {
        RemoteImage(url: url, maxDimension: 700) {
            Rectangle().fill(.quaternary).overlay(ProgressView())
        }
        .frame(maxWidth: .infinity)
        .frame(height: 180)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .accessibilityHidden(true)
    }
}

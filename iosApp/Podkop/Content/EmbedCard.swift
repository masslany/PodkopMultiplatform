import SwiftUI
import PodkopShared

struct TweetPreview: Hashable {
    let authorName: String
    let handle: String
    let avatarURL: String?
    let text: String
    let replies: Int
    let reposts: Int
    let likes: Int
    let mediaThumbnailURL: String?
    let mediaAspectRatio: Float?

    init(authorName: String, handle: String, avatarURL: String?, text: String, replies: Int, reposts: Int,
         likes: Int, mediaThumbnailURL: String? = nil, mediaAspectRatio: Float? = nil) {
        self.authorName = authorName
        self.handle = handle
        self.avatarURL = avatarURL
        self.text = text
        self.replies = replies
        self.reposts = reposts
        self.likes = likes
        self.mediaThumbnailURL = mediaThumbnailURL
        self.mediaAspectRatio = mediaAspectRatio
    }

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

struct EmbedCard: View {
    let embed: Embed
    var thumbnailBytes: Data?
    var loadTweet: ((String) async throws -> TweetPreview)?
    var open: ((URL) -> Void)?
    @State private var tweet: TweetPreview?
    @State private var failed = false

    private var isTweet: Bool { embed.type.lowercased() == "twitter" }

    var body: some View {
        Group {
            if isTweet {
                if let tweet {
                    TweetCard(tweet: tweet, url: URL(string: embed.url), open: open)
                } else {
                    TweetPlaceholderCard(failed: failed || loadTweet == nil, url: URL(string: embed.url), open: open)
                }
            } else {
                otherEmbed
            }
        }
        .task(id: embed.url) {
            guard isTweet, let loadTweet else { return }
            do { tweet = try await loadTweet(embed.url) }
            catch is CancellationError { return }
            catch { failed = true }
        }
    }

    private var otherEmbed: some View {
        VStack(alignment: .leading, spacing: 8) {
            Label(embed.type.capitalized, systemImage: "play.rectangle")
            if let bytes = thumbnailBytes {
                DecodedImage(bytes: bytes, cacheKey: embed.thumbnailURL, maxDimension: 700)
                    .frame(maxHeight: 220)
            } else if !embed.thumbnailURL.isEmpty {
                thumbnail(embed.thumbnailURL)
            }
            if let url = URL(string: embed.url), let open {
                Button(.contentOpenSource) { open(url) }.font(.caption)
            }
        }
        .padding(10)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(PodkopTheme.cardInset, in: RoundedRectangle(cornerRadius: PodkopTheme.smallRadius, style: .continuous))
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

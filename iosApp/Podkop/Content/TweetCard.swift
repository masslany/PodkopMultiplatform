import SwiftUI

/// A tweet as X draws it (Android's `TwitterTweetCard`): the author's round avatar, name and
/// @handle with the X logo, the text with its links, the media, then replies, reposts and likes.
/// The whole card opens the tweet.
struct TweetCard: View {
    let tweet: TweetPreview
    let url: URL?
    var open: ((URL) -> Void)?

    var body: some View {
        TweetFrame(url: url, open: open) {
            VStack(alignment: .leading, spacing: 10) {
                header
                if !tweet.text.isEmpty {
                    Text(Self.linked(tweet.text))
                        .font(.subheadline)
                        .fixedSize(horizontal: false, vertical: true)
                }
                if let media = tweet.mediaThumbnailURL {
                    RemoteImage(url: media, maxDimension: 900) { Rectangle().fill(.quaternary) }
                        .aspectRatio(CGFloat(min(max(tweet.mediaAspectRatio ?? 16 / 9, 0.75), 2.5)),
                                     contentMode: .fit)
                        .frame(maxWidth: .infinity)
                        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                        .overlay(RoundedRectangle(cornerRadius: 12, style: .continuous)
                            .strokeBorder(.separator, lineWidth: 0.5))
                        .accessibilityHidden(true)
                }
                stats
            }
        }
    }

    private var header: some View {
        HStack(alignment: .center, spacing: 10) {
            RemoteImage(url: tweet.avatarURL, maxDimension: 120) {
                Circle().fill(.quaternary)
            }
            .frame(width: 40, height: 40)
            .clipShape(Circle())
            .accessibilityHidden(true)
            VStack(alignment: .leading, spacing: 1) {
                Text(tweet.authorName)
                    .font(.subheadline.weight(.bold))
                    .lineLimit(1)
                Text(verbatim: tweet.handle.hasPrefix("@") ? tweet.handle : "@\(tweet.handle)")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                    .lineLimit(1)
            }
            Spacer(minLength: 8)
            XLogo(size: 18)
        }
    }

    private var stats: some View {
        HStack(spacing: 24) {
            stat("bubble.left", tweet.replies)
            stat("arrow.2.squarepath", tweet.reposts)
            stat("heart", tweet.likes)
            Spacer(minLength: 0)
        }
        .font(.footnote)
        .foregroundStyle(.secondary)
        // The counts carry no label on Android either; VoiceOver reads the author and text.
        .accessibilityHidden(true)
    }

    private func stat(_ symbol: String, _ value: Int) -> some View {
        HStack(spacing: 5) {
            Image(systemName: symbol)
            Text(value.formatted(.number.notation(.compactName)))
                .monospacedDigit()
        }
    }

    /// Tweet text with its web links tappable in the app's blue, like mentions and tags elsewhere.
    static func linked(_ text: String) -> AttributedString {
        var result = AttributedString(text)
        guard let detector = try? NSDataDetector(types: NSTextCheckingResult.CheckingType.link.rawValue) else {
            return result
        }
        let source = text as NSString
        for match in detector.matches(in: text, range: NSRange(location: 0, length: source.length)) {
            guard let url = match.url, let range = Range(match.range, in: text),
                  let lower = AttributedString.Index(range.lowerBound, within: result),
                  let upper = AttributedString.Index(range.upperBound, within: result) else { continue }
            result[lower..<upper].link = url
            result[lower..<upper].foregroundColor = PodkopTheme.tagBlue
        }
        return result
    }
}

/// Before the tweet arrives, or when it cannot: the X logo on a card that still opens the tweet.
struct TweetPlaceholderCard: View {
    let failed: Bool
    let url: URL?
    var open: ((URL) -> Void)?

    var body: some View {
        TweetFrame(url: url, open: open) {
            HStack(spacing: 10) {
                XLogo(size: 18)
                if failed {
                    Text(.contentPreviewUnavailable)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                } else {
                    ProgressView()
                }
                Spacer(minLength: 0)
                if let host = url?.host() {
                    Text(verbatim: host.replacingOccurrences(of: "www.", with: ""))
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
            }
            .frame(minHeight: 44)
        }
    }
}

/// X's bordered card; tapping anywhere outside a link opens the tweet.
private struct TweetFrame<Content: View>: View {
    let url: URL?
    let open: ((URL) -> Void)?
    @ViewBuilder var content: Content

    var body: some View {
        content
            .padding(12)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(PodkopTheme.cardInset, in: RoundedRectangle(cornerRadius: 16, style: .continuous))
            .overlay(RoundedRectangle(cornerRadius: 16, style: .continuous).strokeBorder(.separator, lineWidth: 0.5))
            .contentShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
            .onTapGesture { if let url { open?(url) } }
            .accessibilityElement(children: .combine)
            .accessibilityAddTraits(url != nil && open != nil ? .isLink : [])
            .accessibilityHint(url != nil && open != nil ? String(localized: .contentOpenSource) : "")
    }
}

private struct XLogo: View {
    let size: CGFloat

    var body: some View {
        Image("XLogo")
            .resizable()
            .scaledToFit()
            .frame(width: size, height: size)
            .foregroundStyle(.primary)
            .accessibilityHidden(true)
    }
}

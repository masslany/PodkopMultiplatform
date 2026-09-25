import SwiftUI
import PodkopShared

enum ContentTokens {
    static let cardSpacing: CGFloat = 12
    static let cardPadding: CGFloat = 16
    static let cornerRadius: CGFloat = 14
    static let brand = Color(red: 0.12, green: 0.47, blue: 0.75)
    static let muted = Color.secondary
}

struct ResourceActions {
    var open: (() -> Void)?
    var openAuthor: ((String) -> Void)?
    var openTag: ((String) -> Void)?
    var openURL: ((URL) -> Void)?
    var voteUp: (() -> Void)?
    var voteDown: (() -> Void)?
    var favourite: (() -> Void)?
    var comment: (() -> Void)?
    var menu: (() -> Void)?
    var surveyVote: ((Int) -> Void)?
    var loadTweet: ((String) async throws -> NativeTweetPreview)?

    static let none = ResourceActions()
}

struct NativeResourceCard: View {
    let resource: NativeResource
    var actions: ResourceActions = .none
    var photoBytes: Data?
    var embedThumbnailBytes: Data?
    var autoplayGifs = false
    var isForeground = true
    @State private var adultRevealed = false
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    var body: some View {
        VStack(alignment: .leading, spacing: ContentTokens.cardSpacing) {
            header
            if !resource.title.isEmpty {
                if let open = actions.open {
                    Button(action: open) {
                        Text(resource.title).font(.headline).multilineTextAlignment(.leading)
                    }
                    .buttonStyle(.plain)
                } else {
                    Text(resource.title).font(.headline)
                }
            }
            if resource.adult && !adultRevealed {
                Button("Show adult content") { adultRevealed = true }
                    .buttonStyle(.bordered)
                    .accessibilityHint("Reveals sensitive content")
            } else {
                if resource.kind == .link, !resource.description.isEmpty, resource.deletion == nil {
                    Text(resource.description).font(.subheadline).lineLimit(5)
                }
                NativeRichContent(
                    source: resource.body, deletion: resource.deletion,
                    muted: resource.vote.state == "negative",
                    onProfile: { actions.openAuthor?($0) },
                    onTag: { actions.openTag?($0) },
                    onURL: { actions.openURL?($0) }
                )
                if let survey = resource.survey, resource.deletion == nil {
                    NativeSurveyView(survey: survey, vote: actions.surveyVote)
                }
                if let photo = resource.photo, resource.deletion == nil {
                    NativeMediaView(photo: photo, bytes: photoBytes,
                                    autoplay: autoplayGifs, foreground: isForeground)
                }
                if let embed = resource.embed, resource.deletion == nil {
                    NativeEmbedCard(embed: embed, thumbnailBytes: embedThumbnailBytes,
                                    loadTweet: actions.loadTweet, open: actions.openURL)
                }
            }
            if !resource.tags.isEmpty { tags }
            if resource.kind == .link, let source = resource.sourceLabel {
                if let raw = resource.sourceURL, let url = URL(string: raw),
                   let openURL = actions.openURL {
                    Button(source) { openURL(url) }.font(.caption)
                } else {
                    Text(source).font(.caption).foregroundStyle(.secondary)
                }
            }
            footer
        }
        .padding(ContentTokens.cardPadding)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(.regularMaterial, in: RoundedRectangle(cornerRadius: ContentTokens.cornerRadius))
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("resource-\(resource.id)")
    }

    private var header: some View {
        Group {
            if dynamicTypeSize.isAccessibilitySize {
                VStack(alignment: .leading, spacing: 6) {
                    HStack(spacing: 8) { authorIdentity; Spacer(minLength: 0) }
                    HStack(spacing: 10) { authorBadges; resourceBadges }
                }
            } else {
                HStack(spacing: 8) {
                    authorIdentity
                    authorBadges
                    Spacer(minLength: 4)
                    resourceBadges
                }
            }
        }
    }

    @ViewBuilder private var authorIdentity: some View {
        if let author = resource.author {
            Circle().fill(ContentTokens.brand.opacity(0.16))
                .frame(width: dynamicTypeSize.isAccessibilitySize ? 50 : 32,
                       height: dynamicTypeSize.isAccessibilitySize ? 50 : 32)
                .overlay(Text(String(author.name.prefix(1)).uppercased())
                    .font(.system(size: dynamicTypeSize.isAccessibilitySize ? 24 : 14, weight: .bold)))
                .accessibilityHidden(true)
            if let openAuthor = actions.openAuthor {
                Button(author.name) { openAuthor(author.name) }
                    .font(.subheadline.bold()).foregroundStyle(authorColor(author.color))
            } else {
                Text(author.name).font(.subheadline.bold())
                    .foregroundStyle(authorColor(author.color))
            }
        } else {
            Text("Unknown author").font(.subheadline).foregroundStyle(.secondary)
        }
    }

    @ViewBuilder private var authorBadges: some View {
        if let author = resource.author {
            if author.verified { Image(systemName: "checkmark.seal.fill")
                .foregroundStyle(ContentTokens.brand).accessibilityLabel("Verified author") }
            if author.online { Circle().fill(.green).frame(width: 7, height: 7)
                .accessibilityLabel("Online") }
            if let rank = author.rank { Text("#\(rank)").font(.caption2).foregroundStyle(.secondary) }
        }
    }

    @ViewBuilder private var resourceBadges: some View {
        if resource.hot { Image(systemName: "flame.fill").accessibilityLabel("Hot") }
        if resource.recommended { Image(systemName: "hand.thumbsup.fill").accessibilityLabel("Recommended") }
    }

    private var tags: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(resource.tags, id: \.self) { tag in
                    if let open = actions.openTag {
                        Button("#\(tag)") { open(tag) }.font(.caption)
                    } else {
                        Text("#\(tag)").font(.caption).foregroundStyle(ContentTokens.brand)
                    }
                }
            }
        }
    }

    private var footer: some View {
        HStack(spacing: 16) {
            if let voteUp = actions.voteUp, resource.vote.canUp || resource.vote.canUndo {
                Button { voteUp() } label: {
                    Label("\(resource.vote.up)", systemImage: resource.vote.state == "positive" ? "hand.thumbsup.fill" : "hand.thumbsup")
                }
                .accessibilityLabel(resource.vote.state == "positive" ? "Remove upvote" : "Upvote")
            } else {
                Label("\(resource.vote.up)", systemImage: "hand.thumbsup")
                    .accessibilityLabel(String(localized: "Upvotes") + ": \(resource.vote.up)")
            }
            if (resource.kind == .link || resource.kind == .linkComment), let voteDown = actions.voteDown,
               resource.vote.canDown || resource.vote.canUndo {
                Button { voteDown() } label: {
                    Label("\(resource.vote.down)", systemImage: "hand.thumbsdown")
                }
                .accessibilityLabel("Downvote")
            }
            if let comment = actions.comment {
                Button { comment() } label: {
                    Label("\(resource.commentCount)", systemImage: "bubble")
                }
                .accessibilityLabel(String(localized: "Comments") + ": \(resource.commentCount)")
            } else {
                Label("\(resource.commentCount)", systemImage: "bubble")
                    .accessibilityLabel(String(localized: "Comments") + ": \(resource.commentCount)")
            }
            Spacer(minLength: 0)
            if resource.title.isEmpty, let open = actions.open,
               resource.kind == .entry || resource.kind == .link {
                Button(action: open) {
                    Label("Open", systemImage: "arrow.up.right")
                }
                .accessibilityLabel(resource.kind == .entry ? "Open entry" : "Open link")
            }
            if let favourite = actions.favourite {
                Button { favourite() } label: {
                    Image(systemName: resource.favourite ? "star.fill" : "star")
                }
                .accessibilityLabel(resource.favourite ? "Remove favorite" : "Favorite")
            } else if resource.favourite {
                Image(systemName: "star.fill").accessibilityLabel("Favorite")
            }
            if let menu = actions.menu {
                Button(action: menu) { Image(systemName: "ellipsis") }
                    .accessibilityLabel("More actions")
            }
        }
        .font(.caption)
        .foregroundStyle(.secondary)
    }
}

struct NativeSurveyView: View {
    let survey: NativeSurvey
    var vote: ((Int) -> Void)?
    @State private var showResults = false

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(survey.question).font(.subheadline.bold())
            ForEach(Array(survey.answers.enumerated()), id: \.element.id) { item in
                let index = item.offset + 1
                let answer = item.element
                if survey.selectedOption != nil || showResults {
                    VStack(alignment: .leading, spacing: 3) {
                        HStack {
                            Text(answer.text)
                            if answer.selected { Image(systemName: "checkmark.circle.fill") }
                            Spacer()
                            Text("\(percentage(answer.count))%")
                        }
                        ProgressView(value: Double(percentage(answer.count)), total: 100)
                    }
                    .accessibilityElement(children: .combine)
                } else if survey.canVote, let vote {
                    Button(answer.text) { vote(index) }.buttonStyle(.bordered)
                } else {
                    Text(answer.text)
                }
            }
            HStack {
                Text(String(localized: "Votes") + ": \(survey.count)")
                    .font(.caption).foregroundStyle(.secondary)
                if survey.selectedOption == nil {
                    Button(showResults ? "Hide results" : "Show results") { showResults.toggle() }
                        .font(.caption)
                }
            }
        }
        .padding(10)
        .background(.quaternary, in: RoundedRectangle(cornerRadius: 9))
    }

    private func percentage(_ count: Int) -> Int {
        guard survey.count > 0 else { return 0 }
        return Int((Double(max(0, count)) / Double(survey.count) * 100).rounded())
    }
}

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
                }
                Text("\(tweet.replies) ↩ · \(tweet.reposts) ↻ · \(tweet.likes) ♥")
                    .font(.caption).foregroundStyle(.secondary)
            } else {
                Label(failed ? "Preview unavailable" : embed.type.capitalized,
                      systemImage: failed ? "exclamationmark.triangle" : "play.rectangle")
                if let bytes = thumbnailBytes {
                    NativeDecodedImage(bytes: bytes, cacheKey: embed.thumbnailURL, maxDimension: 700)
                        .frame(maxHeight: 220)
                }
            }
            if let url = URL(string: embed.url), let open {
                Button("Open source") { open(url) }.font(.caption)
            }
        }
        .padding(10)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(.quaternary, in: RoundedRectangle(cornerRadius: 9))
        .task(id: embed.url) {
            guard embed.type.lowercased() == "twitter", let loadTweet else { return }
            do { tweet = try await loadTweet(embed.url) }
            catch is CancellationError { return }
            catch { failed = true }
        }
    }
}

func authorColor(_ name: String?) -> Color {
    switch name {
    case "orange": .orange
    case "burgundy": Color(red: 0.55, green: 0.16, blue: 0.28)
    case "green": .green
    default: .primary
    }
}

enum NativeContentState<Value> {
    case loading, empty, content(Value), failure
}

struct NativeStateView<Content: View>: View {
    let state: NativeContentState<Content>
    var retry: (() -> Void)?
    var body: some View {
        switch state {
        case .loading: ProgressView("Loading…")
        case .empty: ContentUnavailableView("Nothing here yet", systemImage: "tray")
        case .content(let content): content
        case .failure:
            ContentUnavailableView {
                Label("Could not load content", systemImage: "wifi.exclamationmark")
            } actions: {
                if let retry { Button("Retry", action: retry) }
            }
        }
    }
}

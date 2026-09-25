import SwiftUI
import PodkopShared

struct NativeResourceCard: View {
    let resource: NativeResource
    var actions: ResourceActions = .none
    var photoBytes: Data?
    var screenshotPhoto: UIImage?
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
                    if let screenshotPhoto {
                        Image(uiImage: screenshotPhoto.images?.first ?? screenshotPhoto)
                            .resizable().scaledToFit()
                            .frame(maxWidth: .infinity)
                            .frame(height: min(320, max(120, 320 * CGFloat(photo.height) / CGFloat(max(photo.width, 1)))))
                    } else {
                        NativeMediaView(photo: photo, bytes: photoBytes,
                                        autoplay: autoplayGifs, foreground: isForeground)
                    }
                }
                if let embed = resource.embed, resource.deletion == nil {
                    NativeEmbedCard(embed: embed, thumbnailBytes: embedThumbnailBytes,
                                    loadTweet: actions.loadTweet, open: actions.openURL)
                }
            }
            // Entry hashtags are part of the text; like Android, only links get a separate tag row.
            if resource.kind == .link, !resource.tags.isEmpty { tags }
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
            AvatarView(url: author.avatarURL, name: author.name,
                       size: dynamicTypeSize.isAccessibilitySize ? 50 : 32)
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

import SwiftUI
import PodkopShared

/// A link, entry or comment in Wykop's layout (Android's `LinkItem`, `EntryItem` and comment
/// items): links lead with the vote badge beside the title, entries and comments with the author
/// and the signed score. When `actions.open` is set the card is a list row that opens its detail.
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

    private var isListRow: Bool { actions.open != nil }
    private var isComment: Bool { resource.kind == .entryComment || resource.kind == .linkComment }
    private var adultHidden: Bool { resource.adult && !adultRevealed && resource.deletion == nil }

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            if resource.kind == .link { linkLayout } else { entryLayout }
            actionsRow
        }
        .wykopCard(padding: 14)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("resource-\(resource.id)")
    }

    // MARK: Links

    @ViewBuilder private var linkLayout: some View {
        HStack(alignment: .top, spacing: 12) {
            LinkVoteBadge(vote: resource.vote, hot: resource.hot, pending: actions.pending,
                          action: actions.voteUp)
                .padding(.top, 2)
            title
        }
        AdultContentGate(hidden: adultHidden, reveal: { adultRevealed = true }) {
            VStack(alignment: .leading, spacing: 10) {
                if isListRow { linkSummary } else { linkDetailBody }
            }
        }
        linkMeta
        if !resource.tags.isEmpty { tags }
    }

    @ViewBuilder private var title: some View {
        let text = Text(resource.title)
            .font(isListRow ? .headline : .title3.bold())
            .multilineTextAlignment(.leading)
            .foregroundStyle(.primary)
            .frame(maxWidth: .infinity, alignment: .leading)
        if let open = actions.open {
            Button(action: open) { text }.buttonStyle(.plain)
        } else {
            text
        }
    }

    /// Description beside an 80 pt thumbnail, as in Wykop lists.
    @ViewBuilder private var linkSummary: some View {
        if resource.deletion != nil {
            richContent
        } else if !resource.description.isEmpty || resource.photo != nil {
            HStack(alignment: .top, spacing: 12) {
                Text(resource.description)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .lineLimit(4)
                    .frame(maxWidth: .infinity, alignment: .leading)
                if let photo = resource.photo {
                    RemoteImage(url: photo.url, maxDimension: 240) {
                        WykopTheme.cardInset
                    }
                    .frame(width: 80, height: 80)
                    .clipShape(RoundedRectangle(cornerRadius: WykopTheme.smallRadius, style: .continuous))
                    .onTapGesture { actions.open?() }
                    .accessibilityHidden(true)
                }
            }
        }
    }

    @ViewBuilder private var linkDetailBody: some View {
        if resource.deletion == nil {
            if !resource.description.isEmpty {
                Text(resource.description).font(.body)
            }
            media
        }
        if !resource.body.isEmpty || resource.deletion != nil { richContent }
    }

    /// "author • source • time", wrapping like Android's `FlowRow`.
    private var linkMeta: some View {
        FlowLayout(spacing: 5) {
            authorName(font: .subheadline.weight(.semibold))
            if let source = resource.sourceLabel {
                separator
                if let raw = resource.sourceURL, let url = URL(string: raw), let openURL = actions.openURL {
                    Button(source) { openURL(url) }
                        .font(.subheadline)
                        .foregroundStyle(WykopTheme.tagBlue)
                        .buttonStyle(.plain)
                } else {
                    Text(source).font(.subheadline).foregroundStyle(.secondary)
                }
            }
            if let time = PublishedTime.text(iso: resource.createdAt) {
                separator
                Text(time).font(.subheadline).foregroundStyle(.secondary)
            }
        }
    }

    private var separator: some View {
        Text(verbatim: "•").font(.subheadline).foregroundStyle(.tertiary).accessibilityHidden(true)
    }

    private var tags: some View {
        FlowLayout(spacing: 10) {
            ForEach(resource.tags, id: \.self) { tag in
                if let open = actions.openTag {
                    Button("#\(tag)") { open(tag) }
                        .buttonStyle(.plain)
                } else {
                    Text("#\(tag)")
                }
            }
        }
        .font(.footnote.weight(.medium))
        .foregroundStyle(.secondary)
    }

    // MARK: Entries and comments

    @ViewBuilder private var entryLayout: some View {
        if dynamicTypeSize.isAccessibilitySize {
            identity
            scoreControl
        } else {
            HStack(alignment: .top, spacing: 8) {
                identity
                Spacer(minLength: 4)
                scoreControl
            }
        }
        if resource.deletion != nil {
            richContent
        } else {
            AdultContentGate(hidden: adultHidden, reveal: { adultRevealed = true }) {
                VStack(alignment: .leading, spacing: 10) {
                    if !resource.body.isEmpty { richContent }
                    if let survey = resource.survey {
                        NativeSurveyView(survey: survey, vote: actions.surveyVote)
                    }
                    media
                }
            }
        }
    }

    private var identity: some View {
        HStack(alignment: .top, spacing: 10) {
            if let author = resource.author {
                let size: CGFloat = dynamicTypeSize.isAccessibilitySize ? 48 : isComment ? 32 : 36
                Group {
                    if let openAuthor = actions.openAuthor {
                        Button { openAuthor(author.name) } label: {
                            AvatarView(url: author.avatarURL, name: author.name, size: size, gender: author.gender)
                        }
                        .buttonStyle(.plain)
                        .accessibilityHidden(true)
                    } else {
                        AvatarView(url: author.avatarURL, name: author.name, size: size, gender: author.gender)
                    }
                }
            }
            VStack(alignment: .leading, spacing: 2) {
                authorName(font: .subheadline.weight(.bold))
                if let time = PublishedTime.text(iso: resource.createdAt) {
                    Text(time).font(.caption).foregroundStyle(.secondary)
                }
            }
        }
    }

    private var scoreControl: some View {
        ScoreVoteControl(vote: resource.vote, showsDown: resource.kind == .linkComment,
                         pending: actions.pending, up: actions.voteUp, down: actions.voteDown)
    }

    // MARK: Shared pieces

    @ViewBuilder private func authorName(font: Font) -> some View {
        if let author = resource.author {
            HStack(spacing: 4) {
                if let openAuthor = actions.openAuthor {
                    Button(author.name) { openAuthor(author.name) }
                        .buttonStyle(.plain)
                } else {
                    Text(author.name)
                }
                if author.verified {
                    Image(systemName: "checkmark.seal.fill")
                        .font(.caption)
                        .foregroundStyle(WykopTheme.tagBlue)
                        .accessibilityLabel("Verified author")
                }
            }
            .font(font)
            .foregroundStyle(authorColor(author.color))
        } else {
            Text("Unknown author").font(font).foregroundStyle(.secondary)
        }
    }

    private var richContent: some View {
        NativeRichContent(
            source: resource.body, deletion: resource.deletion,
            muted: resource.vote.state == "negative",
            onProfile: { actions.openAuthor?($0) },
            onTag: { actions.openTag?($0) },
            onURL: { actions.openURL?($0) }
        )
    }

    @ViewBuilder private var media: some View {
        if let photo = resource.photo {
            if let screenshotPhoto {
                Image(uiImage: screenshotPhoto.images?.first ?? screenshotPhoto)
                    .resizable().scaledToFit()
                    .frame(maxWidth: .infinity)
                    .frame(height: min(320, max(120, 320 * CGFloat(photo.height) / CGFloat(max(photo.width, 1)))))
            } else {
                NativeMediaView(photo: photo, bytes: photoBytes, autoplay: autoplayGifs, foreground: isForeground)
            }
        }
        if let embed = resource.embed {
            NativeEmbedCard(embed: embed, thumbnailBytes: embedThumbnailBytes,
                            loadTweet: actions.loadTweet, open: actions.openURL)
        }
    }

    /// Reply or open, bury for links, favourite and more (Android's `ResourceInlineActionsRow`).
    private var actionsRow: some View {
        HStack(spacing: 18) {
            if isListRow, let open = actions.open {
                Button(action: open) {
                    Label("\(resource.commentCount)", systemImage: "bubble.left")
                }
                .accessibilityLabel(resource.kind == .link ? "Open link" : "Open entry")
                .accessibilityValue(String(localized: "Comments") + ": \(resource.commentCount)")
            } else {
                if !isComment {
                    Label("\(resource.commentCount)", systemImage: "bubble.left")
                        .accessibilityLabel(String(localized: "Comments") + ": \(resource.commentCount)")
                }
                if let comment = actions.comment {
                    Button(action: comment) {
                        Label("Reply", systemImage: "arrowshape.turn.up.left")
                    }
                }
            }
            if resource.kind == .link, let voteDown = actions.voteDown {
                Button(action: voteDown) {
                    Label("Bury",
                          systemImage: resource.vote.state == "negative" ? "arrow.down.circle.fill" : "arrow.down.circle")
                }
                .foregroundStyle(resource.vote.state == "negative" ? WykopTheme.voteNegative : .secondary)
                .disabled(actions.pending)
                .accessibilityLabel(resource.vote.state == "negative" ? "Remove downvote" : "Downvote")
            }
            Spacer(minLength: 0)
            if let favourite = actions.favourite {
                Button(action: favourite) {
                    Image(systemName: resource.favourite ? "star.fill" : "star")
                        .foregroundStyle(resource.favourite ? WykopTheme.favouriteGold : .secondary)
                }
                .disabled(actions.pending)
                .accessibilityLabel(resource.favourite ? "Remove favorite" : "Favorite")
            } else if resource.favourite {
                Image(systemName: "star.fill").foregroundStyle(WykopTheme.favouriteGold)
                    .accessibilityLabel("Favorite")
            }
            if let menu = actions.menu {
                Button(action: menu) {
                    Image(systemName: "ellipsis")
                        .frame(width: 24, height: 20)
                        .contentShape(Rectangle())
                }
                .accessibilityLabel("More actions")
            }
        }
        .font(.subheadline)
        .labelStyle(.titleAndIcon)
        .buttonStyle(.plain)
        .foregroundStyle(.secondary)
    }
}

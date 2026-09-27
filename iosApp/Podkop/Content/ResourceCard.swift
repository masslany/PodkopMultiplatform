import SwiftUI
import PodkopShared

/// A link, entry or comment in Wykop's layout (Android's `LinkItem`, `EntryItem`, comment items
/// and `LinkDetailsHeader`). Links lead with the vote badge beside the title; entries and comments
/// with the author and the signed score.
struct ResourceCard: View {
    enum Style {
        /// A list row on its own card; tapping anywhere opens the detail.
        case card
        /// Inside a thread card or a comment list; no background of its own.
        case embedded
        /// The top of a detail screen, flush with the background; tapping a link opens its page.
        case detailHeader
    }

    let resource: Resource
    var actions: ResourceActions = .none
    var style: Style = .card
    var photoBytes: Data?
    var screenshotPhoto: UIImage?
    var embedThumbnailBytes: Data?
    var autoplayGifs = false
    var isForeground = true
    /// Off when a surrounding thread card carries this resource's identifier.
    var identified = true
    var showsActions = true
    /// Detail header only: reports the bottom of the title in the `detailContent` coordinate
    /// space, so the screen can show the title in the navigation bar once it scrolls away.
    var onTitleBottom: ((CGFloat) -> Void)?
    @State private var adultRevealed = false
    @State private var blacklistRevealed = false
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    static let detailContentSpace = "detailContent"

    private var isComment: Bool { resource.kind == .entryComment || resource.kind == .linkComment }
    private var adultHidden: Bool { resource.adult && !adultRevealed && resource.deletion == nil }

    var body: some View {
        switch style {
        case .card:
            content
                .podkopCard(padding: 14)
                .contentShape(Rectangle())
                .onTapGesture { actions.open?() }
                .accessibilityElement(children: .contain)
                .accessibilityIdentifier("resource-\(resource.id)")
        case .embedded, .detailHeader:
            content
                .frame(maxWidth: .infinity, alignment: .leading)
                .accessibilityElement(children: .contain)
                .accessibilityIdentifier(identified ? "resource-\(resource.id)" : "")
        }
    }

    @ViewBuilder private var content: some View {
        VStack(alignment: .leading, spacing: 8) {
            if resource.kind == .link {
                if style == .detailHeader { linkDetailLayout } else { linkListLayout }
            } else {
                entryLayout
                if showsActions { actionsRow }
            }
        }
    }

    // MARK: Links

    /// Side by side normally; stacked at accessibility text sizes so text keeps the full width.
    private func adaptiveStack(spacing: CGFloat) -> AnyLayout {
        dynamicTypeSize.isAccessibilitySize
            ? AnyLayout(VStackLayout(alignment: .leading, spacing: spacing))
            : AnyLayout(HStackLayout(alignment: .top, spacing: spacing))
    }

    /// Android's `LinkItem`: badge and title, description with thumbnail, meta, tags and comments.
    @ViewBuilder private var linkListLayout: some View {
        (adaptiveStack(spacing: 12)) {
            LinkVoteBadge(vote: resource.vote, hot: resource.hot, pending: actions.pending,
                          action: actions.voteUp)
            Text(resource.title)
                .font(.headline)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
        AdultContentGate(hidden: adultHidden, reveal: { adultRevealed = true }) {
            if resource.deletion != nil {
                richContent
            } else if !resource.description.isEmpty || resource.photo != nil {
                (adaptiveStack(spacing: 10)) {
                    Text(resource.description)
                        .font(.subheadline)
                        .lineLimit(5)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    if let photo = resource.photo {
                        RemoteImage(url: photo.url, maxDimension: 240) { PodkopTheme.cardInset }
                            .frame(width: 80, height: 80)
                            .clipShape(RoundedRectangle(cornerRadius: PodkopTheme.smallRadius, style: .continuous))
                            .accessibilityHidden(true)
                    }
                }
            }
        }
        linkMeta(showsTime: true)
        HStack(alignment: .bottom, spacing: 8) {
            tags.frame(maxWidth: .infinity, alignment: .leading)
            Label(String(resource.commentCount), systemImage: "text.bubble.fill")
                .font(.footnote)
                .foregroundStyle(.primary)
                .accessibilityLabel(resource.kind == .link ? .commonOpenLink : .commonOpenEntry)
                .accessibilityValue(String(localized: .commonComments) + ": \(resource.commentCount)")
                .accessibilityAddTraits(.isButton)
        }
    }

    /// Android's `LinkDetailsHeader`: a full-width image, then the badge with the bury button
    /// below it beside the title. The image, title and description open the article.
    @ViewBuilder private var linkDetailLayout: some View {
        VStack(alignment: .leading, spacing: 10) {
            if let photo = resource.photo, resource.deletion == nil {
                AdultContentGate(hidden: adultHidden, reveal: { adultRevealed = true }) {
                    RemoteImage(url: photo.url, maxDimension: 1200) { PodkopTheme.cardInset }
                        .frame(height: 200)
                        .frame(maxWidth: .infinity)
                        .clipped()
                        .contentShape(Rectangle())
                        .onTapGesture { openSource() }
                }
                .accessibilityHidden(true)
            }
            (adaptiveStack(spacing: 12)) {
                VStack(spacing: 4) {
                    LinkVoteBadge(vote: resource.vote, hot: resource.hot, pending: actions.pending,
                                  action: actions.voteUp)
                    if let buryLink = actions.buryLink, resource.vote.state != "negative" {
                        // Anchored on the button like Android's dropdown, so the menu points at it.
                        Menu {
                            ForEach(VoteReason.allCases) { reason in
                                Button { buryLink(reason) } label: { Text(reason.title) }
                            }
                        } label: {
                            buryLabel
                        }
                        .menuStyle(.button)
                        .buttonStyle(.plain)
                        .disabled(actions.pending)
                        .accessibilityLabel(.contentDownvote)
                    } else if let voteDown = actions.voteDown {
                        Button(action: voteDown) { buryLabel }
                            .buttonStyle(.plain)
                            .disabled(actions.pending)
                            .accessibilityLabel(resource.vote.state == "negative" ? .contentRemoveDownvote : .contentDownvote)
                    }
                }
                Text(resource.title)
                    .font(.headline)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .contentShape(Rectangle())
                    // Only the image, title and description open the article; a tap gesture
                    // around the vote column would take taps from the bury menu.
                    .onTapGesture { openSource() }
                    .onGeometryChange(for: CGFloat.self) {
                        $0.frame(in: .named(ResourceCard.detailContentSpace)).maxY
                    } action: { onTitleBottom?($0) }
            }
            .padding(.horizontal, 16)
            if resource.deletion == nil, !resource.description.isEmpty {
                Text(resource.description)
                    .font(.subheadline)
                    .padding(.horizontal, 16)
                    .onTapGesture { openSource() }
            }
        }
        VStack(alignment: .leading, spacing: 8) {
            if !resource.body.isEmpty || resource.deletion != nil { richContent }
            if resource.deletion == nil, let embed = resource.embed {
                EmbedCard(embed: embed, thumbnailBytes: embedThumbnailBytes,
                                loadTweet: actions.loadTweet, open: actions.openURL)
            }
            linkMeta(showsTime: false)
            tags
            if showsActions { actionsRow }
        }
        .padding(.horizontal, 16)
    }

    private func openSource() {
        guard let raw = resource.sourceURL, let url = URL(string: raw) else { return }
        actions.openURL?(url)
    }

    /// "author • source • time", wrapping like Android's `FlowRow`.
    private func linkMeta(showsTime: Bool) -> some View {
        FlowLayout(spacing: 4) {
            authorName(font: .footnote.weight(.semibold))
            if let source = resource.sourceLabel {
                separator
                Button(source) { openSource() }
                    .buttonStyle(.plain)
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }
            if showsTime, let time = PublishedTime.text(iso: resource.createdAt) {
                separator
                Text(time).font(.footnote).foregroundStyle(.secondary)
            }
        }
    }

    /// "Zakop" under the link's vote badge, or "Cofnij" once buried.
    private var buryLabel: some View {
        let buried = resource.vote.state == "negative"
        return Text(buried ? .contentUndoBury : .contentBury)
            .font(.caption.weight(.medium))
            .foregroundStyle(buried ? PodkopTheme.voteNegative : .secondary)
    }

    private var separator: some View {
        Text(verbatim: "•").font(.footnote).foregroundStyle(.secondary).accessibilityHidden(true)
    }

    /// Tags separated by dots, as on Android.
    @ViewBuilder private var tags: some View {
        if !resource.tags.isEmpty {
            FlowLayout(spacing: 4) {
                ForEach(Array(resource.tags.enumerated()), id: \.element) { index, tag in
                    Group {
                        if let open = actions.openTag {
                            Button("#" + tag) { open(tag) }.buttonStyle(.plain)
                        } else {
                            Text(verbatim: "#\(tag)")
                        }
                    }
                    .foregroundStyle(PodkopTheme.tagBlue)
                    if index < resource.tags.count - 1 { separator }
                }
            }
            .font(.footnote)
        }
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
        } else if resource.blacklisted && !blacklistRevealed {
            BlacklistedContentNotice { blacklistRevealed = true }
        } else {
            AdultContentGate(hidden: adultHidden, reveal: { adultRevealed = true }) {
                VStack(alignment: .leading, spacing: 8) {
                    if !resource.body.isEmpty { richContent }
                    if let survey = resource.survey {
                        SurveyView(survey: survey, vote: actions.surveyVote, pending: actions.pending)
                    }
                    media
                }
            }
        }
    }

    private var identity: some View {
        HStack(alignment: .top, spacing: 8) {
            if let author = resource.author {
                let size: CGFloat = dynamicTypeSize.isAccessibilitySize ? 48 : isComment ? 32 : 36
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
            VStack(alignment: .leading, spacing: 1) {
                authorName(font: .subheadline.weight(.semibold))
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
                        .foregroundStyle(PodkopTheme.tagBlue)
                        .accessibilityLabel(.commonVerifiedAuthor)
                }
            }
            .font(font)
            .foregroundStyle(authorColor(author.color))
        } else {
            Text(.contentUnknownAuthor).font(font).foregroundStyle(.secondary)
        }
    }

    private var richContent: some View {
        RichContent(
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
                MediaView(photo: photo, bytes: photoBytes, autoplay: autoplayGifs, foreground: isForeground)
            }
        }
        if let embed = resource.embed {
            EmbedCard(embed: embed, thumbnailBytes: embedThumbnailBytes,
                            loadTweet: actions.loadTweet, open: actions.openURL)
        }
    }

    /// Reply, favourite and more (Android's `ResourceInlineActionsRow`). Unavailable actions
    /// stay visible but dimmed, as on Android.
    private var actionsRow: some View {
        HStack(spacing: 16) {
            Button { actions.comment?() } label: {
                Label(.contentReply, systemImage: "arrowshape.turn.up.left")
                    .font(.subheadline.weight(.medium))
            }
            .disabled(actions.comment == nil)
            .opacity(actions.comment == nil ? 0.4 : 1)
            Button { actions.favourite?() } label: {
                Image(systemName: resource.favourite ? "star.fill" : "star")
                    .foregroundStyle(resource.favourite ? PodkopTheme.favouriteGold : .secondary)
            }
            .disabled(actions.favourite == nil)
            .opacity(actions.favourite == nil ? 0.4 : 1)
            .accessibilityLabel(resource.favourite ? .contentRemoveFavorite : .contentFavorite)
            Spacer(minLength: 0)
            if let menu = actions.menu {
                Button(action: menu) {
                    Image(systemName: "ellipsis")
                        .rotationEffect(.degrees(90))
                        .frame(width: 28, height: 24)
                        .contentShape(Rectangle())
                }
                .accessibilityLabel(.contentMoreActions)
            }
        }
        .labelStyle(.titleAndIcon)
        .buttonStyle(.plain)
        .foregroundStyle(.primary)
        .padding(.top, 2)
    }
}

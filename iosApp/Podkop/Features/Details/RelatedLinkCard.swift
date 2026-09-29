import SwiftUI

/// A related link: image on the left, author, source, score with vote buttons and the title
/// (Android's related links row). Tapping the card opens the article.
struct RelatedLinkCard: View {
    let resource: Resource
    var pending = false
    let open: () -> Void
    let openAuthor: (String) -> Void
    var voteUp: (() -> Void)?
    var voteDown: (() -> Void)?
    @Environment(\.adultContentAllowed) private var adultContentAllowed

    var body: some View {
        HStack(alignment: .top, spacing: 10) {
            RemoteImage(url: resource.adult && !adultContentAllowed ? nil : resource.photo?.url,
                        maxDimension: 360) {
                PodkopTheme.cardInset.overlay(Image(systemName: "link").foregroundStyle(.secondary))
            }
            .frame(width: 96)
            .frame(maxHeight: .infinity)
            .clipped()
            .accessibilityHidden(true)
            VStack(alignment: .leading, spacing: 4) {
                HStack(alignment: .top) {
                    VStack(alignment: .leading, spacing: 2) {
                        if let author = resource.author {
                            Button(author.name) { openAuthor(author.name) }
                                .buttonStyle(.plain)
                                .font(.footnote.weight(.semibold))
                                .foregroundStyle(authorColor(author.color))
                                .lineLimit(1)
                        }
                        if let source = resource.sourceLabel {
                            Text(source).font(.caption).foregroundStyle(.secondary).lineLimit(1)
                        }
                    }
                    Spacer(minLength: 4)
                    ScoreVoteControl(vote: resource.vote, showsDown: true, pending: pending,
                                     up: voteUp, down: voteDown)
                }
                Text(resource.title.isEmpty ? resource.body : resource.title)
                    .font(.footnote)
                    .lineLimit(3)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(.vertical, 10)
            .padding(.trailing, 10)
        }
        .frame(width: 300, height: 112)
        .background(PodkopTheme.card)
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
        .contentShape(Rectangle())
        .onTapGesture(perform: open)
        .accessibilityElement(children: .contain)
        .accessibilityAction(named: Text(.commonOpenLink), open)
    }
}

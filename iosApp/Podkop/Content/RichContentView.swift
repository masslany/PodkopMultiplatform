import SwiftUI

struct RichContent: View {
    let source: String
    let deletion: Deletion?
    var muted = false
    var onProfile: (String) -> Void = { _ in }
    var onTag: (String) -> Void = { _ in }
    var onURL: (URL) -> Void = { _ in }
    @State private var expanded = false

    var body: some View {
        Group {
            if let deletion {
                Text(deletionLabel(deletion))
                    .foregroundStyle(.secondary)
            } else {
                let blocks = RichContentParser.parse(source)
                let preview = expanded ? nil : RichContentParser.preview(blocks)
                VStack(alignment: .leading, spacing: 6) {
                    ForEach(Array((preview ?? blocks).enumerated()), id: \.offset) { item in
                        blockView(item.element)
                    }
                    if preview != nil {
                        Button { expanded = true } label: {
                            HStack(spacing: 4) {
                                Text(.contentShowMore)
                                Image(systemName: "chevron.down").font(.caption.weight(.bold))
                            }
                            .font(.subheadline.weight(.semibold))
                            .foregroundStyle(WykopTheme.tagBlue)
                            .frame(minHeight: 32)
                            .contentShape(Rectangle())
                        }
                        .buttonStyle(.plain)
                    }
                }
                .foregroundStyle(muted ? .secondary : .primary)
                .environment(\.openURL, OpenURLAction { url in
                    if url.scheme == "podkop", url.host == "profile" {
                        onProfile(url.pathComponents.dropFirst().joined(separator: "/"))
                    } else if url.scheme == "podkop", url.host == "tag" {
                        onTag(url.pathComponents.dropFirst().joined(separator: "/"))
                    } else {
                        onURL(url)
                    }
                    return .handled
                })
            }
        }
        .font(.subheadline)
        .textSelection(.enabled)
        // Mentions, tags and links keep Wykop's blue while controls stay neutral.
        .tint(WykopTheme.tagBlue)
    }

    @ViewBuilder private func blockView(_ block: RichBlock) -> some View {
        switch block {
        case .paragraph(let text):
            Text(RichContentParser.attributed(text))
        case .bullet(let indent, let text):
            (Text(verbatim: "• ") + Text(RichContentParser.attributed(text)))
                .padding(.leading, CGFloat(indent) * 18)
        case .numbered(let indent, let number, let text):
            (Text(verbatim: "\(number). ") + Text(RichContentParser.attributed(text)))
                .padding(.leading, CGFloat(indent) * 18)
        case .quote(let text):
            // A bar the full height of the quote, with the text indented (Android's block quote).
            Text(RichContentParser.attributed(text))
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.leading, 12)
                .overlay(alignment: .leading) {
                    RoundedRectangle(cornerRadius: 1)
                        .fill(Color.secondary)
                        .frame(width: 2)
                        .accessibilityHidden(true)
                }
        case .code(let text):
            Text(verbatim: text).font(.system(.subheadline, design: .monospaced))
                .padding(8).background(.quaternary, in: RoundedRectangle(cornerRadius: 6))
        case .spoiler(let text):
            SpoilerBlock(text: text)
        case .empty:
            Spacer(minLength: 5).frame(height: 5)
        }
    }

    private func deletionLabel(_ deletion: Deletion) -> LocalizedStringResource {
        switch deletion {
        case .author: .contentRemovedAuthor
        case .moderator: .contentRemovedModerator
        case .entryAuthor: .contentRemovedEntryAuthor
        case .unknown: .contentRemovedContent
        }
    }
}

private struct SpoilerBlock: View {
    let text: String
    @State private var revealed = false
    var body: some View {
        if revealed {
            Text(RichContentParser.attributed(text))
        } else {
            Button(.contentShowSpoiler) { revealed = true }
                .buttonStyle(.bordered)
                .accessibilityHint(.contentRevealsHiddenText)
        }
    }
}

import SwiftUI

struct NativeRichContent: View {
    let source: String
    let deletion: NativeDeletion?
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
                let shown = expanded || source.count <= 1000
                    ? source : String(source.prefix(1000)) + "…"
                VStack(alignment: .leading, spacing: 3) {
                    ForEach(Array(RichContentParser.parse(shown).enumerated()), id: \.offset) { item in
                        blockView(item.element)
                    }
                    if source.count > 1000 && !expanded {
                        Button("Show more") { expanded = true }
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
            (Text("• ") + Text(RichContentParser.attributed(text)))
                .padding(.leading, CGFloat(indent) * 18)
        case .numbered(let indent, let number, let text):
            (Text("\(number). ") + Text(RichContentParser.attributed(text)))
                .padding(.leading, CGFloat(indent) * 18)
        case .quote(let text):
            Text("▏ ") + Text(RichContentParser.attributed(text))
        case .code(let text):
            Text(verbatim: text).font(.system(.subheadline, design: .monospaced))
                .padding(8).background(.quaternary, in: RoundedRectangle(cornerRadius: 6))
        case .spoiler(let text):
            SpoilerBlock(text: text)
        case .empty:
            Spacer(minLength: 5).frame(height: 5)
        }
    }

    private func deletionLabel(_ deletion: NativeDeletion) -> LocalizedStringKey {
        switch deletion {
        case .author: "Removed by author"
        case .moderator: "Removed by moderator"
        case .entryAuthor: "Removed by entry author"
        case .unknown: "Removed content"
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
            Button("Show spoiler") { revealed = true }
                .buttonStyle(.bordered)
                .accessibilityHint("Reveals hidden text")
        }
    }
}

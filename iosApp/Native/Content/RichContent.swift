import Foundation
import SwiftUI

enum RichBlock: Equatable {
    case paragraph(String)
    case bullet(Int, String)
    case numbered(Int, String, String)
    case quote(String)
    case code(String)
    case spoiler(String)
    case empty
}

enum RichContentParser {
    static func parse(_ source: String) -> [RichBlock] {
        let lines = source.replacingOccurrences(of: "\r\n", with: "\n")
            .components(separatedBy: "\n")
        var blocks: [RichBlock] = []
        var code: [String] = []
        var inFence = false
        for line in lines {
            if line.trimmingCharacters(in: .whitespaces).hasPrefix("```") {
                if inFence { blocks.append(.code(code.joined(separator: "\n"))); code = [] }
                inFence.toggle()
                continue
            }
            if inFence { code.append(line); continue }
            if line.isEmpty { blocks.append(.empty); continue }
            let trimmed = line.trimmingCharacters(in: .whitespaces)
            let indent = min(4, line.prefix(while: { $0 == " " || $0 == "\t" }).count / 2)
            if trimmed.hasPrefix("!") {
                blocks.append(.spoiler(String(trimmed.dropFirst())))
            } else if trimmed.hasPrefix(">") {
                blocks.append(.quote(String(trimmed.dropFirst()).trimmingCharacters(in: .whitespaces)))
            } else if trimmed.hasPrefix("- ") || trimmed.hasPrefix("* ") {
                blocks.append(.bullet(indent, String(trimmed.dropFirst(2))))
            } else if let dot = trimmed.firstIndex(of: "."),
                      !trimmed[..<dot].isEmpty,
                      trimmed[..<dot].allSatisfy(\.isNumber),
                      trimmed[trimmed.index(after: dot)...].hasPrefix(" ") {
                blocks.append(.numbered(indent, String(trimmed[..<dot]),
                                        String(trimmed[trimmed.index(dot, offsetBy: 2)...])))
            } else {
                // Standalone dashes are literal, as in the existing Compose renderer.
                blocks.append(.paragraph(trimmed))
            }
        }
        if inFence { blocks.append(.code(code.joined(separator: "\n"))) }
        return blocks
    }

    static func attributed(_ source: String) -> AttributedString {
        let linked = linkMentionsAndTags(source)
        return (try? AttributedString(markdown: linked,
                                      options: .init(interpretedSyntax: .inlineOnlyPreservingWhitespace)))
            ?? AttributedString(source)
    }

    static func linkMentionsAndTags(_ source: String) -> String {
        let pattern = #"(?<![\p{L}\p{N}_/])[@#][\p{L}\p{N}_-]+"#
        guard let expression = try? NSRegularExpression(pattern: pattern) else { return source }
        let range = NSRange(source.startIndex..<source.endIndex, in: source)
        let protectedPattern = #"\x60[^\x60]*\x60|\[[^\]]+\]\([^)]*\)"#
        let protected = (try? NSRegularExpression(pattern: protectedPattern))?
            .matches(in: source, range: range).map(\.range) ?? []
        var result = source
        for match in expression.matches(in: source, range: range).reversed() {
            if protected.contains(where: { NSIntersectionRange($0, match.range).length > 0 }) {
                continue
            }
            guard let span = Range(match.range, in: source),
                  let replacementRange = Range(match.range, in: result) else { continue }
            let token = String(source[span])
            let kind = token.first == "@" ? "profile" : "tag"
            let value = String(token.dropFirst())
            guard let encoded = value.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) else { continue }
            result.replaceSubrange(replacementRange,
                                   with: "[\(token)](podkop://\(kind)/\(encoded))")
        }
        return result
    }
}

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

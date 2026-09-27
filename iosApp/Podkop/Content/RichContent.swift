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

    var textLength: Int {
        switch self {
        case .paragraph(let text), .quote(let text), .code(let text), .spoiler(let text): text.count
        case .bullet(_, let text), .numbered(_, _, let text): text.count
        case .empty: 0
        }
    }
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
                // Consecutive quote lines form one quote, as in Markdown.
                let text = String(trimmed.dropFirst()).trimmingCharacters(in: .whitespaces)
                if case .quote(let previous)? = blocks.last {
                    blocks[blocks.count - 1] = .quote(previous + "\n" + text)
                } else {
                    blocks.append(.quote(text))
                }
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

    /// The blocks to show before "Show more": whole blocks up to about `limit` characters, so
    /// a cut never lands inside a quote or list item. The first block is cut only when it alone
    /// is longer than the limit. Returns nil when everything fits.
    static func preview(_ blocks: [RichBlock], limit: Int = 1000) -> [RichBlock]? {
        var used = 0
        var shown: [RichBlock] = []
        for block in blocks {
            let length = block.textLength
            if used + length > limit {
                if shown.isEmpty, case .paragraph(let text) = block {
                    shown.append(.paragraph(String(text.prefix(limit)) + "…"))
                } else if shown.isEmpty {
                    shown.append(block)
                }
                return shown
            }
            used += length
            shown.append(block)
        }
        return nil
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

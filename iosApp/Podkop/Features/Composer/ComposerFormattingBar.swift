import SwiftUI

/// Compact Markdown tools that operate on the text editor's current selection.
struct ComposerFormattingBar: View {
    @Binding var text: String
    @Binding var selection: NSRange
    var disabled = false

    var body: some View {
        ScrollView(.horizontal) {
            HStack(spacing: 8) {
                tool(.composerBold, icon: "bold", prefix: "**", suffix: "**", placeholder: String(localized: .composerComposerBoldPlaceholder))
                tool(.composerItalic, icon: "italic", prefix: "__", suffix: "__", placeholder: String(localized: .composerComposerItalicPlaceholder))
                tool(.commonLink, icon: "link", prefix: "[", suffix: "](url)", placeholder: String(localized: .composerComposerLinkDescriptionPlaceholder))
                tool(.composerQuote, icon: "text.quote", prefix: ">", suffix: "", placeholder: String(localized: .composerComposerQuotePlaceholder))
                tool(.composerCode, icon: "chevron.left.forwardslash.chevron.right", prefix: "`", suffix: "`", placeholder: String(localized: .composerComposerCodePlaceholder))
                Button { insertSpoiler() } label: { Image(systemName: "eye.slash").frame(width: 44, height: 44) }
                    .accessibilityLabel(.composerSpoiler)
            }
        }
        .scrollIndicators(.hidden)
        .buttonStyle(.plain)
        .font(.body.weight(.semibold))
        .disabled(disabled)
    }

    private func tool(_ title: LocalizedStringResource, icon: String, prefix: String, suffix: String,
                      placeholder: String) -> some View {
        Button {
            let source = text as NSString
            guard selection.location <= source.length,
                  selection.length <= source.length - selection.location else { return }
            let selected = source.substring(with: selection)
            let middle = selected.isEmpty ? placeholder : selected
            text = source.replacingCharacters(in: selection, with: prefix + middle + suffix)
            selection = NSRange(location: selection.location + prefix.utf16.count, length: middle.utf16.count)
        } label: {
            Image(systemName: icon).frame(width: 44, height: 44)
        }
        .accessibilityLabel(title)
    }

    private func insertSpoiler() {
        let source = text as NSString
        guard selection.location <= source.length else { return }
        let preceding = source.substring(to: selection.location) as NSString
        let newline = preceding.range(of: "\n", options: .backwards)
        let start = newline.location == NSNotFound ? 0 : newline.location + 1
        guard start == source.length || source.substring(with: NSRange(location: start, length: 1)) != "!" else { return }
        text = source.replacingCharacters(in: NSRange(location: start, length: 0), with: "!")
        selection.location += 1
    }
}

import SwiftUI

/// Stands in for the body of an entry or comment from a blacklisted user until the reader asks
/// to see it (Android's `BlacklistedContentGate`). The author, time and votes stay visible.
struct BlacklistedContentNotice: View {
    let reveal: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(.contentCommentLabelBlacklistedUser)
                .font(.footnote)
                .foregroundStyle(.secondary)
            Button(action: reveal) {
                Text(.contentCommentButtonShowBlacklistedContent)
                    .font(.footnote.weight(.semibold))
            }
            .buttonStyle(.bordered)
            .controlSize(.small)
            .accessibilityIdentifier("showBlacklistedContent")
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

import SwiftUI

/// Link vote badge: the outlined count Wykop shows beside a link title, with a flame when hot.
/// Tapping digs or undigs when the viewer may vote (Android's `Count`).
struct LinkVoteBadge: View {
    let vote: NativeVote
    let hot: Bool
    var pending = false
    var action: (() -> Void)?

    private var voted: Bool { vote.state == "positive" }
    private var enabled: Bool { action != nil && !pending }

    var body: some View {
        Button { action?() } label: {
            Text(vote.up.formatted())
                .font(.subheadline.weight(.bold).monospacedDigit())
                .minimumScaleFactor(0.6)
                .lineLimit(1)
                .foregroundStyle(hot ? WykopTheme.hotOrange : .primary)
                .frame(width: 48, height: 32)
                .background(voted ? WykopTheme.votePositive.opacity(0.18) : .clear,
                            in: RoundedRectangle(cornerRadius: 12, style: .continuous))
                .overlay(RoundedRectangle(cornerRadius: 12, style: .continuous)
                    .strokeBorder(voted ? WykopTheme.votePositive : Color.secondary.opacity(0.6), lineWidth: 2))
                .overlay(alignment: .bottomTrailing) {
                    if hot {
                        Image(systemName: "flame.fill")
                            .font(.system(size: 14))
                            .foregroundStyle(WykopTheme.hotOrange)
                            .offset(x: 6, y: 6)
                    }
                }
                .opacity(pending ? 0.5 : 1)
        }
        .buttonStyle(.plain)
        .disabled(!enabled)
        .accessibilityLabel(String(localized: "Votes: \(vote.up)"))
        .accessibilityValue(voted ? String(localized: "Dug") : "")
        .accessibilityHint(enabled ? String(localized: voted ? "Removes your vote" : "Digs this link") : "")
        .accessibilityIdentifier("voteBadge")
    }
}

/// Entry and comment votes: the signed score and outlined plus (and, for link comments, minus)
/// buttons that fill when voted (Android's `Vote`).
struct ScoreVoteControl: View {
    let vote: NativeVote
    var showsDown = false
    var pending = false
    var up: (() -> Void)?
    var down: (() -> Void)?

    private var score: Int { vote.up - vote.down }

    var body: some View {
        HStack(spacing: 8) {
            Text(score > 0 ? "+\(score)" : "\(score)")
                .font(.subheadline.weight(.semibold).monospacedDigit())
                .foregroundStyle(score > 0 ? WykopTheme.votePositive : score < 0 ? WykopTheme.voteNegative : .primary)
                .accessibilityLabel(String(localized: "Score \(score)"))
            if let up {
                voteButton(symbol: "plus", color: WykopTheme.votePositive, active: vote.state == "positive",
                           label: vote.state == "positive" ? "Remove upvote" : "Upvote", action: up)
                    .accessibilityIdentifier("voteUp")
            }
            if showsDown, let down {
                voteButton(symbol: "minus", color: WykopTheme.voteNegative, active: vote.state == "negative",
                           label: vote.state == "negative" ? "Remove downvote" : "Downvote", action: down)
                    .accessibilityIdentifier("voteDown")
            }
        }
        .opacity(pending ? 0.5 : 1)
        .disabled(pending)
    }

    private func voteButton(symbol: String, color: Color, active: Bool, label: LocalizedStringKey,
                            action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: symbol)
                .font(.system(size: 12, weight: .bold))
                .foregroundStyle(active ? Color.white : color)
                .frame(width: 26, height: 26)
                .background(active ? color : .clear, in: RoundedRectangle(cornerRadius: 6, style: .continuous))
                .overlay(RoundedRectangle(cornerRadius: 6, style: .continuous).strokeBorder(color, lineWidth: 1.5))
                .contentShape(Rectangle().inset(by: -8))
        }
        .buttonStyle(.plain)
        .accessibilityLabel(label)
    }
}

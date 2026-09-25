import SwiftUI

/// Link vote badge: the outlined count Wykop shows beside a link title (Android's `Count`).
/// Three looks, as on Android: regular, hot (orange count with a flame) and voted (both faded to
/// 60%). When the viewer cannot vote the badge looks the same but ignores taps.
struct LinkVoteBadge: View {
    let vote: Vote
    let hot: Bool
    var pending = false
    var action: (() -> Void)?

    private var voted: Bool { vote.state != "none" }
    private var enabled: Bool { action != nil && !pending }

    var body: some View {
        Text(vote.up.formatted())
            .font(.subheadline.weight(.semibold).monospacedDigit())
            .minimumScaleFactor(0.6)
            .lineLimit(1)
            .foregroundStyle((hot ? WykopTheme.hotOrange : Color.primary).opacity(voted ? 0.6 : 1))
            .frame(width: 46, height: 32)
            .overlay(RoundedRectangle(cornerRadius: 12, style: .continuous)
                .strokeBorder(Color.secondary.opacity(voted ? 0.6 : 1), lineWidth: 2))
            .overlay(alignment: .bottomTrailing) {
                if hot {
                    Image(systemName: "flame.fill")
                        .font(.system(size: 18))
                        .foregroundStyle(WykopTheme.hotOrange)
                        .offset(x: 7, y: 7)
                        .accessibilityHidden(true)
                }
            }
            .opacity(pending ? 0.5 : 1)
            .contentShape(RoundedRectangle(cornerRadius: 12))
            .onTapGesture { if enabled { action?() } }
            .accessibilityElement(children: .ignore)
            .accessibilityLabel(String(localized: .commonVotes(vote.up)))
            .accessibilityValue(voted ? String(localized: .commonDug) : "")
            .accessibilityHint(enabled ? String(localized: voted ? .contentRemovesVote : .contentDigsLink) : "")
            .accessibilityAddTraits(enabled ? .isButton : [])
            .accessibilityIdentifier("voteBadge")
    }
}

/// Entry and comment votes: the signed score and outlined plus (and, for link comments, minus)
/// buttons that fill when voted (Android's `Vote`).
struct ScoreVoteControl: View {
    let vote: Vote
    var showsDown = false
    var pending = false
    var up: (() -> Void)?
    var down: (() -> Void)?

    private var score: Int { vote.up - vote.down }

    var body: some View {
        HStack(spacing: 8) {
            Text(verbatim: score > 0 ? "+\(score)" : "\(score)")
                .font(.subheadline.weight(.semibold).monospacedDigit())
                .foregroundStyle(score > 0 ? WykopTheme.votePositive : score < 0 ? WykopTheme.voteNegative : .primary)
                .accessibilityLabel(String(localized: .contentScore(score)))
            if let up {
                voteButton(symbol: "plus", color: WykopTheme.votePositive, active: vote.state == "positive",
                           label: vote.state == "positive" ? .contentRemoveUpvote : .contentUpvote, action: up)
                    .accessibilityIdentifier("voteUp")
            }
            if showsDown, let down {
                voteButton(symbol: "minus", color: WykopTheme.voteNegative, active: vote.state == "negative",
                           label: vote.state == "negative" ? .contentRemoveDownvote : .contentDownvote, action: down)
                    .accessibilityIdentifier("voteDown")
            }
        }
        .opacity(pending ? 0.5 : 1)
        .disabled(pending)
    }

    private func voteButton(symbol: String, color: Color, active: Bool, label: LocalizedStringResource,
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

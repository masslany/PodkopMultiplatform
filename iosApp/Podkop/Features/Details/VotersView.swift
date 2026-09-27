import SwiftUI

/// Who plussed an entry or comment, or dug or buried a link (Android's
/// `ResourceVotesBottomSheet`): a sheet with a drag handle and one card per voter, with the
/// avatar and gender bar and the name in its rank color. Buried links also show the reason.
struct VotersSheet: View {
    let target: VoterTarget
    let dependencies: AppDependencies
    /// Opens a voter's profile; the presenter closes its own sheets first.
    let openProfile: (String) -> Void
    @State private var model: VoterModel

    init(target: VoterTarget, dependencies: AppDependencies, openProfile: @escaping (String) -> Void) {
        self.target = target
        self.dependencies = dependencies
        self.openProfile = openProfile
        _model = State(initialValue: VoterModel(target: target, loader: dependencies.voterLoader))
    }

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 8) {
                ForEach(model.voters) { voter in
                    Button { openProfile(voter.username) } label: {
                        row(voter)
                    }
                    .buttonStyle(.plain)
                    .onAppear { if voter.username == model.voters.last?.username { model.load() } }
                }
                if model.loading {
                    ProgressView().frame(maxWidth: .infinity).padding(.vertical, 12)
                }
                if model.failed {
                    ThreadMoreButton(title: String(localized: .commonRetry)) { model.load() }
                        .padding(.vertical, 8)
                }
                if model.exhausted && model.voters.isEmpty {
                    Text(emptyTitle)
                        .foregroundStyle(.secondary)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .podkopCard(padding: 16)
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 28)
            .padding(.bottom, 16)
        }
        .background(PodkopTheme.background.ignoresSafeArea())
        .presentationDragIndicator(.visible)
        .presentationDetents([.medium, .large])
        .presentationBackground(PodkopTheme.background)
        .task { model.load() }
        .onDisappear { model.stop() }
        .accessibilityIdentifier("votersSheet")
    }

    private func row(_ voter: Voter) -> some View {
        HStack(spacing: 14) {
            AvatarView(url: voter.avatarURL, name: voter.username, size: 40, gender: voter.gender)
            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: 4) {
                    Text(voter.username)
                        .font(.body.weight(.semibold))
                        .foregroundStyle(authorColor(voter.color))
                    if voter.verified {
                        Image(systemName: "checkmark.seal.fill")
                            .font(.caption)
                            .foregroundStyle(PodkopTheme.tagBlue)
                            .accessibilityLabel(.commonVerifiedAuthor)
                    }
                }
                if let reason = voter.reason.flatMap(Self.reasonTitle) {
                    Text(reason).font(.caption).foregroundStyle(.secondary)
                }
            }
            Spacer(minLength: 0)
            Image(systemName: "chevron.right")
                .font(.footnote.weight(.semibold))
                .foregroundStyle(.tertiary)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(PodkopTheme.card, in: RoundedRectangle(cornerRadius: PodkopTheme.cardRadius, style: .continuous))
        .contentShape(Rectangle())
    }

    private var emptyTitle: LocalizedStringResource {
        switch (target.kind, target.side) {
        case ("link", "down"): .detailsLinkDownvotesEmpty
        case ("link", _): .detailsLinkUpvotesEmpty
        default: .detailsVotesEmpty
        }
    }

    /// The API's reason names, worded like Android's `vote_reason_*` strings.
    static func reasonTitle(_ raw: String) -> String? {
        switch raw.lowercased() {
        case "duplicate": String(localized: .detailsVoteReasonDuplicate)
        case "spam": String(localized: .detailsVoteReasonSpam)
        case "fake": String(localized: .detailsVoteReasonFake)
        case "wrong": String(localized: .detailsVoteReasonWrong)
        case "invalid": String(localized: .detailsVoteReasonInvalid)
        default: nil
        }
    }
}

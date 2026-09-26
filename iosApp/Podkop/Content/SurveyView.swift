import SwiftUI

struct SurveyView: View {
    let survey: Survey
    var vote: ((Int) -> Void)?
    var pending = false
    @State private var showResults = false

    private var resultsVisible: Bool { survey.selectedOption != nil || showResults }

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(survey.question)
                .font(.subheadline.weight(.medium))
                .fixedSize(horizontal: false, vertical: true)
            ForEach(Array(survey.answers.enumerated()), id: \.element.id) { item in
                let answer = item.element
                if survey.canVote, survey.selectedOption == nil, let vote {
                    Button { vote(item.offset + 1) } label: { answerRow(answer) }
                        .buttonStyle(.plain)
                        .disabled(pending)
                        .accessibilityIdentifier("surveyAnswer-\(answer.id)")
                } else {
                    answerRow(answer).accessibilityElement(children: .combine)
                }
            }
            AdaptiveControlRow {
                Text(String(localized: .contentVotes) + ": \(survey.count)")
                    .font(.caption).foregroundStyle(.secondary)
                if pending { ProgressView().controlSize(.small) }
            } trailing: {
                if survey.selectedOption == nil {
                    Button { showResults.toggle() } label: {
                        ZStack {
                            Text(.contentShowResults).hidden()
                            Text(.contentHideResults).hidden()
                            Text(showResults ? .contentHideResults : .contentShowResults)
                        }
                    }
                        .font(.caption.weight(.medium))
                        .buttonStyle(.plain)
                        .frame(minHeight: 44)
                }
            }
        }
        .padding(12)
        .background(WykopTheme.cardInset, in: RoundedRectangle(cornerRadius: 12, style: .continuous))
    }

    private func answerRow(_ answer: Survey.Answer) -> some View {
        let selected = answer.selected
        return HStack(spacing: 12) {
            Text(answer.text)
                .font(.subheadline)
                .fixedSize(horizontal: false, vertical: true)
                .frame(maxWidth: .infinity, alignment: .leading)
            ZStack(alignment: .trailing) {
                // Keep the same width and font height in both modes, including multiline answers.
                HStack(spacing: 8) {
                    Image(systemName: "checkmark.circle.fill").font(.system(size: 22))
                    Text(verbatim: "100%").font(.subheadline.weight(.semibold)).monospacedDigit()
                }
                .hidden()
                if resultsVisible {
                    HStack(spacing: 8) {
                        if selected {
                            Image(systemName: "checkmark.circle.fill").font(.system(size: 22))
                                .foregroundStyle(WykopTheme.votePositive)
                                .accessibilityAddTraits(.isSelected)
                        }
                        Text(verbatim: "\(percentage(answer.count))%")
                            .font(.subheadline.weight(.semibold)).monospacedDigit()
                    }
                } else if survey.canVote, vote != nil {
                    Image(systemName: "circle").font(.system(size: 22))
                        .foregroundStyle(.secondary).accessibilityHidden(true)
                }
            }

        }
        .foregroundStyle(.primary)
        .padding(.horizontal, 12).padding(.vertical, 12)
        .frame(minHeight: 48)
        .background {
            GeometryReader { geometry in
                ZStack(alignment: .leading) {
                    WykopTheme.background
                    if resultsVisible {
                        (selected ? WykopTheme.votePositive.opacity(0.25) : Color.primary.opacity(0.08))
                            .frame(width: geometry.size.width * CGFloat(percentage(answer.count)) / 100)
                    }
                }
            }
        }
        .clipShape(RoundedRectangle(cornerRadius: 9, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: 9).strokeBorder(selected ? WykopTheme.votePositive : .clear))
        .contentShape(RoundedRectangle(cornerRadius: 9))
    }

    private func percentage(_ count: Int) -> Int {
        guard survey.count > 0 else { return 0 }
        return min(100, Int((Double(max(0, count)) / Double(survey.count) * 100).rounded()))
    }
}

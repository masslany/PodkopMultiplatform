import SwiftUI

struct SurveyView: View {
    let survey: Survey
    var vote: ((Int) -> Void)?
    @State private var showResults = false

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(survey.question).font(.subheadline.bold())
            ForEach(Array(survey.answers.enumerated()), id: \.element.id) { item in
                let index = item.offset + 1
                let answer = item.element
                if survey.selectedOption != nil || showResults {
                    VStack(alignment: .leading, spacing: 3) {
                        HStack {
                            Text(answer.text)
                            if answer.selected { Image(systemName: "checkmark.circle.fill") }
                            Spacer()
                            Text(verbatim: "\(percentage(answer.count))%")
                        }
                        ProgressView(value: Double(percentage(answer.count)), total: 100)
                    }
                    .accessibilityElement(children: .combine)
                } else if survey.canVote, let vote {
                    Button(answer.text) { vote(index) }.buttonStyle(.bordered)
                } else {
                    Text(answer.text)
                }
            }
            HStack {
                Text(String(localized: .contentVotes) + ": \(survey.count)")
                    .font(.caption).foregroundStyle(.secondary)
                if survey.selectedOption == nil {
                    Button(showResults ? .contentHideResults : .contentShowResults) { showResults.toggle() }
                        .font(.caption)
                }
            }
        }
        .padding(10)
        .background(WykopTheme.cardInset, in: RoundedRectangle(cornerRadius: WykopTheme.smallRadius, style: .continuous))
    }

    private func percentage(_ count: Int) -> Int {
        guard survey.count > 0 else { return 0 }
        return Int((Double(max(0, count)) / Double(survey.count) * 100).rounded())
    }
}

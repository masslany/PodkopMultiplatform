package pl.masslany.podkop.common.models.survey

import kotlin.math.roundToInt
import kotlinx.collections.immutable.toImmutableList
import pl.masslany.podkop.business.common.domain.models.common.Answer
import pl.masslany.podkop.business.common.domain.models.common.Survey

internal fun Survey?.toSurveyState(): SurveyState? {
    val survey = this ?: return null
    if (survey.answers.isEmpty()) return null

    val totalCount = survey.count.coerceAtLeast(0)
    val votedOptionNumber = survey.toVotedOptionNumber()

    return SurveyState(
        question = survey.question,
        answers = survey.answers
            .mapIndexed { index, answer ->
                answer.toAnswerState(
                    totalCount = totalCount,
                    isSelected = votedOptionNumber == index + 1,
                )
            }
            .toImmutableList(),
        count = totalCount,
        canVote = survey.actions.vote && votedOptionNumber == null,
        votedOptionNumber = votedOptionNumber,
    )
}

private fun Survey.toVotedOptionNumber(): Int? {
    val votedIndex = answers.indexOfFirst { it.voted > 0 }
    return votedIndex
        .takeIf { it >= 0 }
        ?.plus(1)
        ?: voted.takeIf { it in 1..answers.size }
}

private fun Answer.toAnswerState(
    totalCount: Int,
    isSelected: Boolean,
): AnswerState {
    val safeCount = count.coerceAtLeast(0)
    val percentageValue = if (totalCount > 0) {
        (safeCount.toFloat() / totalCount * 100f).roundToInt()
    } else {
        0
    }

    return AnswerState(
        isSelected = isSelected,
        text = text,
        count = safeCount,
        percentageFraction = if (totalCount > 0) {
            (safeCount.toFloat() / totalCount).coerceIn(0f, 1f)
        } else {
            0f
        },
        percentage = percentageValue.toString(),
    )
}

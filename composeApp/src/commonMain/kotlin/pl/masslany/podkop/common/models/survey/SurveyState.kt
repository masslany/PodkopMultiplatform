package pl.masslany.podkop.common.models.survey

import kotlin.math.roundToInt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class SurveyState(
    val question: String,
    val answers: ImmutableList<AnswerState>,
    val count: Int,
    val canVote: Boolean = false,
    val votedOptionNumber: Int? = null,
) {
    val hasVoted: Boolean
        get() = votedOptionNumber != null

    val isResultsToggleVisible: Boolean
        get() = !hasVoted

    val areResultsVisibleByDefault: Boolean
        get() = hasVoted

    val isVoteActionEnabled: Boolean
        get() = canVote && !hasVoted
}

fun SurveyState.registerVote(optionNumber: Int): SurveyState {
    if (hasVoted) return this
    if (optionNumber !in 1..answers.size) return this

    val updatedTotalCount = count.coerceAtLeast(0) + 1
    val updatedAnswers = answers
        .mapIndexed { index, answer ->
            val isSelected = index + 1 == optionNumber
            val updatedCount = answer.count.coerceAtLeast(0) + if (isSelected) 1 else 0

            answer.withResults(
                count = updatedCount,
                totalCount = updatedTotalCount,
                isSelected = isSelected,
            )
        }
        .toImmutableList()

    return copy(
        answers = updatedAnswers,
        count = updatedTotalCount,
        canVote = false,
        votedOptionNumber = optionNumber,
    )
}

private fun AnswerState.withResults(
    count: Int,
    totalCount: Int,
    isSelected: Boolean,
): AnswerState {
    val percentageFraction = if (totalCount > 0) {
        (count.toFloat() / totalCount).coerceIn(0f, 1f)
    } else {
        0f
    }

    return copy(
        isSelected = isSelected,
        count = count,
        percentageFraction = percentageFraction,
        percentage = (percentageFraction * 100f).roundToInt().toString(),
    )
}

package pl.masslany.podkop.common.models.survey

import kotlin.math.roundToInt
import kotlinx.collections.immutable.toImmutableList
import pl.masslany.podkop.business.common.domain.models.common.Answer
import pl.masslany.podkop.business.common.domain.models.common.Survey

internal fun Survey?.toSurveyState(): SurveyState? {
    val survey = this ?: return null
    if (survey.answers.isEmpty()) return null

    val totalCount = survey.count.coerceAtLeast(0)

    return SurveyState(
        question = survey.question,
        answers = survey.answers
            .map { it.toAnswerState(totalCount) }
            .toImmutableList(),
        count = totalCount,
    )
}

private fun Answer.toAnswerState(totalCount: Int): AnswerState {
    val safeCount = count.coerceAtLeast(0)
    val percentageValue = if (totalCount > 0) {
        (safeCount.toFloat() / totalCount * 100f).roundToInt()
    } else {
        0
    }

    return AnswerState(
        isSelected = voted > 0,
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

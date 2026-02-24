package pl.masslany.podkop.common.models.survey

import kotlinx.collections.immutable.ImmutableList

data class SurveyState(
    val question: String,
    val answers: ImmutableList<AnswerState>,
    val count: Int,
)

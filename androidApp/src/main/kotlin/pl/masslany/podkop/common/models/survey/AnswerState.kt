package pl.masslany.podkop.common.models.survey

data class AnswerState(
    val isSelected: Boolean,
    val text: String,
    val count: Int,
    val percentageFraction: Float,
    val percentage: String,
)

package pl.masslany.podkop.business.common.data.network.models.entries

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.data.network.models.common.ActionsDto

@Serializable
data class SurveyDto(
    @SerialName("actions")
    val actions: ActionsDto,
    @SerialName("answers")
    val answers: List<AnswerDto>,
    @SerialName("count")
    val count: Int,
    @SerialName("deletable")
    val deletable: Boolean,
    @SerialName("editable")
    val editable: Boolean,
    @SerialName("key")
    val key: String,
    @SerialName("question")
    val question: String,
    @SerialName("voted")
    val voted: Int,
)

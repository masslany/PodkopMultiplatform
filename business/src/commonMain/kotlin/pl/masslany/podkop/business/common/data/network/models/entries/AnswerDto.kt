package pl.masslany.podkop.business.common.data.network.models.entries

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnswerDto(
    @SerialName("count")
    val count: Int,
    @SerialName("id")
    val id: Int,
    @SerialName("text")
    val text: String,
    @SerialName("voted")
    val voted: Int,
)

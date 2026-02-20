package pl.masslany.podkop.business.tags.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TagsAutoCompleteResponseDto(
    @SerialName("data")
    val data: List<TagsAutoCompleteDataDto>,
)

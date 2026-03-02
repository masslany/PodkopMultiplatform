package pl.masslany.podkop.business.favourites.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavouriteRequestDto(
    @SerialName("data")
    val data: FavouriteRequestDataDto,
)

@Serializable
data class FavouriteRequestDataDto(
    @SerialName("type")
    val type: String,
    @SerialName("source_id")
    val sourceId: Int,
)

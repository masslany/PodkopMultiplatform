package pl.masslany.podkop.business.profile.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EntriesDetailsDto(
    @SerialName("added")
    val added: Int,
    @SerialName("commented")
    val commented: Int,
    @SerialName("voted")
    val voted: Int,
)

package pl.masslany.podkop.business.profile.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LinksDetailsDto(
    @SerialName("added")
    val added: Int,
    @SerialName("commented")
    val commented: Int,
    @SerialName("down")
    val down: Int? = null,
    @SerialName("published")
    val published: Int,
    @SerialName("related")
    val related: Int,
    @SerialName("up")
    val up: Int,
)

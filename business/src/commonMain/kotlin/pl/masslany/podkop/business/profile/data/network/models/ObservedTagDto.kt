package pl.masslany.podkop.business.profile.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ObservedTagDto(
    @SerialName("name")
    val name: String,
    @SerialName("pinned")
    val pinned: Boolean,
)

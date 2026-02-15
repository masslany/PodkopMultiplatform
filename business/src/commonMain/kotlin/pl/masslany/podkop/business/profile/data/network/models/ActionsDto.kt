package pl.masslany.podkop.business.profile.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActionsDto(
    @SerialName("blacklist")
    val blacklist: Boolean,
    @SerialName("follow")
    val follow: Boolean,
    @SerialName("update")
    val update: Boolean,
    @SerialName("update_gender")
    val updateGender: Boolean,
    @SerialName("update_note")
    val updateNote: Boolean,
)

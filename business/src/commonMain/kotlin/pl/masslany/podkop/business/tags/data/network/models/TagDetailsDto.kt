package pl.masslany.podkop.business.tags.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.data.network.models.common.MediaDto

@Serializable
data class TagDetailsDto(
    @SerialName("name")
    val name: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("followers")
    val followers: Int? = null,
    @SerialName("media")
    val media: MediaDto? = null,
    @SerialName("follow")
    val follow: Boolean? = null,
    @SerialName("notifications")
    val notifications: Boolean? = null,
    @SerialName("actions")
    val actions: TagDetailsActionsDto? = null,
)

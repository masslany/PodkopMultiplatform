package pl.masslany.podkop.business.tags.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TagDetailsActionsDto(
    @SerialName("report")
    val report: Boolean? = null,
    @SerialName("update")
    val update: Boolean? = null,
    @SerialName("create_coauthor")
    val createCoauthor: Boolean? = null,
    @SerialName("delete_coauthor")
    val deleteCoauthor: Boolean? = null,
    @SerialName("blacklist")
    val blacklist: Boolean? = null,
)

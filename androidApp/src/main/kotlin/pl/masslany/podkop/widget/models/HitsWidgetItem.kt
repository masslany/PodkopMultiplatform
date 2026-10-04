package pl.masslany.podkop.widget.models

import kotlinx.serialization.Serializable

@Serializable
data class HitsWidgetItem(
    val id: Int,
    val title: String,
    val votes: Int,
    val isHot: Boolean = false,
    val comments: Int,
    val source: String? = null,
    val thumbnailUrl: String? = null,
)

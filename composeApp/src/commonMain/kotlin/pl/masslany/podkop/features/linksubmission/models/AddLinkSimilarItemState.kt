package pl.masslany.podkop.features.linksubmission.models

import kotlinx.serialization.Serializable

@Serializable
data class AddLinkSimilarItemState(
    val id: Int,
    val title: String,
    val digCount: Int,
    val createdAt: String,
    val sourceLabel: String,
)

package pl.masslany.podkop.business.tags.domain.models

import pl.masslany.podkop.business.common.domain.models.common.Media

data class TagDetails(
    val name: String,
    val description: String,
    val followers: Int,
    val media: Media?,
    val isObserved: Boolean,
    val areNotificationsEnabled: Boolean,
)

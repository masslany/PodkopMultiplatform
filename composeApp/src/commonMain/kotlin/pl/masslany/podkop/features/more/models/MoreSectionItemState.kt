package pl.masslany.podkop.features.more.models

import pl.masslany.podkop.features.more.MoreActions

data class MoreSectionItemState(
    val type: MoreSectionItemType,
    val badgeCount: Int,
    val onClick: MoreActions.() -> Unit,
)

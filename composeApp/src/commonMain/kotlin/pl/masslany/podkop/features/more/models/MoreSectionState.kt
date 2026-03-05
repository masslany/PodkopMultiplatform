package pl.masslany.podkop.features.more.models

import kotlinx.collections.immutable.ImmutableList

data class MoreSectionState(val type: MoreSectionType, val items: ImmutableList<MoreSectionItemState>)

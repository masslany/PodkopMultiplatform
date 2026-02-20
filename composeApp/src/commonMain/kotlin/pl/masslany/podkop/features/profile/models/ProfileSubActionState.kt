package pl.masslany.podkop.features.profile.models

import kotlinx.collections.immutable.ImmutableList

data class ProfileSubActionState(
    val items: ImmutableList<ProfileSubActionType>,
    val selected: ProfileSubActionType,
    val expanded: Boolean,
)

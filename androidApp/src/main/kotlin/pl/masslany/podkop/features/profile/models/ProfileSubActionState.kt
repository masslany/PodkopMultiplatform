package pl.masslany.podkop.features.profile.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ProfileSubActionState(
    val items: ImmutableList<ProfileSubActionType>,
    val selected: ProfileSubActionType,
    val expanded: Boolean,
) {
    companion object {
        val initial = ProfileSubActionState(
            items = persistentListOf(ProfileSubActionType.Actions),
            selected = ProfileSubActionType.Actions,
            expanded = false,
        )
    }
}

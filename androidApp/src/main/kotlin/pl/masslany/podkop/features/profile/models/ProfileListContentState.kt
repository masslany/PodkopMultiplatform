package pl.masslany.podkop.features.profile.models

import kotlinx.collections.immutable.ImmutableList
import pl.masslany.podkop.common.models.UserItemState
import pl.masslany.podkop.features.resources.models.ResourceItemState

sealed interface ProfileListContentState {
    data object Empty : ProfileListContentState

    data class Resources(val items: ImmutableList<ResourceItemState>) : ProfileListContentState

    data class ObservedUsers(val items: ImmutableList<UserItemState>) : ProfileListContentState

    data class ObservedTags(val items: ImmutableList<ProfileObservedTagItemState>) : ProfileListContentState
}

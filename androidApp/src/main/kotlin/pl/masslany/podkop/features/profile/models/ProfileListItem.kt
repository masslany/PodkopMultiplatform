package pl.masslany.podkop.features.profile.models

import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.profile.domain.models.ObservedTag
import pl.masslany.podkop.business.profile.domain.models.ObservedUser

sealed interface ProfileListItem {
    data class Resource(val item: ResourceItem) : ProfileListItem
    data class ObservedUserItem(val user: ObservedUser) : ProfileListItem
    data class ObservedTagItem(val tag: ObservedTag) : ProfileListItem
}

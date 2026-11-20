package pl.masslany.podkop.features.profile.models

import pl.masslany.podkop.business.common.domain.models.common.PaginatedData
import pl.masslany.podkop.business.common.domain.models.common.Pagination

data class ProfileListItemsPage(override val data: List<ProfileListItem>, override val pagination: Pagination?) :
    PaginatedData<ProfileListItem>

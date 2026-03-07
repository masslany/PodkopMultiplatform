package pl.masslany.podkop.business.privatemessages.domain.models

import pl.masslany.podkop.business.common.domain.models.common.PaginatedData
import pl.masslany.podkop.business.common.domain.models.common.Pagination

data class PrivateMessageThreadPage(
    override val data: List<PrivateMessage>,
    override val pagination: Pagination?,
) : PaginatedData<PrivateMessage>

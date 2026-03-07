package pl.masslany.podkop.business.privatemessages.domain.models

import pl.masslany.podkop.business.common.domain.models.common.PaginatedData
import pl.masslany.podkop.business.common.domain.models.common.Pagination

data class PrivateMessagesPage(
    override val data: List<PrivateMessageConversation>,
    override val pagination: Pagination?,
) : PaginatedData<PrivateMessageConversation>

package pl.masslany.podkop.business.privatemessages.data.network.api

import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessagesListDto

interface PrivateMessagesApi {
    suspend fun getConversations(page: Any? = null): Result<PrivateMessagesListDto>
}

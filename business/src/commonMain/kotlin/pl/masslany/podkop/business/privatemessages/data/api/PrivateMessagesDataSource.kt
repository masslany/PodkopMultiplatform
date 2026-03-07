package pl.masslany.podkop.business.privatemessages.data.api

import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessagesListDto

interface PrivateMessagesDataSource {
    suspend fun getConversations(page: Any? = null): Result<PrivateMessagesListDto>
}

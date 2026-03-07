package pl.masslany.podkop.business.privatemessages.data.network.main

import pl.masslany.podkop.business.privatemessages.data.api.PrivateMessagesDataSource
import pl.masslany.podkop.business.privatemessages.data.network.api.PrivateMessagesApi
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessagesListDto

class PrivateMessagesDataSourceImpl(
    private val privateMessagesApi: PrivateMessagesApi,
) : PrivateMessagesDataSource {
    override suspend fun getConversations(page: Any?): Result<PrivateMessagesListDto> {
        return privateMessagesApi.getConversations(page = page)
    }
}

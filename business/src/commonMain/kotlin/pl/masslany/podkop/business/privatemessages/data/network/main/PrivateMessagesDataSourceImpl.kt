package pl.masslany.podkop.business.privatemessages.data.network.main

import pl.masslany.podkop.business.privatemessages.data.api.PrivateMessagesDataSource
import pl.masslany.podkop.business.privatemessages.data.network.api.PrivateMessagesApi
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageItemResponseDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageThreadDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessagesListDto

class PrivateMessagesDataSourceImpl(
    private val privateMessagesApi: PrivateMessagesApi,
) : PrivateMessagesDataSource {
    override suspend fun getConversations(page: Any?): Result<PrivateMessagesListDto> {
        return privateMessagesApi.getConversations(page = page)
    }

    override suspend fun getConversationMessages(
        username: String,
        page: Any?,
    ): Result<PrivateMessageThreadDto> {
        return privateMessagesApi.getConversationMessages(
            username = username,
            page = page,
        )
    }

    override suspend fun getConversationMessagesNewer(username: String): Result<PrivateMessageThreadDto> {
        return privateMessagesApi.getConversationMessagesNewer(username = username)
    }

    override suspend fun openConversation(
        username: String,
        content: String,
        adult: Boolean,
        photoKey: String?,
        embed: String?,
    ): Result<PrivateMessageItemResponseDto> {
        return privateMessagesApi.openConversation(
            username = username,
            content = content,
            adult = adult,
            photoKey = photoKey,
            embed = embed,
        )
    }

    override suspend fun readAll(): Result<Unit> {
        return privateMessagesApi.readAll()
    }
}

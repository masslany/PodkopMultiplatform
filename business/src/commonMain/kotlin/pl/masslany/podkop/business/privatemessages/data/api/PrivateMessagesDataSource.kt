package pl.masslany.podkop.business.privatemessages.data.api

import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageItemResponseDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageThreadDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessagesListDto

interface PrivateMessagesDataSource {
    suspend fun getConversations(page: Any? = null): Result<PrivateMessagesListDto>

    suspend fun getConversationMessages(
        username: String,
        page: Any? = null,
    ): Result<PrivateMessageThreadDto>

    suspend fun getConversationMessagesNewer(username: String): Result<PrivateMessageThreadDto>

    suspend fun openConversation(
        username: String,
        content: String,
        adult: Boolean = false,
        photoKey: String? = null,
        embed: String? = null,
    ): Result<PrivateMessageItemResponseDto>

    suspend fun readAll(): Result<Unit>
}

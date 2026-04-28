package pl.masslany.podkop.business.privatemessages.data.network.api

import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageItemResponseDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageThreadDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessagesListDto

interface PrivateMessagesApi {
    suspend fun getConversations(page: Int = 1): Result<PrivateMessagesListDto>

    suspend fun getConversationMessages(
        username: String,
        page: Int = 1,
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

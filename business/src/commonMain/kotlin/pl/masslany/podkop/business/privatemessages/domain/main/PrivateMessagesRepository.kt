package pl.masslany.podkop.business.privatemessages.domain.main

import pl.masslany.podkop.business.privatemessages.domain.models.PrivateMessage
import pl.masslany.podkop.business.privatemessages.domain.models.PrivateMessageThreadPage
import pl.masslany.podkop.business.privatemessages.domain.models.PrivateMessagesPage

interface PrivateMessagesRepository {
    suspend fun getConversations(page: Any? = null): Result<PrivateMessagesPage>

    suspend fun getConversationMessages(
        username: String,
        page: Any? = null,
    ): Result<PrivateMessageThreadPage>

    suspend fun getConversationMessagesNewer(username: String): Result<PrivateMessageThreadPage>

    suspend fun openConversation(
        username: String,
        content: String,
        adult: Boolean = false,
        photoKey: String? = null,
        embed: String? = null,
    ): Result<PrivateMessage>

    suspend fun readAll(): Result<Unit>
}

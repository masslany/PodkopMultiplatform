package pl.masslany.podkop.business.privatemessages.data.main

import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.privatemessages.data.api.PrivateMessagesDataSource
import pl.masslany.podkop.business.privatemessages.domain.models.PrivateMessage
import pl.masslany.podkop.business.privatemessages.domain.models.PrivateMessageThreadPage
import pl.masslany.podkop.business.privatemessages.domain.main.PrivateMessagesRepository
import pl.masslany.podkop.business.privatemessages.domain.models.PrivateMessagesPage
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider

class PrivateMessagesRepositoryImpl(
    private val privateMessagesDataSource: PrivateMessagesDataSource,
    private val dispatcherProvider: DispatcherProvider,
) : PrivateMessagesRepository {
    override suspend fun getConversations(page: Int): Result<PrivateMessagesPage> =
        withContext(dispatcherProvider.io) {
            privateMessagesDataSource.getConversations(page = page)
                .mapCatching { it.toPrivateMessagesPage() }
        }

    override suspend fun getConversationMessages(
        username: String,
        page: Int,
    ): Result<PrivateMessageThreadPage> = withContext(dispatcherProvider.io) {
        privateMessagesDataSource.getConversationMessages(
            username = username,
            page = page,
        ).mapCatching { it.toPrivateMessageThreadPage() }
    }

    override suspend fun getConversationMessagesNewer(
        username: String,
    ): Result<PrivateMessageThreadPage> = withContext(dispatcherProvider.io) {
        privateMessagesDataSource.getConversationMessagesNewer(username = username)
            .mapCatching { it.toPrivateMessageThreadPage() }
    }

    override suspend fun openConversation(
        username: String,
        content: String,
        adult: Boolean,
        photoKey: String?,
        embed: String?,
    ): Result<PrivateMessage> = withContext(dispatcherProvider.io) {
        privateMessagesDataSource.openConversation(
            username = username,
            content = content,
            adult = adult,
            photoKey = photoKey,
            embed = embed,
        ).mapCatching { it.toPrivateMessage() }
    }

    override suspend fun readAll(): Result<Unit> = withContext(dispatcherProvider.io) {
        privateMessagesDataSource.readAll()
    }
}

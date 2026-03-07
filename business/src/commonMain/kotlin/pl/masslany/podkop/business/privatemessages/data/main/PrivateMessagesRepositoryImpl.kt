package pl.masslany.podkop.business.privatemessages.data.main

import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.privatemessages.data.api.PrivateMessagesDataSource
import pl.masslany.podkop.business.privatemessages.domain.main.PrivateMessagesRepository
import pl.masslany.podkop.business.privatemessages.domain.models.PrivateMessagesPage
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider

class PrivateMessagesRepositoryImpl(
    private val privateMessagesDataSource: PrivateMessagesDataSource,
    private val dispatcherProvider: DispatcherProvider,
) : PrivateMessagesRepository {
    override suspend fun getConversations(page: Any?): Result<PrivateMessagesPage> =
        withContext(dispatcherProvider.io) {
            privateMessagesDataSource.getConversations(page = page)
                .mapCatching { it.toPrivateMessagesPage() }
        }
}

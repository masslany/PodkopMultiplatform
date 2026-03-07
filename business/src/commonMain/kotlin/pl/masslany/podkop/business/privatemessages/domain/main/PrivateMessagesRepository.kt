package pl.masslany.podkop.business.privatemessages.domain.main

import pl.masslany.podkop.business.privatemessages.domain.models.PrivateMessagesPage

interface PrivateMessagesRepository {
    suspend fun getConversations(page: Any? = null): Result<PrivateMessagesPage>
}

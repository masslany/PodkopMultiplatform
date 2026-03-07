package pl.masslany.podkop.business.testsupport.fakes

import pl.masslany.podkop.business.privatemessages.data.api.PrivateMessagesDataSource
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageItemResponseDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessageThreadDto
import pl.masslany.podkop.business.privatemessages.data.network.models.PrivateMessagesListDto

class FakePrivateMessagesDataSource : PrivateMessagesDataSource {
    data class GetConversationMessagesCall(
        val username: String,
        val page: Any?,
    )

    data class OpenConversationCall(
        val username: String,
        val content: String,
        val adult: Boolean,
        val photoKey: String?,
        val embed: String?,
    )

    var getConversationsResult: Result<PrivateMessagesListDto> =
        unstubbedResult("PrivateMessagesDataSource.getConversations")
    var getConversationMessagesResult: Result<PrivateMessageThreadDto> =
        unstubbedResult("PrivateMessagesDataSource.getConversationMessages")
    var getConversationMessagesNewerResult: Result<PrivateMessageThreadDto> =
        unstubbedResult("PrivateMessagesDataSource.getConversationMessagesNewer")
    var openConversationResult: Result<PrivateMessageItemResponseDto> =
        unstubbedResult("PrivateMessagesDataSource.openConversation")
    var readAllResult: Result<Unit> = unstubbedResult("PrivateMessagesDataSource.readAll")

    val getConversationsCalls = mutableListOf<Any?>()
    val getConversationMessagesCalls = mutableListOf<GetConversationMessagesCall>()
    val getConversationMessagesNewerCalls = mutableListOf<String>()
    val openConversationCalls = mutableListOf<OpenConversationCall>()
    var readAllCalls = 0

    override suspend fun getConversations(page: Any?): Result<PrivateMessagesListDto> {
        getConversationsCalls += page
        return getConversationsResult
    }

    override suspend fun getConversationMessages(
        username: String,
        page: Any?,
    ): Result<PrivateMessageThreadDto> {
        getConversationMessagesCalls += GetConversationMessagesCall(
            username = username,
            page = page,
        )
        return getConversationMessagesResult
    }

    override suspend fun getConversationMessagesNewer(username: String): Result<PrivateMessageThreadDto> {
        getConversationMessagesNewerCalls += username
        return getConversationMessagesNewerResult
    }

    override suspend fun openConversation(
        username: String,
        content: String,
        adult: Boolean,
        photoKey: String?,
        embed: String?,
    ): Result<PrivateMessageItemResponseDto> {
        openConversationCalls += OpenConversationCall(
            username = username,
            content = content,
            adult = adult,
            photoKey = photoKey,
            embed = embed,
        )
        return openConversationResult
    }

    override suspend fun readAll(): Result<Unit> {
        readAllCalls += 1
        return readAllResult
    }
}

package pl.masslany.podkop.business.testsupport.fakes

import pl.masslany.podkop.common.pagination.PageRequest

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.tags.data.api.TagsDataSource
import pl.masslany.podkop.business.tags.data.network.models.TagDetailsResponseDto
import pl.masslany.podkop.business.tags.data.network.models.TagsAutoCompleteResponseDto

class FakeTagsDataSource : TagsDataSource {
    data class GetTagStreamCall(
        val tagName: String,
        val page: PageRequest,
        val limit: Int?,
        val sort: String,
        val type: String,
    )

    var getTagDetailsResult: Result<TagDetailsResponseDto> = unstubbedResult("TagsDataSource.getTagDetails")
    var observeTagResult: Result<Unit> = unstubbedResult("TagsDataSource.observeTag")
    var unobserveTagResult: Result<Unit> = unstubbedResult("TagsDataSource.unobserveTag")
    var enableTagNotificationsResult: Result<Unit> = unstubbedResult("TagsDataSource.enableTagNotifications")
    var disableTagNotificationsResult: Result<Unit> = unstubbedResult("TagsDataSource.disableTagNotifications")
    var getTagStreamResult: Result<ResourceResponseDto> = unstubbedResult("TagsDataSource.getTagStream")
    var getTagsAutoCompleteResult: Result<TagsAutoCompleteResponseDto> = unstubbedResult("TagsDataSource.getTagsAutoComplete")

    val getTagDetailsCalls = mutableListOf<String>()
    val observeTagCalls = mutableListOf<String>()
    val unobserveTagCalls = mutableListOf<String>()
    val enableTagNotificationsCalls = mutableListOf<String>()
    val disableTagNotificationsCalls = mutableListOf<String>()
    val getTagStreamCalls = mutableListOf<GetTagStreamCall>()
    val getTagsAutoCompleteCalls = mutableListOf<String>()

    override suspend fun getTagDetails(tagName: String): Result<TagDetailsResponseDto> {
        getTagDetailsCalls += tagName
        return getTagDetailsResult
    }

    override suspend fun observeTag(tagName: String): Result<Unit> {
        observeTagCalls += tagName
        return observeTagResult
    }

    override suspend fun unobserveTag(tagName: String): Result<Unit> {
        unobserveTagCalls += tagName
        return unobserveTagResult
    }

    override suspend fun enableTagNotifications(tagName: String): Result<Unit> {
        enableTagNotificationsCalls += tagName
        return enableTagNotificationsResult
    }

    override suspend fun disableTagNotifications(tagName: String): Result<Unit> {
        disableTagNotificationsCalls += tagName
        return disableTagNotificationsResult
    }

    override suspend fun getTagStream(
        tagName: String,
        page: PageRequest,
        limit: Int?,
        sort: String,
        type: String,
    ): Result<ResourceResponseDto> {
        getTagStreamCalls += GetTagStreamCall(tagName, page, limit, sort, type)
        return getTagStreamResult
    }

    override suspend fun getTagsAutoComplete(query: String): Result<TagsAutoCompleteResponseDto> {
        getTagsAutoCompleteCalls += query
        return getTagsAutoCompleteResult
    }
}

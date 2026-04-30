package pl.masslany.podkop.business.tags.data.network.main

import pl.masslany.podkop.common.pagination.PageRequest

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.tags.data.api.TagsDataSource
import pl.masslany.podkop.business.tags.data.network.api.TagsApi
import pl.masslany.podkop.business.tags.data.network.models.TagDetailsResponseDto
import pl.masslany.podkop.business.tags.data.network.models.TagsAutoCompleteResponseDto

class TagsDataSourceImpl(
    private val tagsApi: TagsApi,
) : TagsDataSource {
    override suspend fun getTagDetails(tagName: String): Result<TagDetailsResponseDto> {
        return tagsApi.getTagDetails(tagName)
    }

    override suspend fun observeTag(tagName: String): Result<Unit> {
        return tagsApi.observeTag(tagName)
    }

    override suspend fun unobserveTag(tagName: String): Result<Unit> {
        return tagsApi.unobserveTag(tagName)
    }

    override suspend fun enableTagNotifications(tagName: String): Result<Unit> {
        return tagsApi.enableTagNotifications(tagName)
    }

    override suspend fun disableTagNotifications(tagName: String): Result<Unit> {
        return tagsApi.disableTagNotifications(tagName)
    }

    override suspend fun getTagStream(
        tagName: String,
        page: PageRequest,
        limit: Int?,
        sort: String,
        type: String,
    ): Result<ResourceResponseDto> {
        return tagsApi.getTagStream(tagName, page, limit, sort, type)
    }

    override suspend fun getTagsAutoComplete(query: String): Result<TagsAutoCompleteResponseDto> {
        return tagsApi.getTagsAutoComplete(query)
    }
}

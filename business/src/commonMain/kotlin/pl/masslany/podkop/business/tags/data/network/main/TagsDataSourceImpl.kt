package pl.masslany.podkop.business.tags.data.network.main

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.common.data.network.models.common.SingleResourceResponseDto
import pl.masslany.podkop.business.tags.data.api.TagsDataSource
import pl.masslany.podkop.business.tags.data.network.api.TagsApi
import pl.masslany.podkop.business.tags.data.network.models.TagsAutoCompleteResponseDto

class TagsDataSourceImpl(
    private val tagsApi: TagsApi,
) : TagsDataSource {
    override suspend fun getTagDetails(tagName: String): Result<SingleResourceResponseDto> {
        return tagsApi.getTagDetails(tagName)
    }

    override suspend fun getTagStream(
        tagName: String,
        page: Any?,
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

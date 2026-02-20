package pl.masslany.podkop.business.tags.data.api

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.common.data.network.models.common.SingleResourceResponseDto
import pl.masslany.podkop.business.tags.data.network.models.TagsAutoCompleteResponseDto

interface TagsDataSource {
    suspend fun getTagDetails(tagName: String): Result<SingleResourceResponseDto>

    suspend fun getTagStream(
        tagName: String,
        page: Any?,
        limit: Int?,
        sort: String,
        type: String,
    ): Result<ResourceResponseDto>

    suspend fun getTagsAutoComplete(query: String): Result<TagsAutoCompleteResponseDto>
}

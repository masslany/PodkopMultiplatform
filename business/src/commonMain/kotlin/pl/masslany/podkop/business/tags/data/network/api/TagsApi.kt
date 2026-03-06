package pl.masslany.podkop.business.tags.data.network.api

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.tags.data.network.models.TagDetailsResponseDto
import pl.masslany.podkop.business.tags.data.network.models.TagsAutoCompleteResponseDto

interface TagsApi {
    suspend fun getTagDetails(tagName: String): Result<TagDetailsResponseDto>

    suspend fun observeTag(tagName: String): Result<Unit>

    suspend fun unobserveTag(tagName: String): Result<Unit>

    suspend fun enableTagNotifications(tagName: String): Result<Unit>

    suspend fun disableTagNotifications(tagName: String): Result<Unit>

    suspend fun getTagStream(
        tagName: String,
        page: Any?,
        limit: Int?,
        sort: String,
        type: String,
    ): Result<ResourceResponseDto>

    suspend fun getTagsAutoComplete(query: String): Result<TagsAutoCompleteResponseDto>
}

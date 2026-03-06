package pl.masslany.podkop.business.tags.domain.main

import pl.masslany.podkop.business.tags.domain.models.TagDetails
import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.tags.domain.models.TagsAutoComplete
import pl.masslany.podkop.business.tags.domain.models.request.TagsSort
import pl.masslany.podkop.business.tags.domain.models.request.TagsType

interface TagsRepository {
    suspend fun getTagDetails(tagName: String): Result<TagDetails>

    suspend fun observeTag(tagName: String): Result<Unit>

    suspend fun unobserveTag(tagName: String): Result<Unit>

    suspend fun enableTagNotifications(tagName: String): Result<Unit>

    suspend fun disableTagNotifications(tagName: String): Result<Unit>

    suspend fun getTagStream(
        tagName: String,
        page: Any?,
        limit: Int?,
        sort: TagsSort,
        type: TagsType,
    ): Result<Resources>

    fun getTagsTypes(): List<TagsType>

    fun getTagsSorts(): List<TagsSort>

    suspend fun getAutoCompleteTags(query: String): Result<TagsAutoComplete>
}

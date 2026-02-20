package pl.masslany.podkop.business.tags.domain.main

import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.tags.domain.models.TagsAutoComplete
import pl.masslany.podkop.business.tags.domain.models.request.TagsSort
import pl.masslany.podkop.business.tags.domain.models.request.TagsType

interface TagsRepository {
    suspend fun getTagDetails(tagName: String): Result<ResourceItem>

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

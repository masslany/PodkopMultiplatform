package pl.masslany.podkop.business.tags.data.main

import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.common.data.main.mapper.common.toResourceItemList
import pl.masslany.podkop.business.common.data.main.mapper.common.toResources
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.tags.data.api.TagsDataSource
import pl.masslany.podkop.business.tags.data.main.mapper.toTagsAutoComplete
import pl.masslany.podkop.business.tags.domain.main.TagsRepository
import pl.masslany.podkop.business.tags.domain.models.TagsAutoComplete
import pl.masslany.podkop.business.tags.domain.models.request.TagsSort
import pl.masslany.podkop.business.tags.domain.models.request.TagsType
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider

class TagsRepositoryImpl(
    private val tagsDataSource: TagsDataSource,
    private val dispatcherProvider: DispatcherProvider,
) : TagsRepository {
    override suspend fun getTagDetails(tagName: String): Result<ResourceItem> {
        return withContext(dispatcherProvider.io) {
            tagsDataSource.getTagDetails(tagName).mapCatching {
                listOf(it.data).toResourceItemList().first()
            }
        }
    }

    override suspend fun getTagStream(
        tagName: String,
        page: Any?,
        limit: Int?,
        sort: TagsSort,
        type: TagsType,
    ): Result<Resources> {
        return withContext(dispatcherProvider.io) {
            tagsDataSource.getTagStream(tagName, page, limit, sort.value, type.value).mapCatching {
                it.toResources()
            }
        }
    }

    override fun getTagsSorts(): List<TagsSort> {
        return listOf(TagsSort.All, TagsSort.Best)
    }

    override fun getTagsTypes(): List<TagsType> {
        return listOf(TagsType.All, TagsType.Entries, TagsType.Links)
    }

    override suspend fun getAutoCompleteTags(query: String): Result<TagsAutoComplete> {
        return withContext(dispatcherProvider.io) {
            tagsDataSource.getTagsAutoComplete(query).mapCatching {
                it.toTagsAutoComplete()
            }
        }
    }
}

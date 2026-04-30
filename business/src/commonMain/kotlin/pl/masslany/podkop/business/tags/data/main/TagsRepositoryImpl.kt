package pl.masslany.podkop.business.tags.data.main

import pl.masslany.podkop.common.pagination.PageRequest

import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.common.data.main.mapper.common.toResources
import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.tags.data.api.TagsDataSource
import pl.masslany.podkop.business.tags.data.main.mapper.toTagDetails
import pl.masslany.podkop.business.tags.data.main.mapper.toTagsAutoComplete
import pl.masslany.podkop.business.tags.domain.main.TagsRepository
import pl.masslany.podkop.business.tags.domain.models.TagDetails
import pl.masslany.podkop.business.tags.domain.models.TagsAutoComplete
import pl.masslany.podkop.business.tags.domain.models.request.TagsSort
import pl.masslany.podkop.business.tags.domain.models.request.TagsType
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider

class TagsRepositoryImpl(
    private val tagsDataSource: TagsDataSource,
    private val dispatcherProvider: DispatcherProvider,
) : TagsRepository {
    override suspend fun getTagDetails(tagName: String): Result<TagDetails> {
        return withContext(dispatcherProvider.io) {
            tagsDataSource.getTagDetails(tagName).mapCatching {
                it.data.toTagDetails()
            }
        }
    }

    override suspend fun observeTag(tagName: String): Result<Unit> = withContext(dispatcherProvider.io) {
        tagsDataSource.observeTag(tagName)
    }

    override suspend fun unobserveTag(tagName: String): Result<Unit> = withContext(dispatcherProvider.io) {
        tagsDataSource.unobserveTag(tagName)
    }

    override suspend fun enableTagNotifications(tagName: String): Result<Unit> =
        withContext(dispatcherProvider.io) {
            tagsDataSource.enableTagNotifications(tagName)
        }

    override suspend fun disableTagNotifications(tagName: String): Result<Unit> =
        withContext(dispatcherProvider.io) {
            tagsDataSource.disableTagNotifications(tagName)
        }

    override suspend fun getTagStream(
        tagName: String,
        page: PageRequest,
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

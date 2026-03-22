package pl.masslany.podkop.testsupport.fakes

import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.tags.domain.main.TagsRepository
import pl.masslany.podkop.business.tags.domain.models.TagDetails
import pl.masslany.podkop.business.tags.domain.models.TagsAutoComplete
import pl.masslany.podkop.business.tags.domain.models.request.TagsSort
import pl.masslany.podkop.business.tags.domain.models.request.TagsType

class FakeTagsRepository : TagsRepository {
    var getAutoCompleteTagsHandler: suspend (query: String) -> Result<TagsAutoComplete> =
        { Result.success(TagsAutoComplete(tags = emptyList())) }

    val getAutoCompleteTagsCalls = mutableListOf<String>()

    override suspend fun getAutoCompleteTags(query: String): Result<TagsAutoComplete> {
        getAutoCompleteTagsCalls += query
        return getAutoCompleteTagsHandler(query)
    }

    override suspend fun getTagDetails(tagName: String): Result<TagDetails> = notUsed()
    override suspend fun observeTag(tagName: String): Result<Unit> = notUsed()
    override suspend fun unobserveTag(tagName: String): Result<Unit> = notUsed()
    override suspend fun enableTagNotifications(tagName: String): Result<Unit> = notUsed()
    override suspend fun disableTagNotifications(tagName: String): Result<Unit> = notUsed()
    override suspend fun getTagStream(
        tagName: String,
        page: Any?,
        limit: Int?,
        sort: TagsSort,
        type: TagsType,
    ): Result<Resources> = notUsed()

    override fun getTagsTypes(): List<TagsType> = emptyList()

    override fun getTagsSorts(): List<TagsSort> = emptyList()
}

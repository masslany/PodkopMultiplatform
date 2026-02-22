package pl.masslany.podkop.business.testsupport.fakes

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.common.data.network.models.common.SingleResourceResponseDto
import pl.masslany.podkop.business.tags.data.api.TagsDataSource
import pl.masslany.podkop.business.tags.data.network.models.TagsAutoCompleteResponseDto

class FakeTagsDataSource : TagsDataSource {
    data class GetTagStreamCall(
        val tagName: String,
        val page: Any?,
        val limit: Int?,
        val sort: String,
        val type: String,
    )

    var getTagDetailsResult: Result<SingleResourceResponseDto> = unstubbedResult("TagsDataSource.getTagDetails")
    var getTagStreamResult: Result<ResourceResponseDto> = unstubbedResult("TagsDataSource.getTagStream")
    var getTagsAutoCompleteResult: Result<TagsAutoCompleteResponseDto> = unstubbedResult("TagsDataSource.getTagsAutoComplete")

    val getTagDetailsCalls = mutableListOf<String>()
    val getTagStreamCalls = mutableListOf<GetTagStreamCall>()
    val getTagsAutoCompleteCalls = mutableListOf<String>()

    override suspend fun getTagDetails(tagName: String): Result<SingleResourceResponseDto> {
        getTagDetailsCalls += tagName
        return getTagDetailsResult
    }

    override suspend fun getTagStream(
        tagName: String,
        page: Any?,
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

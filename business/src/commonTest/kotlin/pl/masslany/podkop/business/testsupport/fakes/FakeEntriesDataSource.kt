package pl.masslany.podkop.business.testsupport.fakes

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.common.data.network.models.common.SingleResourceResponseDto
import pl.masslany.podkop.business.entries.data.api.EntriesDataSource

class FakeEntriesDataSource : EntriesDataSource {
    data class GetEntriesCall(
        val page: Any?,
        val limit: Int?,
        val sort: String,
        val hotSort: Int,
        val category: String?,
        val bucket: String?,
    )

    data class GetEntryCommentsCall(
        val entryId: Int,
        val page: Any?,
    )

    var getEntriesResult: Result<ResourceResponseDto> = unstubbedResult("EntriesDataSource.getEntries")
    var getEntryResult: Result<SingleResourceResponseDto> = unstubbedResult("EntriesDataSource.getEntry")
    var getEntryCommentsResult: Result<ResourceResponseDto> = unstubbedResult("EntriesDataSource.getEntryComments")
    var voteUpResult: Result<Unit> = unstubbedResult("EntriesDataSource.voteUp")
    var removeVoteUpResult: Result<Unit> = unstubbedResult("EntriesDataSource.removeVoteUp")

    val getEntriesCalls = mutableListOf<GetEntriesCall>()
    val getEntryCalls = mutableListOf<Int>()
    val getEntryCommentsCalls = mutableListOf<GetEntryCommentsCall>()
    val voteUpCalls = mutableListOf<Int>()
    val removeVoteUpCalls = mutableListOf<Int>()

    override suspend fun getEntries(
        page: Any?,
        limit: Int?,
        sort: String,
        hotSort: Int,
        category: String?,
        bucket: String?,
    ): Result<ResourceResponseDto> {
        getEntriesCalls += GetEntriesCall(page, limit, sort, hotSort, category, bucket)
        return getEntriesResult
    }

    override suspend fun getEntry(entryId: Int): Result<SingleResourceResponseDto> {
        getEntryCalls += entryId
        return getEntryResult
    }

    override suspend fun getEntryComments(
        entryId: Int,
        page: Any?,
    ): Result<ResourceResponseDto> {
        getEntryCommentsCalls += GetEntryCommentsCall(entryId, page)
        return getEntryCommentsResult
    }

    override suspend fun voteUp(entryId: Int): Result<Unit> {
        voteUpCalls += entryId
        return voteUpResult
    }

    override suspend fun removeVoteUp(entryId: Int): Result<Unit> {
        removeVoteUpCalls += entryId
        return removeVoteUpResult
    }
}

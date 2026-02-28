package pl.masslany.podkop.business.testsupport.fakes

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.common.data.network.models.common.SingleResourceResponseDto
import pl.masslany.podkop.business.entries.data.api.EntriesDataSource
import pl.masslany.podkop.business.entries.data.network.models.EntryVotersResponseDto

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

    data class GetEntryVotesCall(
        val entryId: Int,
        val page: Any?,
    )

    data class GetEntryCommentVotesCall(
        val entryId: Int,
        val commentId: Int,
        val page: Any?,
    )

    data class CreateEntryCommentCall(
        val entryId: Int,
        val content: String,
        val adult: Boolean,
    )

    var getEntriesResult: Result<ResourceResponseDto> = unstubbedResult("EntriesDataSource.getEntries")
    var getEntryResult: Result<SingleResourceResponseDto> = unstubbedResult("EntriesDataSource.getEntry")
    var getEntryCommentsResult: Result<ResourceResponseDto> = unstubbedResult("EntriesDataSource.getEntryComments")
    var getEntryVotesResult: Result<EntryVotersResponseDto> = unstubbedResult("EntriesDataSource.getEntryVotes")
    var getEntryCommentVotesResult: Result<EntryVotersResponseDto> =
        unstubbedResult("EntriesDataSource.getEntryCommentVotes")
    var createEntryCommentResult: Result<SingleResourceResponseDto> =
        unstubbedResult("EntriesDataSource.createEntryComment")
    var voteUpResult: Result<Unit> = unstubbedResult("EntriesDataSource.voteUp")
    var removeVoteUpResult: Result<Unit> = unstubbedResult("EntriesDataSource.removeVoteUp")

    val getEntriesCalls = mutableListOf<GetEntriesCall>()
    val getEntryCalls = mutableListOf<Int>()
    val getEntryCommentsCalls = mutableListOf<GetEntryCommentsCall>()
    val getEntryVotesCalls = mutableListOf<GetEntryVotesCall>()
    val getEntryCommentVotesCalls = mutableListOf<GetEntryCommentVotesCall>()
    val createEntryCommentCalls = mutableListOf<CreateEntryCommentCall>()
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

    override suspend fun getEntryVotes(
        entryId: Int,
        page: Any?,
    ): Result<EntryVotersResponseDto> {
        getEntryVotesCalls += GetEntryVotesCall(entryId, page)
        return getEntryVotesResult
    }

    override suspend fun getEntryCommentVotes(
        entryId: Int,
        commentId: Int,
        page: Any?,
    ): Result<EntryVotersResponseDto> {
        getEntryCommentVotesCalls += GetEntryCommentVotesCall(entryId, commentId, page)
        return getEntryCommentVotesResult
    }

    override suspend fun createEntryComment(
        entryId: Int,
        content: String,
        adult: Boolean,
    ): Result<SingleResourceResponseDto> {
        createEntryCommentCalls += CreateEntryCommentCall(
            entryId = entryId,
            content = content,
            adult = adult,
        )
        return createEntryCommentResult
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

package pl.masslany.podkop.business.testsupport.fakes

import pl.masslany.podkop.common.pagination.PageRequest

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.common.data.network.models.common.SingleResourceResponseDto
import pl.masslany.podkop.business.entries.data.api.EntriesDataSource
import pl.masslany.podkop.business.entries.data.network.models.EntryVotersResponseDto

class FakeEntriesDataSource : EntriesDataSource {
    data class GetEntriesCall(
        val page: PageRequest,
        val limit: Int?,
        val sort: String,
        val hotSort: Int,
        val category: String?,
        val bucket: String?,
    )

    data class GetEntryCommentsCall(
        val entryId: Int,
        val page: Int?,
    )

    data class GetEntryVotesCall(
        val entryId: Int,
        val page: Int?,
    )

    data class GetEntryCommentVotesCall(
        val entryId: Int,
        val commentId: Int,
        val page: Int?,
    )

    data class CreateEntryCommentCall(
        val entryId: Int,
        val content: String,
        val adult: Boolean,
        val photoKey: String?,
    )

    data class CreateEntryCall(
        val content: String,
        val adult: Boolean,
        val photoKey: String?,
    )

    data class UpdateEntryCall(
        val entryId: Int,
        val content: String,
        val adult: Boolean,
        val photoKey: String?,
    )

    data class UpdateEntryCommentCall(
        val entryId: Int,
        val commentId: Int,
        val content: String,
        val adult: Boolean,
        val photoKey: String?,
    )

    data class VoteSurveyCall(
        val entryId: Int,
        val optionNumber: Int,
    )

    var getEntriesResult: Result<ResourceResponseDto> = unstubbedResult("EntriesDataSource.getEntries")
    var getEntryResult: Result<SingleResourceResponseDto> = unstubbedResult("EntriesDataSource.getEntry")
    var getEntryCommentsResult: Result<ResourceResponseDto> = unstubbedResult("EntriesDataSource.getEntryComments")
    var getEntryVotesResult: Result<EntryVotersResponseDto> = unstubbedResult("EntriesDataSource.getEntryVotes")
    var getEntryCommentVotesResult: Result<EntryVotersResponseDto> =
        unstubbedResult("EntriesDataSource.getEntryCommentVotes")
    var createEntryCommentResult: Result<SingleResourceResponseDto> =
        unstubbedResult("EntriesDataSource.createEntryComment")
    var createEntryResult: Result<SingleResourceResponseDto> =
        unstubbedResult("EntriesDataSource.createEntry")
    var updateEntryResult: Result<SingleResourceResponseDto> =
        unstubbedResult("EntriesDataSource.updateEntry")
    var updateEntryCommentResult: Result<SingleResourceResponseDto> =
        unstubbedResult("EntriesDataSource.updateEntryComment")
    var voteUpResult: Result<Unit> = unstubbedResult("EntriesDataSource.voteUp")
    var voteSurveyResult: Result<Unit> = unstubbedResult("EntriesDataSource.voteSurvey")
    var removeVoteUpResult: Result<Unit> = unstubbedResult("EntriesDataSource.removeVoteUp")
    var deleteEntryResult: Result<Unit> = unstubbedResult("EntriesDataSource.deleteEntry")
    var deleteEntryCommentResult: Result<Unit> = unstubbedResult("EntriesDataSource.deleteEntryComment")
    var voteUpCommentResult: Result<Unit> = unstubbedResult("EntriesDataSource.voteUpComment")
    var removeVoteUpCommentResult: Result<Unit> = unstubbedResult("EntriesDataSource.removeVoteUpComment")

    val getEntriesCalls = mutableListOf<GetEntriesCall>()
    val getEntryCalls = mutableListOf<Int>()
    val getEntryCommentsCalls = mutableListOf<GetEntryCommentsCall>()
    val getEntryVotesCalls = mutableListOf<GetEntryVotesCall>()
    val getEntryCommentVotesCalls = mutableListOf<GetEntryCommentVotesCall>()
    val createEntryCommentCalls = mutableListOf<CreateEntryCommentCall>()
    val createEntryCalls = mutableListOf<CreateEntryCall>()
    val updateEntryCalls = mutableListOf<UpdateEntryCall>()
    val updateEntryCommentCalls = mutableListOf<UpdateEntryCommentCall>()
    val voteUpCalls = mutableListOf<Int>()
    val voteSurveyCalls = mutableListOf<VoteSurveyCall>()
    val removeVoteUpCalls = mutableListOf<Int>()
    val deleteEntryCalls = mutableListOf<Int>()
    val deleteEntryCommentCalls = mutableListOf<Pair<Int, Int>>()
    val voteUpCommentCalls = mutableListOf<Pair<Int, Int>>()
    val removeVoteUpCommentCalls = mutableListOf<Pair<Int, Int>>()

    override suspend fun getEntries(
        page: PageRequest,
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
        page: Int?,
    ): Result<ResourceResponseDto> {
        getEntryCommentsCalls += GetEntryCommentsCall(entryId, page)
        return getEntryCommentsResult
    }

    override suspend fun getEntryVotes(
        entryId: Int,
        page: Int?,
    ): Result<EntryVotersResponseDto> {
        getEntryVotesCalls += GetEntryVotesCall(entryId, page)
        return getEntryVotesResult
    }

    override suspend fun getEntryCommentVotes(
        entryId: Int,
        commentId: Int,
        page: Int?,
    ): Result<EntryVotersResponseDto> {
        getEntryCommentVotesCalls += GetEntryCommentVotesCall(entryId, commentId, page)
        return getEntryCommentVotesResult
    }

    override suspend fun createEntryComment(
        entryId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<SingleResourceResponseDto> {
        createEntryCommentCalls += CreateEntryCommentCall(
            entryId = entryId,
            content = content,
            adult = adult,
            photoKey = photoKey,
        )
        return createEntryCommentResult
    }

    override suspend fun createEntry(
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<SingleResourceResponseDto> {
        createEntryCalls += CreateEntryCall(
            content = content,
            adult = adult,
            photoKey = photoKey,
        )
        return createEntryResult
    }

    override suspend fun updateEntry(
        entryId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<SingleResourceResponseDto> {
        updateEntryCalls += UpdateEntryCall(
            entryId = entryId,
            content = content,
            adult = adult,
            photoKey = photoKey,
        )
        return updateEntryResult
    }

    override suspend fun updateEntryComment(
        entryId: Int,
        commentId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<SingleResourceResponseDto> {
        updateEntryCommentCalls += UpdateEntryCommentCall(
            entryId = entryId,
            commentId = commentId,
            content = content,
            adult = adult,
            photoKey = photoKey,
        )
        return updateEntryCommentResult
    }

    override suspend fun voteUp(entryId: Int): Result<Unit> {
        voteUpCalls += entryId
        return voteUpResult
    }

    override suspend fun voteSurvey(
        entryId: Int,
        optionNumber: Int,
    ): Result<Unit> {
        voteSurveyCalls += VoteSurveyCall(entryId = entryId, optionNumber = optionNumber)
        return voteSurveyResult
    }

    override suspend fun removeVoteUp(entryId: Int): Result<Unit> {
        removeVoteUpCalls += entryId
        return removeVoteUpResult
    }

    override suspend fun deleteEntry(entryId: Int): Result<Unit> {
        deleteEntryCalls += entryId
        return deleteEntryResult
    }

    override suspend fun deleteEntryComment(entryId: Int, commentId: Int): Result<Unit> {
        deleteEntryCommentCalls += entryId to commentId
        return deleteEntryCommentResult
    }

    override suspend fun voteUpComment(entryId: Int, commentId: Int): Result<Unit> {
        voteUpCommentCalls += entryId to commentId
        return voteUpCommentResult
    }

    override suspend fun removeVoteUpComment(entryId: Int, commentId: Int): Result<Unit> {
        removeVoteUpCommentCalls += entryId to commentId
        return removeVoteUpCommentResult
    }
}

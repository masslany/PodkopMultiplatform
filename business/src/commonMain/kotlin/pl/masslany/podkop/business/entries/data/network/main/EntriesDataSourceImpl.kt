package pl.masslany.podkop.business.entries.data.network.main

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.common.data.network.models.common.SingleResourceResponseDto
import pl.masslany.podkop.business.entries.data.api.EntriesDataSource
import pl.masslany.podkop.business.entries.data.network.api.EntriesApi
import pl.masslany.podkop.business.entries.data.network.models.EntryVotersResponseDto


class EntriesDataSourceImpl(
    private val entriesApi: EntriesApi,
) : EntriesDataSource {
    override suspend fun getEntries(
        page: Any?,
        limit: Int?,
        sort: String,
        hotSort: Int,
        category: String?,
        bucket: String?,
    ): Result<ResourceResponseDto> {
        return entriesApi.getEntries(page, limit, sort, hotSort, category, bucket)
    }

    override suspend fun getEntry(entryId: Int): Result<SingleResourceResponseDto> {
        return entriesApi.getEntry(entryId)
    }

    override suspend fun getEntryComments(
        entryId: Int,
        page: Any?,
    ): Result<ResourceResponseDto> {
        return entriesApi.getEntryComments(entryId, page)
    }

    override suspend fun getEntryVotes(
        entryId: Int,
        page: Any?,
    ): Result<EntryVotersResponseDto> {
        return entriesApi.getEntryVotes(entryId, page)
    }

    override suspend fun getEntryCommentVotes(
        entryId: Int,
        commentId: Int,
        page: Any?,
    ): Result<EntryVotersResponseDto> {
        return entriesApi.getEntryCommentVotes(entryId, commentId, page)
    }

    override suspend fun createEntryComment(
        entryId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<SingleResourceResponseDto> {
        return entriesApi.createEntryComment(
            entryId = entryId,
            content = content,
            adult = adult,
            photoKey = photoKey,
        )
    }

    override suspend fun createEntry(
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<SingleResourceResponseDto> {
        return entriesApi.createEntry(
            content = content,
            adult = adult,
            photoKey = photoKey,
        )
    }

    override suspend fun voteUp(entryId: Int): Result<Unit> {
        return entriesApi.voteUp(entryId)
    }

    override suspend fun removeVoteUp(entryId: Int): Result<Unit> {
        return entriesApi.removeVoteUp(entryId)
    }

    override suspend fun deleteEntry(entryId: Int): Result<Unit> {
        return entriesApi.deleteEntry(entryId)
    }

    override suspend fun deleteEntryComment(entryId: Int, commentId: Int): Result<Unit> {
        return entriesApi.deleteEntryComment(entryId, commentId)
    }

    override suspend fun voteUpComment(entryId: Int, commentId: Int): Result<Unit> {
        return entriesApi.voteUpComment(entryId, commentId)
    }

    override suspend fun removeVoteUpComment(entryId: Int, commentId: Int): Result<Unit> {
        return entriesApi.removeVoteUpComment(entryId, commentId)
    }
}

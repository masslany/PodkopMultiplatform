package pl.masslany.podkop.business.entries.data.api

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.common.data.network.models.common.SingleResourceResponseDto
import pl.masslany.podkop.business.entries.data.network.models.EntryVotersResponseDto


interface EntriesDataSource {
    @Suppress("LongParameterList")
    suspend fun getEntries(
        page: Any?,
        limit: Int?,
        sort: String,
        hotSort: Int,
        category: String?,
        bucket: String?,
    ): Result<ResourceResponseDto>

    suspend fun getEntry(entryId: Int): Result<SingleResourceResponseDto>

    suspend fun getEntryComments(
        entryId: Int,
        page: Any?,
    ): Result<ResourceResponseDto>

    suspend fun getEntryVotes(
        entryId: Int,
        page: Any?,
    ): Result<EntryVotersResponseDto>

    suspend fun getEntryCommentVotes(
        entryId: Int,
        commentId: Int,
        page: Any?,
    ): Result<EntryVotersResponseDto>

    suspend fun createEntryComment(
        entryId: Int,
        content: String,
        adult: Boolean,
    ): Result<SingleResourceResponseDto>

    suspend fun createEntry(
        content: String,
        adult: Boolean,
    ): Result<SingleResourceResponseDto>

    suspend fun voteUp(entryId: Int): Result<Unit>

    suspend fun removeVoteUp(entryId: Int): Result<Unit>

    suspend fun deleteEntry(entryId: Int): Result<Unit>

    suspend fun deleteEntryComment(entryId: Int, commentId: Int): Result<Unit>

    suspend fun voteUpComment(entryId: Int, commentId: Int): Result<Unit>

    suspend fun removeVoteUpComment(entryId: Int, commentId: Int): Result<Unit>
}

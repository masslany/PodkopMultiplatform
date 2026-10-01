package pl.masslany.podkop.business.entries.data.api

import pl.masslany.podkop.common.pagination.PageRequest

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.common.data.network.models.common.SingleResourceResponseDto
import pl.masslany.podkop.business.entries.data.network.models.EntryThreadRepliesResponseDto
import pl.masslany.podkop.business.entries.data.network.models.EntryThreadResponseDto
import pl.masslany.podkop.business.entries.data.network.models.EntryVotersResponseDto


interface EntriesDataSource {
    @Suppress("LongParameterList")
    suspend fun getEntries(
        page: PageRequest,
        limit: Int?,
        sort: String,
        hotSort: Int,
        category: String?,
        bucket: String?,
    ): Result<ResourceResponseDto>

    suspend fun getEntry(entryId: Int): Result<SingleResourceResponseDto>

    suspend fun getEntryComments(
        entryId: Int,
        page: Int?,
    ): Result<ResourceResponseDto>

    suspend fun getEntryThread(
        entryId: Int,
        sort: String,
        limit: Int,
    ): Result<EntryThreadResponseDto>

    @Suppress("LongParameterList")
    suspend fun getEntryThreadReplies(
        entryId: Int,
        parentCommentId: Int?,
        sort: String,
        afterId: Int?,
        limit: Int,
    ): Result<EntryThreadRepliesResponseDto>

    @Suppress("LongParameterList")
    suspend fun createEntryThreadReply(
        entryId: Int,
        parentCommentId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<EntryThreadResponseDto>

    suspend fun getEntryVotes(
        entryId: Int,
        page: Int?,
    ): Result<EntryVotersResponseDto>

    suspend fun getEntryCommentVotes(
        entryId: Int,
        commentId: Int,
        page: Int?,
    ): Result<EntryVotersResponseDto>

    suspend fun createEntryComment(
        entryId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<SingleResourceResponseDto>

    suspend fun createEntry(
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<SingleResourceResponseDto>

    suspend fun updateEntry(
        entryId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<SingleResourceResponseDto>

    suspend fun updateEntryComment(
        entryId: Int,
        commentId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<SingleResourceResponseDto>

    suspend fun voteUp(entryId: Int): Result<Unit>

    suspend fun voteSurvey(
        entryId: Int,
        optionNumber: Int,
    ): Result<Unit>

    suspend fun removeVoteUp(entryId: Int): Result<Unit>

    suspend fun deleteEntry(entryId: Int): Result<Unit>

    suspend fun deleteEntryComment(entryId: Int, commentId: Int): Result<Unit>

    suspend fun voteUpComment(entryId: Int, commentId: Int): Result<Unit>

    suspend fun removeVoteUpComment(entryId: Int, commentId: Int): Result<Unit>
}

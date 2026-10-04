package pl.masslany.podkop.business.entries.domain.main

import pl.masslany.podkop.common.pagination.PageRequest

import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.common.domain.models.common.Voters
import pl.masslany.podkop.business.entries.domain.models.EntryThread
import pl.masslany.podkop.business.entries.domain.models.EntryThreadComment
import pl.masslany.podkop.business.entries.domain.models.EntryThreadReplies
import pl.masslany.podkop.business.entries.domain.models.EntryThreadSort
import pl.masslany.podkop.business.entries.domain.models.request.EntriesSortType
import pl.masslany.podkop.business.entries.domain.models.request.HotSortType
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
interface EntriesRepository {
    @Suppress("LongParameterList")
    suspend fun getEntries(
        page: PageRequest,
        limit: Int?,
        entriesSortType: EntriesSortType,
        hotSortType: HotSortType,
        category: String?,
        bucket: String?,
    ): Result<Resources>

    fun getEntriesSortTypes(): List<EntriesSortType>

    fun getHotSortTypes(): List<HotSortType>

    suspend fun getEntry(entryId: Int): Result<ResourceItem>

    suspend fun getEntryComments(
        entryId: Int,
        page: Int?,
    ): Result<Resources>

    /** The entry with the first page of its comments as a reply tree. */
    suspend fun getEntryThread(
        entryId: Int,
        sort: EntryThreadSort,
    ): Result<EntryThread>

    /**
     * Direct replies to [parentCommentId], or top-level comments when it is null, loaded after the
     * sibling with id [afterId].
     */
    suspend fun getEntryThreadReplies(
        entryId: Int,
        parentCommentId: Int?,
        sort: EntryThreadSort,
        afterId: Int?,
    ): Result<EntryThreadReplies>

    /** Posts a reply nested under [parentCommentId] (an [EntryThreadComment.replyParentId]). */
    suspend fun createEntryThreadReply(
        entryId: Int,
        parentCommentId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<EntryThreadComment>

    suspend fun getEntryVotes(
        entryId: Int,
        page: Int?,
    ): Result<Voters>

    suspend fun getEntryCommentVotes(
        entryId: Int,
        commentId: Int,
        page: Int?,
    ): Result<Voters>

    suspend fun createEntryComment(
        entryId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<ResourceItem>

    suspend fun createEntry(
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<ResourceItem>

    suspend fun updateEntry(
        entryId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<ResourceItem>

    suspend fun updateEntryComment(
        entryId: Int,
        commentId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<ResourceItem>

    suspend fun voteUp(entryId: Int): Result<Unit>

    suspend fun voteSurvey(
        entryId: Int,
        optionNumber: Int,
    ): Result<Unit>

    suspend fun removeVoteUp(entryId: Int): Result<Unit>

    suspend fun deleteEntry(entryId: Int): Result<Unit>

    suspend fun deleteEntryComment(
        entryId: Int,
        commentId: Int,
    ): Result<Unit>

    suspend fun voteUpComment(
        entryId: Int,
        commentId: Int,
    ): Result<Unit>

    suspend fun removeVoteUpComment(
        entryId: Int,
        commentId: Int,
    ): Result<Unit>

    suspend fun getLastUpdated(): Instant

    suspend fun setLastUpdated(lastUpdated: Instant)
}

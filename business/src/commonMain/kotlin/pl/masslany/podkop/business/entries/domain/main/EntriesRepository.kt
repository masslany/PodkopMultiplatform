package pl.masslany.podkop.business.entries.domain.main

import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.entries.domain.models.EntryVoters
import pl.masslany.podkop.business.entries.domain.models.request.EntriesSortType
import pl.masslany.podkop.business.entries.domain.models.request.HotSortType
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
interface EntriesRepository {
    @Suppress("LongParameterList")
    suspend fun getEntries(
        page: Any?,
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
        page: Any?,
    ): Result<Resources>

    suspend fun getEntryVotes(
        entryId: Int,
        page: Any?,
    ): Result<EntryVoters>

    suspend fun getEntryCommentVotes(
        entryId: Int,
        commentId: Int,
        page: Any?,
    ): Result<EntryVoters>

    suspend fun createEntryComment(
        entryId: Int,
        content: String,
        adult: Boolean,
    ): Result<ResourceItem>

    suspend fun createEntry(
        content: String,
        adult: Boolean,
    ): Result<ResourceItem>

    suspend fun voteUp(entryId: Int): Result<Unit>

    suspend fun removeVoteUp(entryId: Int): Result<Unit>

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

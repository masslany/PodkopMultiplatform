package pl.masslany.podkop.business.entries.domain.main

import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Resources
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

    suspend fun voteUp(entryId: Int): Result<Unit>

    suspend fun removeVoteUp(entryId: Int): Result<Unit>

    suspend fun getLastUpdated(): Instant

    suspend fun setLastUpdated(lastUpdated: Instant)
}

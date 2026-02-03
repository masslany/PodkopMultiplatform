package pl.masslany.podkop.business.entries.data.main

import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.common.data.main.mapper.common.toResourceItemList
import pl.masslany.podkop.business.common.data.main.mapper.common.toResources
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.entries.data.api.EntriesDataSource
import pl.masslany.podkop.business.entries.domain.main.EntriesRepository
import pl.masslany.podkop.business.entries.domain.models.request.EntriesSortType
import pl.masslany.podkop.business.entries.domain.models.request.HotSortType
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider
import pl.masslany.podkop.common.persistence.api.KeyValueStorage
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class EntriesRepositoryImpl(
    private val entriesDataSource: EntriesDataSource,
    private val dispatcherProvider: DispatcherProvider,
    private val keyValueStorage: KeyValueStorage,
) : EntriesRepository {
    override suspend fun getEntries(
        page: Any?,
        limit: Int?,
        entriesSortType: EntriesSortType,
        hotSortType: HotSortType,
        category: String?,
        bucket: String?,
    ): Result<Resources> {
        return withContext(dispatcherProvider.io) {
            entriesDataSource.getEntries(
                page = page,
                limit = limit,
                sort = entriesSortType.value,
                hotSort = hotSortType.value,
                category = category,
                bucket = bucket,
            ).mapCatching {
                it.toResources()
            }
        }
    }

    override fun getEntriesSortTypes(): List<EntriesSortType> {
        return listOf(EntriesSortType.Hot, EntriesSortType.Newest, EntriesSortType.Active)
    }

    override fun getHotSortTypes(): List<HotSortType> {
        return listOf(HotSortType.TwoHours, HotSortType.SixHours, HotSortType.TwelveHours)
    }

    override suspend fun getEntry(entryId: Int): Result<ResourceItem> {
        return withContext(dispatcherProvider.io) {
            entriesDataSource.getEntry(entryId).mapCatching {
                listOf(it.data).toResourceItemList().first()
            }
        }
    }

    override suspend fun getEntryComments(
        entryId: Int,
        page: Any?,
    ): Result<Resources> {
        return withContext(dispatcherProvider.io) {
            entriesDataSource.getEntryComments(entryId, page).mapCatching {
                it.toResources()
            }
        }
    }

    override suspend fun voteUp(entryId: Int): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            entriesDataSource.voteUp(entryId)
        }
    }

    override suspend fun removeVoteUp(entryId: Int): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            entriesDataSource.removeVoteUp(entryId)
        }
    }

    override suspend fun getLastUpdated(): Instant {
        return withContext(dispatcherProvider.io) {
            val long = keyValueStorage.getLong(ENTRIES_LAST_UPDATED_KEY) ?: Clock.System.now().epochSeconds

            Instant.fromEpochSeconds(long)
        }
    }

    override suspend fun setLastUpdated(lastUpdated: Instant) {
        val epochSeconds = lastUpdated.epochSeconds

        keyValueStorage.putLong(ENTRIES_LAST_UPDATED_KEY, epochSeconds)
    }

    internal companion object {
        const val ENTRIES_LAST_UPDATED_KEY = "ENTRIES_LAST_UPDATED_KEY"
    }
}

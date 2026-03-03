package pl.masslany.podkop.business.entries.data.main

import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.common.data.main.mapper.common.toResourceItemList
import pl.masslany.podkop.business.common.data.main.mapper.common.toResources
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.common.domain.models.common.Voters
import pl.masslany.podkop.business.entries.data.api.EntriesDataSource
import pl.masslany.podkop.business.entries.data.main.mapper.toVoters
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

    override suspend fun getEntryVotes(
        entryId: Int,
        page: Any?,
    ): Result<Voters> {
        return withContext(dispatcherProvider.io) {
            entriesDataSource.getEntryVotes(entryId, page).mapCatching {
                it.toVoters()
            }
        }
    }

    override suspend fun getEntryCommentVotes(
        entryId: Int,
        commentId: Int,
        page: Any?,
    ): Result<Voters> {
        return withContext(dispatcherProvider.io) {
            entriesDataSource.getEntryCommentVotes(entryId, commentId, page).mapCatching {
                it.toVoters()
            }
        }
    }

    override suspend fun createEntryComment(
        entryId: Int,
        content: String,
        adult: Boolean,
    ): Result<ResourceItem> {
        return withContext(dispatcherProvider.io) {
            entriesDataSource.createEntryComment(
                entryId = entryId,
                content = content,
                adult = adult,
            ).mapCatching {
                listOf(it.data).toResourceItemList().first()
            }
        }
    }

    override suspend fun createEntry(
        content: String,
        adult: Boolean,
    ): Result<ResourceItem> {
        return withContext(dispatcherProvider.io) {
            entriesDataSource.createEntry(
                content = content,
                adult = adult,
            ).mapCatching {
                listOf(it.data).toResourceItemList().first()
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

    override suspend fun deleteEntry(entryId: Int): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            entriesDataSource.deleteEntry(entryId)
        }
    }

    override suspend fun deleteEntryComment(
        entryId: Int,
        commentId: Int,
    ): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            entriesDataSource.deleteEntryComment(entryId, commentId)
        }
    }

    override suspend fun voteUpComment(
        entryId: Int,
        commentId: Int,
    ): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            entriesDataSource.voteUpComment(entryId, commentId)
        }
    }

    override suspend fun removeVoteUpComment(
        entryId: Int,
        commentId: Int,
    ): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            entriesDataSource.removeVoteUpComment(entryId, commentId)
        }
    }

    override suspend fun getLastUpdated(): Instant {
        return withContext(dispatcherProvider.io) {
            val long = keyValueStorage.getLong(ENTRIES_LAST_UPDATED_KEY) ?: Clock.System.now().toEpochMilliseconds()

            Instant.fromEpochMilliseconds(long)
        }
    }

    override suspend fun setLastUpdated(lastUpdated: Instant) {
        val epochSeconds = lastUpdated.toEpochMilliseconds()

        keyValueStorage.putLong(ENTRIES_LAST_UPDATED_KEY, epochSeconds)
    }

    internal companion object {
        const val ENTRIES_LAST_UPDATED_KEY = "ENTRIES_LAST_UPDATED_KEY"
    }
}

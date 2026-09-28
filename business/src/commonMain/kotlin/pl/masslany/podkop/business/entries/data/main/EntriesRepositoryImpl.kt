package pl.masslany.podkop.business.entries.data.main

import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.common.data.main.mapper.common.toResourceItemList
import pl.masslany.podkop.business.common.data.main.mapper.common.toResources
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.common.domain.models.common.Voters
import pl.masslany.podkop.business.entries.data.api.EntriesDataSource
import pl.masslany.podkop.business.entries.data.main.mapper.toEntryThread
import pl.masslany.podkop.business.entries.data.main.mapper.toEntryThreadComment
import pl.masslany.podkop.business.entries.data.main.mapper.toEntryThreadReplies
import pl.masslany.podkop.business.entries.data.main.mapper.toVoters
import pl.masslany.podkop.business.entries.domain.main.EntriesRepository
import pl.masslany.podkop.business.entries.domain.models.EntryThread
import pl.masslany.podkop.business.entries.domain.models.EntryThreadComment
import pl.masslany.podkop.business.entries.domain.models.EntryThreadReplies
import pl.masslany.podkop.business.entries.domain.models.EntryThreadSort
import pl.masslany.podkop.business.entries.domain.models.request.EntriesSortType
import pl.masslany.podkop.business.entries.domain.models.request.HotSortType
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider
import pl.masslany.podkop.common.pagination.PageRequest
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
        page: PageRequest,
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
        return listOf(HotSortType.TwoHours, HotSortType.SixHours, HotSortType.TwelveHours, HotSortType.TwentyFourHours)
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
        page: Int?,
    ): Result<Resources> {
        return withContext(dispatcherProvider.io) {
            entriesDataSource.getEntryComments(entryId, page).mapCatching {
                it.toResources()
            }
        }
    }

    override suspend fun getEntryThread(
        entryId: Int,
        sort: EntryThreadSort,
    ): Result<EntryThread> {
        return withContext(dispatcherProvider.io) {
            entriesDataSource.getEntryThread(
                entryId = entryId,
                sort = sort.value,
                limit = THREAD_PAGE_SIZE,
            ).mapCatching {
                it.data.toEntryThread()
            }
        }
    }

    override suspend fun getEntryThreadReplies(
        entryId: Int,
        parentCommentId: Int?,
        sort: EntryThreadSort,
        afterId: Int?,
    ): Result<EntryThreadReplies> {
        return withContext(dispatcherProvider.io) {
            entriesDataSource.getEntryThreadReplies(
                entryId = entryId,
                parentCommentId = parentCommentId,
                sort = sort.value,
                afterId = afterId,
                limit = THREAD_PAGE_SIZE,
            ).mapCatching {
                it.data.toEntryThreadReplies(fallbackDepth = if (parentCommentId == null) 0 else 1)
            }
        }
    }

    override suspend fun createEntryThreadReply(
        entryId: Int,
        parentCommentId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<EntryThreadComment> {
        return withContext(dispatcherProvider.io) {
            entriesDataSource.createEntryThreadReply(
                entryId = entryId,
                parentCommentId = parentCommentId,
                content = content,
                adult = adult,
                photoKey = photoKey,
            ).mapCatching {
                it.data.toEntryThreadComment(fallbackDepth = 1)
            }
        }
    }

    override suspend fun getEntryVotes(
        entryId: Int,
        page: Int?,
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
        page: Int?,
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
        photoKey: String?,
    ): Result<ResourceItem> {
        return withContext(dispatcherProvider.io) {
            entriesDataSource.createEntryComment(
                entryId = entryId,
                content = content,
                adult = adult,
                photoKey = photoKey,
            ).mapCatching {
                listOf(it.data).toResourceItemList().first()
            }
        }
    }

    override suspend fun createEntry(
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<ResourceItem> {
        return withContext(dispatcherProvider.io) {
            entriesDataSource.createEntry(
                content = content,
                adult = adult,
                photoKey = photoKey,
            ).mapCatching {
                listOf(it.data).toResourceItemList().first()
            }
        }
    }

    override suspend fun updateEntry(
        entryId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<ResourceItem> {
        return withContext(dispatcherProvider.io) {
            entriesDataSource.updateEntry(
                entryId = entryId,
                content = content,
                adult = adult,
                photoKey = photoKey,
            ).mapCatching {
                listOf(it.data).toResourceItemList().first()
            }
        }
    }

    override suspend fun updateEntryComment(
        entryId: Int,
        commentId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<ResourceItem> {
        return withContext(dispatcherProvider.io) {
            entriesDataSource.updateEntryComment(
                entryId = entryId,
                commentId = commentId,
                content = content,
                adult = adult,
                photoKey = photoKey,
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

    override suspend fun voteSurvey(
        entryId: Int,
        optionNumber: Int,
    ): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            entriesDataSource.voteSurvey(entryId, optionNumber)
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

        // Largest page the thread endpoints accept.
        const val THREAD_PAGE_SIZE = 25
    }
}

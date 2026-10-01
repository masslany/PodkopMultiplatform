package pl.masslany.podkop.testsupport.fakes

import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.common.domain.models.common.Voters
import pl.masslany.podkop.business.entries.domain.main.EntriesRepository
import pl.masslany.podkop.business.entries.domain.models.EntryThread
import pl.masslany.podkop.business.entries.domain.models.EntryThreadComment
import pl.masslany.podkop.business.entries.domain.models.EntryThreadReplies
import pl.masslany.podkop.business.entries.domain.models.EntryThreadSort
import pl.masslany.podkop.business.entries.domain.models.request.EntriesSortType
import pl.masslany.podkop.business.entries.domain.models.request.HotSortType
import pl.masslany.podkop.common.pagination.PageRequest
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Entry reads used by the entry details screen; every other call fails the test. */
@OptIn(ExperimentalTime::class)
class FakeEntriesRepository : EntriesRepository {
    data class ThreadRepliesCall(val parentCommentId: Int?, val afterId: Int?)

    var entryResult: Result<ResourceItem>? = null
    var threadResult: Result<EntryThread>? = null
    var commentsResult: Result<Resources>? = null

    /** Answers a replies request; may suspend to keep the request in flight. */
    var threadRepliesHandler: suspend (ThreadRepliesCall) -> Result<EntryThreadReplies> = { notUsed() }
    val threadRepliesCalls = mutableListOf<ThreadRepliesCall>()

    override suspend fun getEntry(entryId: Int): Result<ResourceItem> = entryResult ?: notUsed()

    override suspend fun getEntryThread(entryId: Int, sort: EntryThreadSort): Result<EntryThread> = threadResult ?: notUsed()

    override suspend fun getEntryThreadReplies(
        entryId: Int,
        parentCommentId: Int?,
        sort: EntryThreadSort,
        afterId: Int?,
    ): Result<EntryThreadReplies> {
        val call = ThreadRepliesCall(parentCommentId, afterId)
        threadRepliesCalls += call
        return threadRepliesHandler(call)
    }

    override suspend fun getEntryComments(entryId: Int, page: Int?): Result<Resources> = commentsResult ?: notUsed()

    override suspend fun getEntries(
        page: PageRequest,
        limit: Int?,
        entriesSortType: EntriesSortType,
        hotSortType: HotSortType,
        category: String?,
        bucket: String?,
    ): Result<Resources> = notUsed()

    override fun getEntriesSortTypes(): List<EntriesSortType> = error("not used")

    override fun getHotSortTypes(): List<HotSortType> = error("not used")

    override suspend fun createEntryThreadReply(
        entryId: Int,
        parentCommentId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<EntryThreadComment> = notUsed()

    override suspend fun getEntryVotes(entryId: Int, page: Int?): Result<Voters> = notUsed()

    override suspend fun getEntryCommentVotes(entryId: Int, commentId: Int, page: Int?): Result<Voters> = notUsed()

    override suspend fun createEntryComment(
        entryId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<ResourceItem> = notUsed()

    override suspend fun createEntry(content: String, adult: Boolean, photoKey: String?): Result<ResourceItem> =
        notUsed()

    override suspend fun updateEntry(
        entryId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<ResourceItem> = notUsed()

    override suspend fun updateEntryComment(
        entryId: Int,
        commentId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<ResourceItem> = notUsed()

    override suspend fun voteUp(entryId: Int): Result<Unit> = notUsed()

    override suspend fun voteSurvey(entryId: Int, optionNumber: Int): Result<Unit> = notUsed()

    override suspend fun removeVoteUp(entryId: Int): Result<Unit> = notUsed()

    override suspend fun deleteEntry(entryId: Int): Result<Unit> = notUsed()

    override suspend fun deleteEntryComment(entryId: Int, commentId: Int): Result<Unit> = notUsed()

    override suspend fun voteUpComment(entryId: Int, commentId: Int): Result<Unit> = notUsed()

    override suspend fun removeVoteUpComment(entryId: Int, commentId: Int): Result<Unit> = notUsed()

    override suspend fun getLastUpdated(): Instant = error("not used")

    override suspend fun setLastUpdated(lastUpdated: Instant) = error("not used")
}

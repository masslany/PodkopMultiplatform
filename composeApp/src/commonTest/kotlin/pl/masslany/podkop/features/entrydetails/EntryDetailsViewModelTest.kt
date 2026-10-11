package pl.masslany.podkop.features.entrydetails

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pl.masslany.podkop.business.common.domain.models.common.Deleted
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Voted
import pl.masslany.podkop.business.entries.domain.models.EntryThread
import pl.masslany.podkop.business.entries.domain.models.EntryThreadComment
import pl.masslany.podkop.business.entries.domain.models.EntryThreadReplies
import pl.masslany.podkop.common.preview.FakeAppSettings
import pl.masslany.podkop.common.preview.NoOpTopBarActions
import pl.masslany.podkop.testsupport.fakes.FakeAppLogger
import pl.masslany.podkop.testsupport.fakes.FakeAuthRepository
import pl.masslany.podkop.testsupport.fakes.FakeEntriesRepository
import pl.masslany.podkop.testsupport.fakes.FakeProfileRepository
import pl.masslany.podkop.testsupport.fakes.FakeResourceItemStateHolder
import pl.masslany.podkop.testsupport.fakes.FakeSnackbarManager
import pl.masslany.podkop.testsupport.navigation.createTestAppNavigator
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class EntryDetailsViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `refresh cancels a replies load still in flight`() = runTest(dispatcher) {
        val firstRepliesPage = CompletableDeferred<Result<EntryThreadReplies>>()
        var repliesLoadCancelled = false
        val entries = threadedEntries().apply {
            threadRepliesHandler = {
                try {
                    firstRepliesPage.await()
                } catch (cancelled: kotlin.coroutines.cancellation.CancellationException) {
                    repliesLoadCancelled = true
                    throw cancelled
                }
            }
        }
        val snackbar = FakeSnackbarManager()
        val sut = createSut(entries, snackbar)
        backgroundScope.launch { sut.state.collect {} }
        advanceUntilIdle()

        sut.onShowMoreEntryRepliesClicked(parentCommentId = 10)
        advanceUntilIdle()
        sut.onRefresh()
        firstRepliesPage.complete(Result.success(replies(total = 3, comment(11), comment(12))))
        advanceUntilIdle()

        assertTrue(repliesLoadCancelled)
        assertEquals(listOf("comment-10", "comment-11", "more-10"), sut.state.value.threadRows?.map { it.key })
        assertTrue(snackbar.emittedEvents.isEmpty(), "a cancelled load is not an error")
    }

    @Test
    fun `replies load appends the page under its parent`() = runTest(dispatcher) {
        val entries = threadedEntries().apply {
            threadRepliesHandler = { Result.success(replies(total = 3, comment(12), comment(13))) }
        }
        val sut = createSut(entries, FakeSnackbarManager())
        backgroundScope.launch { sut.state.collect {} }
        advanceUntilIdle()

        sut.onShowMoreEntryRepliesClicked(parentCommentId = 10)
        advanceUntilIdle()

        assertEquals(listOf(FakeEntriesRepository.ThreadRepliesCall(10, 11)), entries.threadRepliesCalls)
        assertEquals(
            listOf("comment-10", "comment-11", "comment-12", "comment-13"),
            sut.state.value.threadRows?.map { it.key },
        )
    }

    @Test
    fun `a failed page of top-level comments offers a retry that loads it`() = runTest(dispatcher) {
        var failNext = true
        val entries = threadedEntries(topLevelTotal = 2).apply {
            threadRepliesHandler = {
                if (failNext) Result.failure(IllegalStateException("offline")) else Result.success(replies(total = 2, comment(9)))
            }
        }
        val snackbar = FakeSnackbarManager()
        val sut = createSut(entries, snackbar)
        backgroundScope.launch { sut.state.collect {} }
        advanceUntilIdle()

        sut.paginate()
        advanceUntilIdle()

        assertTrue(sut.state.value.isPaginationError)
        assertFalse(sut.shouldPaginate(lastVisibleIndex = 3, totalItems = 4), "scrolling does not retry a failed page")
        assertTrue(snackbar.emittedEvents.isEmpty(), "the list shows the error in place")

        failNext = false
        sut.paginate()
        advanceUntilIdle()

        assertFalse(sut.state.value.isPaginationError)
        assertEquals(
            listOf(FakeEntriesRepository.ThreadRepliesCall(null, 10), FakeEntriesRepository.ThreadRepliesCall(null, 10)),
            entries.threadRepliesCalls,
        )
        assertEquals("comment-9", sut.state.value.threadRows?.last()?.key)
    }

    private fun threadedEntries(topLevelTotal: Int = 1) = FakeEntriesRepository().apply {
        entryResult = Result.success(resource(1, Resource.Entry))
        threadResult = Result.success(
            EntryThread(
                entry = resource(1, Resource.Entry),
                comments = replies(total = topLevelTotal, comment(10, replies(total = 3, comment(11)))),
            ),
        )
    }

    private fun TestScope.createSut(entries: FakeEntriesRepository, snackbar: FakeSnackbarManager) =
        EntryDetailsViewModel(
            screen = EntryDetailsScreen(id = 1),
            entriesRepository = entries,
            authRepository = FakeAuthRepository(),
            profileRepository = FakeProfileRepository(),
            resourceItemStateHolder = FakeResourceItemStateHolder(),
            logger = FakeAppLogger(),
            snackbarManager = snackbar,
            appNavigator = createTestAppNavigator(backgroundScope),
            appSettings = FakeAppSettings(threadedEntryCommentsInitial = true),
            topBarActions = NoOpTopBarActions,
        )

    private fun replies(total: Int, vararg items: EntryThreadComment) =
        EntryThreadReplies(totalCount = total, items = items.toList())

    private fun comment(id: Int, replies: EntryThreadReplies = EntryThreadReplies(0, emptyList())) =
        EntryThreadComment(
            comment = resource(id, Resource.EntryComment),
            depth = if (id < 11) 0 else 1,
            isByEntryAuthor = false,
            replyParentId = id,
            replies = replies,
        )

    private fun resource(id: Int, resource: Resource) = ResourceItem(
        actions = null, adult = false, archive = false, author = null, comments = null,
        content = "c$id", createdAt = null, deleted = Deleted.None, deletable = false,
        description = "", editable = false, hot = false, id = id, media = null, name = "",
        parent = null, publishedAt = null, recommended = false, resource = resource, slug = "",
        source = null, tags = emptyList(), title = "", voted = Voted.None, votes = null, favourite = false,
    )
}

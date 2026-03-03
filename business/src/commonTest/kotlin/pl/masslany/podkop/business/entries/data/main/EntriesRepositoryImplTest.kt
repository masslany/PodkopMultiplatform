package pl.masslany.podkop.business.entries.data.main

import kotlinx.coroutines.runBlocking
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.Gender
import pl.masslany.podkop.business.common.domain.models.common.NameColor
import pl.masslany.podkop.business.common.domain.models.common.Voted
import pl.masslany.podkop.business.entries.data.network.models.EntryVotersResponseDto
import pl.masslany.podkop.business.common.domain.models.common.Voter
import pl.masslany.podkop.business.common.domain.models.common.Voters
import pl.masslany.podkop.business.entries.domain.models.request.EntriesSortType
import pl.masslany.podkop.business.entries.domain.models.request.HotSortType
import pl.masslany.podkop.business.testsupport.fakes.FakeDispatcherProvider
import pl.masslany.podkop.business.testsupport.fakes.FakeEntriesDataSource
import pl.masslany.podkop.business.testsupport.fakes.FakeKeyValueStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.math.abs
import pl.masslany.podkop.business.testsupport.fixtures.BusinessFixtures as Fixtures

@OptIn(ExperimentalTime::class)
class EntriesRepositoryImplTest {

    @Test
    fun `get entries maps response and forwards shaped params`() = runBlocking {
        val entriesDataSource = FakeEntriesDataSource().apply {
            getEntriesResult = Result.success(
                Fixtures.resourceResponseDto(
                    data = listOf(Fixtures.resourceItemDto(id = 11, resource = "entry", voted = 1)),
                ),
            )
        }
        val sut = createSut(entriesDataSource = entriesDataSource)

        val actual = sut.getEntries(
            page = "cursor-1",
            limit = 30,
            entriesSortType = EntriesSortType.Hot,
            hotSortType = HotSortType.SixHours,
            category = "tech",
            bucket = "mobile",
        )

        assertEquals(
            listOf(
                FakeEntriesDataSource.GetEntriesCall(
                    page = "cursor-1",
                    limit = 30,
                    sort = "hot",
                    hotSort = 6,
                    category = "tech",
                    bucket = "mobile",
                ),
            ),
            entriesDataSource.getEntriesCalls,
        )
        assertEquals(
            Fixtures.resources(
                data = listOf(Fixtures.resourceItem(id = 11, resource = Resource.Entry, voted = Voted.Positive)),
            ),
            actual.getOrThrow(),
        )
    }

    @Test
    fun `get entries sort types returns supported values in order`() {
        val sut = createSut()

        assertEquals(
            listOf(EntriesSortType.Hot, EntriesSortType.Newest, EntriesSortType.Active),
            sut.getEntriesSortTypes(),
        )
    }

    @Test
    fun `get hot sort types returns supported values in order`() {
        val sut = createSut()

        assertEquals(
            listOf(HotSortType.TwoHours, HotSortType.SixHours, HotSortType.TwelveHours),
            sut.getHotSortTypes(),
        )
    }

    @Test
    fun `get entry maps single resource item`() = runBlocking {
        val entriesDataSource = FakeEntriesDataSource().apply {
            getEntryResult = Result.success(
                Fixtures.singleResourceResponseDto(
                    data = Fixtures.resourceItemDto(id = 99, resource = "link", voted = -1),
                ),
            )
        }
        val sut = createSut(entriesDataSource = entriesDataSource)

        val actual = sut.getEntry(99)

        assertEquals(listOf(99), entriesDataSource.getEntryCalls)
        assertEquals(
            Fixtures.resourceItem(id = 99, resource = Resource.Link, voted = Voted.Negative),
            actual.getOrThrow(),
        )
    }

    @Test
    fun `get entry comments maps resources and forwards page`() = runBlocking {
        val entriesDataSource = FakeEntriesDataSource().apply {
            getEntryCommentsResult = Result.success(Fixtures.resourceResponseDto())
        }
        val sut = createSut(entriesDataSource = entriesDataSource)

        val actual = sut.getEntryComments(entryId = 123, page = "next-page")

        assertEquals(
            listOf(FakeEntriesDataSource.GetEntryCommentsCall(entryId = 123, page = "next-page")),
            entriesDataSource.getEntryCommentsCalls,
        )
        assertEquals(Fixtures.resources(), actual.getOrThrow())
    }

    @Test
    fun `get entry votes maps users and forwards page`() = runBlocking {
        val entriesDataSource = FakeEntriesDataSource().apply {
            getEntryVotesResult = Result.success(
                EntryVotersResponseDto(
                    data = listOf(
                        Fixtures.userDto(
                            username = "alice",
                            avatar = "a1",
                            gender = "m",
                            color = "burgundy",
                            online = true,
                            company = false,
                            verified = true,
                            status = "active",
                        ),
                        Fixtures.userDto(
                            username = "eve",
                            avatar = "",
                            gender = null,
                            color = "black",
                            online = false,
                            company = true,
                            verified = false,
                            status = "banned",
                        ),
                    ),
                    pagination = Fixtures.paginationDto(perPage = 100000, total = 19, next = null, prev = "1"),
                ),
            )
        }
        val sut = createSut(entriesDataSource = entriesDataSource)

        val actual = sut.getEntryVotes(entryId = 85140119, page = "2")

        assertEquals(
            listOf(FakeEntriesDataSource.GetEntryVotesCall(entryId = 85140119, page = "2")),
            entriesDataSource.getEntryVotesCalls,
        )
        assertEquals(
            Voters(
                data = listOf(
                    Voter(
                        username = "alice",
                        avatar = "a1",
                        gender = Gender.Male,
                        color = NameColor.Burgundy,
                        online = true,
                        company = false,
                        verified = true,
                        status = "active",
                    ),
                    Voter(
                        username = "eve",
                        avatar = "",
                        gender = Gender.Unspecified,
                        color = NameColor.Black,
                        online = false,
                        company = true,
                        verified = false,
                        status = "banned",
                    ),
                ),
                pagination = Fixtures.pagination(perPage = 100000, total = 19, next = "", prev = "1"),
            ),
            actual.getOrThrow(),
        )
    }

    @Test
    fun `get entry comment votes maps users and forwards identifiers`() = runBlocking {
        val entriesDataSource = FakeEntriesDataSource().apply {
            getEntryCommentVotesResult = Result.success(
                EntryVotersResponseDto(
                    data = listOf(
                        Fixtures.userDto(
                            username = "comment-voter",
                            gender = "f",
                            color = "green",
                            status = "inactive",
                        ),
                    ),
                    pagination = null,
                ),
            )
        }
        val sut = createSut(entriesDataSource = entriesDataSource)

        val actual = sut.getEntryCommentVotes(
            entryId = 123,
            commentId = 456,
            page = 1,
        )

        assertEquals(
            listOf(FakeEntriesDataSource.GetEntryCommentVotesCall(entryId = 123, commentId = 456, page = 1)),
            entriesDataSource.getEntryCommentVotesCalls,
        )
        assertEquals(
            Voters(
                data = listOf(
                    Voter(
                        username = "comment-voter",
                        avatar = "https://example.com/user.png",
                        gender = Gender.Female,
                        color = NameColor.Green,
                        online = false,
                        company = true,
                        verified = false,
                        status = "inactive",
                    ),
                ),
                pagination = null,
            ),
            actual.getOrThrow(),
        )
    }

    @Test
    fun `create entry comment forwards payload and maps single resource item`() = runBlocking {
        val entriesDataSource = FakeEntriesDataSource().apply {
            createEntryCommentResult = Result.success(
                Fixtures.singleResourceResponseDto(
                    data = Fixtures.resourceItemDto(
                        id = 1001,
                        resource = "entry_comment",
                        parent = Fixtures.parentDto(id = 77),
                        voted = 0,
                    ),
                ),
            )
        }
        val sut = createSut(entriesDataSource = entriesDataSource)

        val actual = sut.createEntryComment(
            entryId = 77,
            content = "@author: hello there",
            adult = false,
            photoKey = "photo-key-1",
        )

        assertEquals(
            listOf(
                FakeEntriesDataSource.CreateEntryCommentCall(
                    entryId = 77,
                    content = "@author: hello there",
                    adult = false,
                    photoKey = "photo-key-1",
                ),
            ),
            entriesDataSource.createEntryCommentCalls,
        )
        assertEquals(
            Fixtures.resourceItem(
                id = 1001,
                resource = Resource.EntryComment,
                parent = Fixtures.parent(id = 77),
                voted = Voted.None,
            ),
            actual.getOrThrow(),
        )
    }

    @Test
    fun `create entry comment propagates failure`() = runBlocking {
        val expected = IllegalArgumentException("validation failed")
        val entriesDataSource = FakeEntriesDataSource().apply {
            createEntryCommentResult = Result.failure(expected)
        }
        val sut = createSut(entriesDataSource = entriesDataSource)

        val actual = sut.createEntryComment(
            entryId = 8,
            content = "foo",
            adult = false,
            photoKey = null,
        )

        assertTrue(actual.isFailure)
        assertSame(expected, actual.exceptionOrNull())
    }

    @Test
    fun `create entry forwards payload and maps single resource item`() = runBlocking {
        val entriesDataSource = FakeEntriesDataSource().apply {
            createEntryResult = Result.success(
                Fixtures.singleResourceResponseDto(
                    data = Fixtures.resourceItemDto(
                        id = 222,
                        resource = "entry",
                        voted = 0,
                    ),
                ),
            )
        }
        val sut = createSut(entriesDataSource = entriesDataSource)

        val actual = sut.createEntry(
            content = "new entry content",
            adult = true,
            photoKey = "photo-key-2",
        )

        assertEquals(
            listOf(
                FakeEntriesDataSource.CreateEntryCall(
                    content = "new entry content",
                    adult = true,
                    photoKey = "photo-key-2",
                ),
            ),
            entriesDataSource.createEntryCalls,
        )
        assertEquals(
            Fixtures.resourceItem(
                id = 222,
                resource = Resource.Entry,
                voted = Voted.None,
            ),
            actual.getOrThrow(),
        )
    }

    @Test
    fun `create entry propagates failure`() = runBlocking {
        val expected = IllegalStateException("entry creation failed")
        val entriesDataSource = FakeEntriesDataSource().apply {
            createEntryResult = Result.failure(expected)
        }
        val sut = createSut(entriesDataSource = entriesDataSource)

        val actual = sut.createEntry(
            content = "foo",
            adult = false,
            photoKey = null,
        )

        assertTrue(actual.isFailure)
        assertSame(expected, actual.exceptionOrNull())
    }

    @Test
    fun `vote up delegates to data source`() = runBlocking {
        val entriesDataSource = FakeEntriesDataSource().apply {
            voteUpResult = Result.success(Unit)
        }
        val sut = createSut(entriesDataSource = entriesDataSource)

        val actual = sut.voteUp(7)

        assertTrue(actual.isSuccess)
        assertEquals(listOf(7), entriesDataSource.voteUpCalls)
    }

    @Test
    fun `remove vote up delegates to data source`() = runBlocking {
        val entriesDataSource = FakeEntriesDataSource().apply {
            removeVoteUpResult = Result.success(Unit)
        }
        val sut = createSut(entriesDataSource = entriesDataSource)

        val actual = sut.removeVoteUp(8)

        assertTrue(actual.isSuccess)
        assertEquals(listOf(8), entriesDataSource.removeVoteUpCalls)
    }

    @Test
    fun `delete entry delegates to data source`() = runBlocking {
        val entriesDataSource = FakeEntriesDataSource().apply {
            deleteEntryResult = Result.success(Unit)
        }
        val sut = createSut(entriesDataSource = entriesDataSource)

        val actual = sut.deleteEntry(9)

        assertTrue(actual.isSuccess)
        assertEquals(listOf(9), entriesDataSource.deleteEntryCalls)
    }

    @Test
    fun `delete entry comment delegates to data source`() = runBlocking {
        val entriesDataSource = FakeEntriesDataSource().apply {
            deleteEntryCommentResult = Result.success(Unit)
        }
        val sut = createSut(entriesDataSource = entriesDataSource)

        val actual = sut.deleteEntryComment(entryId = 9, commentId = 99)

        assertTrue(actual.isSuccess)
        assertEquals(listOf(9 to 99), entriesDataSource.deleteEntryCommentCalls)
    }

    @Test
    fun `entry comment vote operations delegate to data source`() = runBlocking {
        val entriesDataSource = FakeEntriesDataSource().apply {
            voteUpCommentResult = Result.success(Unit)
            removeVoteUpCommentResult = Result.success(Unit)
        }
        val sut = createSut(entriesDataSource = entriesDataSource)

        val vote = sut.voteUpComment(entryId = 11, commentId = 22)
        val remove = sut.removeVoteUpComment(entryId = 33, commentId = 44)

        assertTrue(vote.isSuccess)
        assertTrue(remove.isSuccess)
        assertEquals(listOf(11 to 22), entriesDataSource.voteUpCommentCalls)
        assertEquals(listOf(33 to 44), entriesDataSource.removeVoteUpCommentCalls)
    }

    @Test
    fun `get last updated returns stored instant`() = runBlocking {
        val keyValueStorage = FakeKeyValueStorage().apply {
            putLong(EntriesRepositoryImpl.ENTRIES_LAST_UPDATED_KEY, 1234L)
        }
        val sut = createSut(keyValueStorage = keyValueStorage)

        val actual = sut.getLastUpdated()

        assertEquals(Instant.fromEpochMilliseconds(1234), actual)
    }

    @Test
    fun `get last updated falls back to current time when storage is empty`() = runBlocking {
        val sut = createSut(keyValueStorage = FakeKeyValueStorage())

        val actual = sut.getLastUpdated()
        val now = Clock.System.now()
        val deltaMillis = abs(now.toEpochMilliseconds() - actual.toEpochMilliseconds())

        assertTrue(deltaMillis <= 5_000L)
    }

    @Test
    fun `set last updated stores epoch seconds under repository key`() = runBlocking {
        val keyValueStorage = FakeKeyValueStorage()
        val sut = createSut(keyValueStorage = keyValueStorage)
        val instant = Instant.fromEpochMilliseconds(999)

        sut.setLastUpdated(instant)

        assertEquals(999L, keyValueStorage.getLong(EntriesRepositoryImpl.ENTRIES_LAST_UPDATED_KEY))
    }

    @Test
    fun `get entries propagates failure`() = runBlocking {
        val expected = IllegalStateException("http 500")
        val entriesDataSource = FakeEntriesDataSource().apply {
            getEntriesResult = Result.failure(expected)
        }
        val sut = createSut(entriesDataSource = entriesDataSource)

        val actual = sut.getEntries(
            page = null,
            limit = null,
            entriesSortType = EntriesSortType.Newest,
            hotSortType = HotSortType.TwoHours,
            category = null,
            bucket = null,
        )

        assertTrue(actual.isFailure)
        assertSame(expected, actual.exceptionOrNull())
    }

    private fun createSut(
        entriesDataSource: FakeEntriesDataSource = FakeEntriesDataSource(),
        keyValueStorage: FakeKeyValueStorage = FakeKeyValueStorage(),
    ): EntriesRepositoryImpl {
        return EntriesRepositoryImpl(
            entriesDataSource = entriesDataSource,
            dispatcherProvider = FakeDispatcherProvider(),
            keyValueStorage = keyValueStorage,
        )
    }
}

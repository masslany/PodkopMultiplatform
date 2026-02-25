package pl.masslany.podkop.business.entries.data.main

import kotlinx.coroutines.runBlocking
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.Gender
import pl.masslany.podkop.business.common.domain.models.common.NameColor
import pl.masslany.podkop.business.common.domain.models.common.Voted
import pl.masslany.podkop.business.entries.data.network.models.EntryVotersResponseDto
import pl.masslany.podkop.business.entries.domain.models.EntryVoter
import pl.masslany.podkop.business.entries.domain.models.EntryVoters
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
            EntryVoters(
                data = listOf(
                    EntryVoter(
                        username = "alice",
                        avatar = "a1",
                        gender = Gender.Male,
                        color = NameColor.Burgundy,
                        online = true,
                        company = false,
                        verified = true,
                        status = "active",
                    ),
                    EntryVoter(
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
            EntryVoters(
                data = listOf(
                    EntryVoter(
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
        val before = Clock.System.now()

        val actual = sut.getLastUpdated()

        val after = Clock.System.now()

        assertTrue(actual >= before)
        assertTrue(actual <= after)
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

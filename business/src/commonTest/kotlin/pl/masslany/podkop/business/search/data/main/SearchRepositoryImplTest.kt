package pl.masslany.podkop.business.search.data.main

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import pl.masslany.podkop.business.common.domain.models.common.Pagination
import pl.masslany.podkop.business.search.domain.models.request.SearchSort
import pl.masslany.podkop.business.search.domain.models.request.SearchStreamQuery
import pl.masslany.podkop.business.testsupport.fakes.FakeDispatcherProvider
import pl.masslany.podkop.business.testsupport.fakes.FakeSearchDataSource
import pl.masslany.podkop.business.testsupport.fixtures.BusinessFixtures as Fixtures

class SearchRepositoryImplTest {

    @Test
    fun `get search stream maps resources and forwards filters`() = runBlocking {
        val searchDataSource = FakeSearchDataSource().apply {
            getSearchStreamResult = Result.success(Fixtures.resourceResponseDto())
        }
        val sut = SearchRepositoryImpl(
            searchDataSource = searchDataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )
        val query = SearchStreamQuery(
            query = "test",
            sort = SearchSort.Comments,
            minimumVotes = 100,
            dateFrom = "2025-01-01 00:00:00",
            dateTo = "2025-01-31 23:59:59",
            domains = listOf("wykop.pl", "example.com"),
            users = listOf("wykop"),
            tags = listOf("android", "ios"),
            category = "technologia",
        )

        val actual = sut.getSearchStream(
            page = 2,
            limit = 40,
            query = query,
        )

        assertEquals(
            listOf(
                FakeSearchDataSource.GetSearchStreamCall(
                    page = 2,
                    limit = 40,
                    query = query,
                ),
            ),
            searchDataSource.getSearchStreamCalls,
        )
        assertEquals(Fixtures.resources(), actual.getOrThrow())
    }

    @Test
    fun `get search stream synthesizes next page when api omits it`() = runBlocking {
        val searchDataSource = FakeSearchDataSource().apply {
            getSearchStreamResult = Result.success(
                Fixtures.resourceResponseDto(
                    pagination = Fixtures.paginationDto(
                        total = 72,
                        perPage = 40,
                        next = null,
                        prev = null,
                    ),
                ),
            )
        }
        val sut = SearchRepositoryImpl(
            searchDataSource = searchDataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )

        val actual = sut.getSearchStream(
            page = 1,
            limit = null,
            query = SearchStreamQuery(query = "test"),
        )

        assertEquals(
            Pagination(
                perPage = 40,
                total = 72,
                next = "2",
                prev = "",
            ),
            actual.getOrThrow().pagination,
        )
    }

    @Test
    fun `get search stream propagates failure`() = runBlocking {
        val expected = IllegalArgumentException("bad request")
        val searchDataSource = FakeSearchDataSource().apply {
            getSearchStreamResult = Result.failure(expected)
        }
        val sut = SearchRepositoryImpl(
            searchDataSource = searchDataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )

        val actual = sut.getSearchStream(
            page = 1,
            limit = null,
            query = SearchStreamQuery(query = "test"),
        )

        assertTrue(actual.isFailure)
        assertSame(expected, actual.exceptionOrNull())
    }
}

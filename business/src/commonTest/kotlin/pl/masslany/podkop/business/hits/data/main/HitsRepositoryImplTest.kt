package pl.masslany.podkop.business.hits.data.main

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import pl.masslany.podkop.business.hits.domain.models.request.HitsSortType
import pl.masslany.podkop.business.testsupport.fakes.FakeDispatcherProvider
import pl.masslany.podkop.business.testsupport.fakes.FakeHitsDataSource
import pl.masslany.podkop.business.testsupport.fixtures.BusinessFixtures as Fixtures

class HitsRepositoryImplTest {

    @Test
    fun `get link hits maps resources and passes sort value`() = runBlocking {
        val hitsDataSource = FakeHitsDataSource().apply {
            getLinkHitsResult = Result.success(
                Fixtures.resourceResponseDto(
                    data = listOf(Fixtures.resourceItemDto(id = 1, resource = "link", voted = 1)),
                ),
            )
        }
        val sut = HitsRepositoryImpl(
            hitsDataSource = hitsDataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )

        val actual = sut.getLinkHits(HitsSortType.Day)

        assertEquals("day", hitsDataSource.getLinkHitsCalls.single())
        assertEquals(
            Fixtures.resources(
                data = listOf(Fixtures.resourceItem(id = 1)),
            ),
            actual.getOrThrow(),
        )
    }

    @Test
    fun `get link hits propagates failure`() = runBlocking {
        val expected = IllegalStateException("network")
        val hitsDataSource = FakeHitsDataSource().apply {
            getLinkHitsResult = Result.failure(expected)
        }
        val sut = HitsRepositoryImpl(
            hitsDataSource = hitsDataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )

        val actual = sut.getLinkHits(HitsSortType.Day)

        assertTrue(actual.isFailure)
        assertSame(expected, actual.exceptionOrNull())
    }
}

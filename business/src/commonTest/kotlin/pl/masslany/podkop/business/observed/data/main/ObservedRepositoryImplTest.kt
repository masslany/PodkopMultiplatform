package pl.masslany.podkop.business.observed.data.main

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import pl.masslany.podkop.business.observed.data.network.models.ObservedItemDto
import pl.masslany.podkop.business.observed.data.network.models.ObservedResponseDto
import pl.masslany.podkop.business.observed.domain.models.ObservedResource
import pl.masslany.podkop.business.observed.domain.models.ObservedResources
import pl.masslany.podkop.business.observed.domain.models.request.ObservedType
import pl.masslany.podkop.business.testsupport.fakes.FakeDispatcherProvider
import pl.masslany.podkop.business.testsupport.fakes.FakeObservedDataSource
import pl.masslany.podkop.common.pagination.PageRequest
import pl.masslany.podkop.business.testsupport.fixtures.BusinessFixtures as Fixtures

class ObservedRepositoryImplTest {

    @Test
    fun `get observed maps response and forwards args`() = runBlocking {
        val observedDataSource = FakeObservedDataSource().apply {
            getObservedResult = Result.success(
                ObservedResponseDto(
                    data = listOf(
                        ObservedItemDto(
                            type = "entry",
                            item = Fixtures.resourceItemDto(id = 17, resource = "entry"),
                        ),
                        ObservedItemDto(
                            type = "link",
                            item = Fixtures.resourceItemDto(id = 18, resource = "link"),
                        ),
                    ),
                    pagination = Fixtures.paginationDto(
                        perPage = 25,
                        total = 52,
                        next = "cursor-next",
                        prev = null,
                    ),
                ),
            )
        }
        val sut = createSut(observedDataSource = observedDataSource)

        val actual = sut.getObserved(page = PageRequest.PageCursor("cursor-1"), type = ObservedType.Discussions)

        assertEquals(
            listOf(
                FakeObservedDataSource.GetObservedCall(
                    page = PageRequest.PageCursor("cursor-1"),
                    type = ObservedType.Discussions,
                ),
            ),
            observedDataSource.getObservedCalls,
        )
        assertEquals(
            ObservedResources(
                data = listOf(
                    ObservedResource(
                        item = Fixtures.resourceItem(
                            id = 17,
                            resource = pl.masslany.podkop.business.common.domain.models.common.Resource.Entry,
                        ),
                        newContentCount = null,
                    ),
                    ObservedResource(
                        item = Fixtures.resourceItem(
                            id = 18,
                            resource = pl.masslany.podkop.business.common.domain.models.common.Resource.Link,
                        ),
                        newContentCount = null,
                    ),
                ),
                pagination = Fixtures.pagination(
                    perPage = 25,
                    total = 52,
                    next = "cursor-next",
                    prev = "",
                ),
            ),
            actual.getOrThrow(),
        )
    }

    private fun createSut(
        observedDataSource: FakeObservedDataSource = FakeObservedDataSource(),
    ): ObservedRepositoryImpl {
        return ObservedRepositoryImpl(
            observedDataSource = observedDataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )
    }
}

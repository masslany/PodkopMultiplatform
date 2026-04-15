package pl.masslany.podkop.common.pagination

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import pl.masslany.podkop.business.common.domain.models.common.PaginatedData
import pl.masslany.podkop.business.common.domain.models.common.Pagination

class PaginatorTest {

    private val paginator = Paginator<Any>(
        scope = CoroutineScope(Dispatchers.Unconfined),
        onNewItems = {},
        loader = { Result.success(TestPaginatedData()) },
    )

    @Test
    fun `reachedEnd returns true when total has been emitted`() {
        assertTrue(
            paginator.reachedEnd(
                pagination = Pagination(
                    perPage = 25,
                    total = 340,
                    next = "",
                    prev = "",
                ),
                receivedItemsCount = 15,
                emittedItemsCount = 340,
            ),
        )
    }

    @Test
    fun `reachedEnd returns false when next cursor exists even for short mapped page`() {
        assertFalse(
            paginator.reachedEnd(
                pagination = Pagination(
                    perPage = 40,
                    total = 72,
                    next = "2",
                    prev = "",
                ),
                receivedItemsCount = 38,
                emittedItemsCount = 38,
            ),
        )
    }

    @Test
    fun `reachedEnd returns true for short final page without next cursor`() {
        assertTrue(
            paginator.reachedEnd(
                pagination = Pagination(
                    perPage = 25,
                    total = 0,
                    next = "",
                    prev = "",
                ),
                receivedItemsCount = 15,
                emittedItemsCount = 315,
            ),
        )
    }

    @Test
    fun `reachedEnd returns false when next is blank but total says more items remain`() {
        assertFalse(
            paginator.reachedEnd(
                pagination = Pagination(
                    perPage = 35,
                    total = 2295,
                    next = "",
                    prev = "",
                ),
                receivedItemsCount = 35,
                emittedItemsCount = 35,
            ),
        )
    }

    @Test
    fun `reachedEnd returns true when next is blank even without per page or total`() {
        assertTrue(
            paginator.reachedEnd(
                pagination = Pagination(
                    perPage = 0,
                    total = 0,
                    next = "",
                    prev = "",
                ),
                receivedItemsCount = 9,
                emittedItemsCount = 9,
            ),
        )
    }

    @Test
    fun `setup keeps paginator idle when next is blank but full page suggests more items`() {
        paginator.setup(
            pagination = Pagination(
                perPage = 35,
                total = 2295,
                next = "",
                prev = "",
            ),
            initialItemCount = 35,
        )

        assertEquals(PaginatorState.Idle, paginator.state.value)
    }

    @Test
    fun `paginate requests second page when next is blank but full page suggests more items`() {
        val requests = mutableListOf<PageRequest>()
        val paginator = Paginator(
            scope = CoroutineScope(Dispatchers.Unconfined),
            onNewItems = {},
        ) { request ->
            requests += request
            Result.success(
                TestPaginatedData(
                    data = List(35) { Any() },
                    pagination = Pagination(
                        perPage = 35,
                        total = 2295,
                        next = "",
                        prev = "",
                    ),
                ),
            )
        }

        paginator.setup(
            pagination = Pagination(
                perPage = 35,
                total = 2295,
                next = "",
                prev = "",
            ),
            initialItemCount = 35,
        )

        paginator.paginate()

        assertEquals(listOf<PageRequest>(PageRequest.Index(2)), requests)
        assertEquals(PaginatorState.Idle, paginator.state.value)
    }
}

private data class TestPaginatedData(
    override val data: List<Any> = emptyList(),
    override val pagination: Pagination? = null,
) : PaginatedData<Any>

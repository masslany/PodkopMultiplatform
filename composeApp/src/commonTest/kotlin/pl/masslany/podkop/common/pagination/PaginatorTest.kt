package pl.masslany.podkop.common.pagination

import kotlin.test.Test
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
    fun `reachedEnd returns true when api explicitly reports no next page`() {
        assertTrue(
            paginator.reachedEnd(
                pagination = Pagination(
                    perPage = 25,
                    total = 340,
                    next = "",
                    prev = "",
                ),
                receivedItemsCount = 25,
                emittedItemsCount = 325,
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
}

private data class TestPaginatedData(
    override val data: List<Any> = emptyList(),
    override val pagination: Pagination? = null,
) : PaginatedData<Any>

package pl.masslany.podkop.ios

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import pl.masslany.podkop.business.common.domain.models.common.Pagination
import pl.masslany.podkop.common.pagination.PageRequest
import pl.masslany.podkop.common.pagination.PaginationMode

class NextPageRequestTest {
    private fun pagination(next: String = "", total: Int = 0, perPage: Int = 0) =
        Pagination(perPage = perPage, total = total, next = next, prev = "")

    @Test
    fun cursorModeSendsTheReturnedCursorAsPage() {
        val next = nextPageRequest(
            PaginationMode.CursorInPage, PageRequest.Initial, pagination(next = "123"), received = 20, loaded = 0,
        )
        assertEquals(PageRequest.PageCursor("123"), next)
    }

    @Test
    fun cursorThatDidNotAdvanceEndsTheList() {
        assertNull(
            nextPageRequest(
                PaginationMode.CursorInPage, PageRequest.PageCursor("abc"), pagination(next = "abc"),
                received = 20, loaded = 20,
            ),
        )
    }

    @Test
    fun cursorModeStopsWithoutCursorOrAtTotal() {
        assertNull(nextPageRequest(PaginationMode.CursorInPage, PageRequest.Initial, pagination(), 20, 0))
        assertNull(
            nextPageRequest(PaginationMode.CursorInPage, PageRequest.Initial, pagination("x", total = 40), 20, 20),
        )
    }

    @Test
    fun numberedModeAdvancesUntilShortOrEmptyPage() {
        assertEquals(
            PageRequest.Number(3),
            nextPageRequest(PaginationMode.Numbered, PageRequest.Number(2), pagination(perPage = 25), 25, 25),
        )
        assertNull(nextPageRequest(PaginationMode.Numbered, PageRequest.Number(2), pagination(perPage = 25), 24, 25))
        assertNull(nextPageRequest(PaginationMode.Numbered, PageRequest.Number(1), pagination(next = "2"), 0, 0))
    }

    @Test
    fun numberedShortPageKeepsPagingWhileTotalSaysMore() {
        // As a real profile tab served it: 23 of 25 items on page one, with a total of 532.
        assertEquals(
            PageRequest.Number(2),
            nextPageRequest(PaginationMode.Numbered, PageRequest.Number(1), pagination(perPage = 25, total = 532), 23, 0),
        )
        assertNull(
            nextPageRequest(PaginationMode.Numbered, PageRequest.Number(2), pagination(perPage = 25, total = 532), 0, 23),
        )
    }

    @Test
    fun numberedModePrefersNumericNextAndHonoursTotal() {
        assertEquals(
            PageRequest.Number(5),
            nextPageRequest(PaginationMode.Numbered, PageRequest.Number(1), pagination(next = "5"), 10, 0),
        )
        assertNull(
            nextPageRequest(PaginationMode.Numbered, PageRequest.Number(3), pagination(next = "4", total = 73), 13, 60),
        )
    }

    @Test
    fun numberedPageWithoutPaginationMetadataKeepsPaging() {
        assertEquals(
            PageRequest.Number(2),
            nextPageRequest(PaginationMode.Numbered, PageRequest.Number(1), null, 10, 0),
        )
    }
}

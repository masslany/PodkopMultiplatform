package pl.masslany.podkop.common.pagination

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PageRequestTest {

    @Test
    fun `initial request is numbered for numbered pagination`() {
        assertEquals(PageRequest.Number(1), PaginationMode.Numbered.initialRequest())
    }

    @Test
    fun `initial request omits cursor parameters`() {
        assertEquals(PageRequest.Initial, PaginationMode.CursorInPage.initialRequest())
        assertEquals(PageRequest.Initial, PaginationMode.CursorInKey.initialRequest())
    }

    @Test
    fun `next request maps cursor according to mode`() {
        assertEquals(PageRequest.Number(4), PaginationMode.Numbered.nextRequest(next = "4", nextNumber = 2))
        assertEquals(
            PageRequest.PageCursor("hash"),
            PaginationMode.CursorInPage.nextRequest(next = "hash", nextNumber = 2),
        )
        assertEquals(
            PageRequest.KeyCursor("hash"),
            PaginationMode.CursorInKey.nextRequest(next = "hash", nextNumber = 2),
        )
    }

    @Test
    fun `numbered pagination falls back to next number when next is blank`() {
        assertEquals(PageRequest.Number(2), PaginationMode.Numbered.nextRequest(next = "", nextNumber = 2))
    }

    @Test
    fun `cursor pagination stops when next is blank`() {
        assertNull(PaginationMode.CursorInPage.nextRequest(next = "", nextNumber = 2))
        assertNull(PaginationMode.CursorInKey.nextRequest(next = null, nextNumber = 2))
    }

    @Test
    fun `numberOrNull returns number only for numbered requests`() {
        assertEquals(3, PageRequest.Number(3).numberOrNull())
        assertNull(PageRequest.Initial.numberOrNull())
        assertNull(PageRequest.PageCursor("hash").numberOrNull())
        assertNull(PageRequest.KeyCursor("hash").numberOrNull())
    }
}

package pl.masslany.podkop.business.common.data.network.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import pl.masslany.podkop.common.pagination.PageRequest

class PageQueryParameterTest {

    @Test
    fun `page query parameter uses page for numeric indexes`() {
        assertEquals("page" to "2", paginationQueryParameter(PageRequest.Number(2)))
    }

    @Test
    fun `page query parameter uses page for page cursors`() {
        assertEquals("page" to "next-cursor", paginationQueryParameter(PageRequest.PageCursor("next-cursor")))
        assertEquals("page" to "123", paginationQueryParameter(PageRequest.PageCursor("123")))
    }

    @Test
    fun `page query parameter uses key for key cursors`() {
        assertEquals("key" to "next-cursor", paginationQueryParameter(PageRequest.KeyCursor("next-cursor")))
        assertEquals("key" to "123", paginationQueryParameter(PageRequest.KeyCursor("123")))
    }

    @Test
    fun `page query parameter omits initial requests`() {
        assertNull(paginationQueryParameter(PageRequest.Initial))
    }
}

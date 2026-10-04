package pl.masslany.podkop.widget.models

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HitsWidgetStateTest {
    @Test
    fun `needs a refresh without data or once the data is stale`() {
        val snapshot = HitsWidgetSnapshot(updatedAtMillis = 0, items = emptyList())
        val twoHours = 2 * 60 * 60 * 1000L

        assertTrue(HitsWidgetState().needsRefresh(nowMillis = 0))
        assertFalse(HitsWidgetState(isRefreshing = true).needsRefresh(nowMillis = 0))
        assertFalse(HitsWidgetState(snapshot = snapshot).needsRefresh(nowMillis = twoHours - 1))
        assertTrue(HitsWidgetState(snapshot = snapshot).needsRefresh(nowMillis = twoHours))
    }
}

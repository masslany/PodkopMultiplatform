package pl.masslany.podkop.widget.models

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlinx.serialization.json.Json

class HitsWidgetItemTest {
    @Test
    fun `older snapshots without the hot flag read as not hot`() {
        val item = Json.decodeFromString<HitsWidgetItem>(
            """{"id":1,"title":"Link 1","votes":5,"comments":0}""",
        )

        assertFalse(item.isHot)
    }
}

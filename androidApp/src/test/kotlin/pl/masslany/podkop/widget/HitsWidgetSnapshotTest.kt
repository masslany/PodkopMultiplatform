package pl.masslany.podkop.widget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import pl.masslany.podkop.business.common.domain.models.common.Comments
import pl.masslany.podkop.business.common.domain.models.common.Deleted
import pl.masslany.podkop.business.common.domain.models.common.Media
import pl.masslany.podkop.business.common.domain.models.common.Photo
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Source
import pl.masslany.podkop.business.common.domain.models.common.Voted
import pl.masslany.podkop.business.common.domain.models.common.Votes

class HitsWidgetSnapshotTest {
    @Test
    fun `maps a link to a widget item`() {
        val items = listOf(link(id = 1, hot = true)).toHitsWidgetItems()

        assertEquals(
            listOf(
                HitsWidgetItem(
                    id = 1,
                    title = "Link 1",
                    votes = 120,
                    isHot = true,
                    comments = 34,
                    source = "example.com",
                    thumbnailUrl = "https://example.com/1.jpg",
                ),
            ),
            items,
        )
    }

    @Test
    fun `keeps only visible links up to the limit`() {
        val items = listOf(
            link(id = 1),
            link(id = 2, resource = Resource.Entry),
            link(id = 3, deleted = Deleted.Moderator),
            link(id = 4, title = " "),
            link(id = 5),
            link(id = 6),
        ).toHitsWidgetItems(limit = 2)

        assertEquals(listOf(1, 5), items.map { it.id })
    }

    @Test
    fun `leaves out adult thumbnails and blank sources`() {
        val item = listOf(link(id = 1, adult = true, source = "")).toHitsWidgetItems().single()

        assertNull(item.thumbnailUrl)
        assertNull(item.source)
    }

    @Test
    fun `older snapshots without the hot flag read as not hot`() {
        val item = Json.decodeFromString<HitsWidgetItem>(
            """{"id":1,"title":"Link 1","votes":5,"comments":0}""",
        )

        assertFalse(item.isHot)
    }

    @Test
    fun `needs a refresh without data or once the data is stale`() {
        val snapshot = HitsWidgetSnapshot(updatedAtMillis = 0, items = emptyList())
        val twoHours = 2 * 60 * 60 * 1000L

        assertTrue(HitsWidgetState().needsRefresh(nowMillis = 0))
        assertFalse(HitsWidgetState(isRefreshing = true).needsRefresh(nowMillis = 0))
        assertFalse(HitsWidgetState(snapshot = snapshot).needsRefresh(nowMillis = twoHours - 1))
        assertTrue(HitsWidgetState(snapshot = snapshot).needsRefresh(nowMillis = twoHours))
    }

    private fun link(
        id: Int,
        title: String = "Link $id",
        resource: Resource = Resource.Link,
        deleted: Deleted = Deleted.None,
        adult: Boolean = false,
        hot: Boolean = false,
        source: String = "example.com",
    ) = ResourceItem(
        actions = null,
        adult = adult,
        archive = false,
        author = null,
        comments = Comments(count = 34, hot = false, items = emptyList()),
        content = "",
        createdAt = null,
        deleted = deleted,
        deletable = false,
        description = "",
        editable = false,
        hot = hot,
        id = id,
        media = Media(
            embed = null,
            photo = Photo(
                height = 0,
                key = "",
                label = "",
                mimeType = "image/jpeg",
                size = 0,
                url = "https://example.com/$id.jpg",
                width = 0,
            ),
            survey = null,
        ),
        name = "",
        parent = null,
        publishedAt = null,
        recommended = false,
        resource = resource,
        slug = "",
        source = Source(label = source, type = "", typeId = 0, url = "https://$source"),
        tags = emptyList(),
        title = title,
        voted = Voted.None,
        votes = Votes(count = 120, down = 0, up = 120),
        favourite = false,
    )
}

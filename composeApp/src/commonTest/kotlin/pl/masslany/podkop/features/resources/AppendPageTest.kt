package pl.masslany.podkop.features.resources

import kotlin.test.Test
import kotlin.test.assertEquals
import pl.masslany.podkop.business.common.domain.models.common.Deleted
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Voted
import pl.masslany.podkop.features.resources.models.ResourceItemState
import pl.masslany.podkop.features.resources.models.toResourceItemState

class AppendPageTest {

    @Test
    fun `a later page skips items already listed`() {
        // The homepage repeats its promoted link and entries on every page.
        val listed = listOf(resource(9000, Resource.Link), resource(101, Resource.Link), resource(9001, Resource.Entry))
            .map { it.toResourceItemState() }

        val result = listed.appendPage(
            listOf(resource(9000, Resource.Link), resource(201, Resource.Link), resource(9001, Resource.Entry)),
        ) { it.toResourceItemState() }

        assertEquals(listOf(9000, 101, 9001, 201), result.map { it.id })
    }

    @Test
    fun `an item repeated within a page is added once`() {
        val result = emptyList<ResourceItemState>().appendPage(
            listOf(resource(1, Resource.Link), resource(1, Resource.Link), resource(2, Resource.Entry)),
        ) { it.toResourceItemState() }

        assertEquals(listOf(1, 2), result.map { it.id })
    }

    @Test
    fun `listed items keep their state`() {
        val listed = listOf(resource(1, Resource.Link).copy(content = "listed")).map { it.toResourceItemState() }

        val result = listed.appendPage(listOf(resource(1, Resource.Link).copy(content = "repeated"))) { it.toResourceItemState() }

        assertEquals(listed, result)
    }

    private fun resource(id: Int, resource: Resource) = ResourceItem(
        actions = null, adult = false, archive = false, author = null, comments = null,
        content = "c$id", createdAt = null, deleted = Deleted.None, deletable = false,
        description = "", editable = false, hot = false, id = id, media = null, name = "",
        parent = null, publishedAt = null, recommended = false, resource = resource, slug = "",
        source = null, tags = emptyList(), title = "", voted = Voted.None, votes = null, favourite = false,
    )
}

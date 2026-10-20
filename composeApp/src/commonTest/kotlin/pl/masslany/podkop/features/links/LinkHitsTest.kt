package pl.masslany.podkop.features.links

import kotlin.test.Test
import kotlin.test.assertEquals
import pl.masslany.podkop.business.common.domain.models.common.Deleted
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Voted
import pl.masslany.podkop.features.links.hits.models.HitItemState

class LinkHitsTest {

    private val sut = LinkHits()

    @Test
    fun `hits keep only links`() {
        sut.replace(listOf(resource(1, Resource.Link), resource(2, Resource.Entry), resource(3, Resource.Link)))

        assertEquals(listOf(1, 3), sut.items.value.map { it.id })
    }

    @Test
    fun `a refreshed link replaces only its own hit`() {
        sut.replace(listOf(resource(1, Resource.Link), resource(3, Resource.Link)))

        sut.refresh(resource(1, Resource.Link).copy(title = "refreshed"))

        assertEquals(listOf("refreshed", "t3"), sut.items.value.map { (it as HitItemState).titleState.title })
    }

    @Test
    fun `a refreshed entry leaves hits alone`() {
        sut.replace(listOf(resource(1, Resource.Link)))
        val before = sut.items.value

        sut.refresh(resource(1, Resource.Entry).copy(title = "entry"))

        assertEquals(before, sut.items.value)
    }

    private fun resource(id: Int, resource: Resource) = ResourceItem(
        actions = null, adult = false, archive = false, author = null, comments = null,
        content = "c$id", createdAt = null, deleted = Deleted.None, deletable = false,
        description = "", editable = false, hot = false, id = id, media = null, name = "",
        parent = null, publishedAt = null, recommended = false, resource = resource, slug = "",
        source = null, tags = emptyList(), title = "t$id", voted = Voted.None, votes = null, favourite = false,
    )
}

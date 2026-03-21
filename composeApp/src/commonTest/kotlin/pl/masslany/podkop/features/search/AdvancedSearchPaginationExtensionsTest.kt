package pl.masslany.podkop.features.search

import kotlin.test.Test
import kotlin.test.assertEquals
import pl.masslany.podkop.business.common.domain.models.common.Deleted
import pl.masslany.podkop.business.common.domain.models.common.Pagination
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.common.domain.models.common.Voted
import pl.masslany.podkop.common.pagination.PageRequest

class AdvancedSearchPaginationExtensionsTest {

    @Test
    fun `withSearchFallbackPagination synthesizes next page when total is not reached`() {
        val resources = resources(
            data = List(20) { index ->
                resourceItem(id = 1000 + index)
            },
            pagination = Pagination(
                perPage = 40,
                total = 73,
                next = "",
                prev = "",
            ),
        )

        val actual = resources.withSearchFallbackPagination(
            currentItemCount = 38,
            request = PageRequest.Cursor("2"),
        )

        assertEquals("3", actual.pagination?.next)
    }

    @Test
    fun `withSearchFallbackPagination keeps blank next when total is reached`() {
        val resources = resources(
            data = List(15) { index ->
                resourceItem(id = 2000 + index)
            },
            pagination = Pagination(
                perPage = 40,
                total = 73,
                next = "",
                prev = "",
            ),
        )

        val actual = resources.withSearchFallbackPagination(
            currentItemCount = 58,
            request = PageRequest.Cursor("3"),
        )

        assertEquals("", actual.pagination?.next)
    }
}

private fun resources(
    data: List<ResourceItem>,
    pagination: Pagination,
) = Resources(
    data = data,
    pagination = pagination,
)

private fun resourceItem(id: Int) = ResourceItem(
    actions = null,
    adult = false,
    archive = false,
    author = null,
    comments = null,
    content = "",
    createdAt = null,
    deleted = Deleted.None,
    deletable = false,
    description = "",
    editable = false,
    hot = false,
    id = id,
    media = null,
    name = "",
    parent = null,
    publishedAt = null,
    recommended = false,
    resource = Resource.Link,
    slug = "slug-$id",
    source = null,
    tags = emptyList(),
    title = "title-$id",
    voted = Voted.None,
    votes = null,
    favourite = false,
)

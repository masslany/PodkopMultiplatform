package pl.masslany.podkop.business.common.data.main.mapper.links

import kotlin.test.Test
import kotlin.test.assertEquals
import pl.masslany.podkop.business.common.domain.models.common.Deleted
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.Voted
import pl.masslany.podkop.business.testsupport.fixtures.BusinessFixtures as Fixtures

class LinkMapperTest {

    @Test
    fun `single resource response mapper wraps mapped resource item`() {
        val dto = Fixtures.singleResourceResponseDto(
            data = Fixtures.resourceItemDto(
                id = 777,
                resource = "link",
                voted = 1,
                deleted = "author",
            ),
        )

        assertEquals(
            Fixtures.link(
                data = Fixtures.resourceItem(
                    id = 777,
                    resource = Resource.Link,
                    voted = Voted.Positive,
                    deleted = Deleted.Author,
                ),
            ),
            dto.toLink(),
        )
    }
}

package pl.masslany.podkop.business.tags.data.main.mapper

import kotlin.test.Test
import kotlin.test.assertEquals
import pl.masslany.podkop.business.testsupport.fixtures.BusinessFixtures as Fixtures

class TagsAutoCompleteMapperTest {

    @Test
    fun `tags auto complete mapper maps response items`() {
        val dto = Fixtures.tagsAutoCompleteResponseDto(
            data = listOf(
                Fixtures.tagsAutoCompleteDataDto(name = "#kotlin", observedQuantity = 11),
                Fixtures.tagsAutoCompleteDataDto(name = "#compose", observedQuantity = 22),
            ),
        )

        assertEquals(
            Fixtures.tagsAutoComplete(
                tags = listOf(
                    Fixtures.tagsAutoCompleteItem(name = "#kotlin", observedQuantity = 11),
                    Fixtures.tagsAutoCompleteItem(name = "#compose", observedQuantity = 22),
                ),
            ),
            dto.toTagsAutoComplete(),
        )
    }
}

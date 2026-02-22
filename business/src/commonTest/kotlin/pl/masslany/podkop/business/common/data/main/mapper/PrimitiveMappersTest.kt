package pl.masslany.podkop.business.common.data.main.mapper

import kotlin.test.Test
import kotlin.test.assertEquals
import pl.masslany.podkop.business.common.domain.models.common.Gender
import pl.masslany.podkop.business.common.domain.models.common.NameColor
import pl.masslany.podkop.business.common.domain.models.common.Resource

class PrimitiveMappersTest {

    @Test
    fun `gender mapper maps known and fallback values`() {
        assertEquals(Gender.Male, "m".toGender())
        assertEquals(Gender.Female, "f".toGender())
        assertEquals(Gender.Unspecified, "x".toGender())
        assertEquals(Gender.Unspecified, null.toGender())
    }

    @Test
    fun `name color mapper maps known and fallback values`() {
        assertEquals(NameColor.Orange, "orange".toNameColor())
        assertEquals(NameColor.Burgundy, "burgundy".toNameColor())
        assertEquals(NameColor.Green, "green".toNameColor())
        assertEquals(NameColor.Black, "black".toNameColor())
        assertEquals(NameColor.Orange, "unknown".toNameColor())
    }

    @Test
    fun `resource mapper maps known and fallback values`() {
        assertEquals(Resource.Link, "link".toResource())
        assertEquals(Resource.Entry, "entry".toResource())
        assertEquals(Resource.EntryComment, "entry_comment".toResource())
        assertEquals(Resource.LinkComment, "link_comment".toResource())
        assertEquals(Resource.Unknown, "other".toResource())
        assertEquals(Resource.Unknown, null.toResource())
    }
}

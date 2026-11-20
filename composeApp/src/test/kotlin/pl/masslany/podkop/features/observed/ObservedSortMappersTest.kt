package pl.masslany.podkop.features.observed

import kotlin.test.Test
import kotlin.test.assertEquals
import pl.masslany.podkop.business.observed.domain.models.request.ObservedType
import pl.masslany.podkop.common.models.DropdownMenuItemType

class ObservedSortMappersTest {

    @Test
    fun `type mapper converts both directions`() {
        assertEquals(DropdownMenuItemType.Everything, ObservedType.All.toDropdownMenuItemType())
        assertEquals(DropdownMenuItemType.Profiles, ObservedType.Profiles.toDropdownMenuItemType())
        assertEquals(
            DropdownMenuItemType.Discussions,
            ObservedType.Discussions.toDropdownMenuItemType(),
        )
        assertEquals(DropdownMenuItemType.Tags, ObservedType.Tags.toDropdownMenuItemType())

        assertEquals(ObservedType.All, DropdownMenuItemType.Everything.toObservedType())
        assertEquals(ObservedType.Profiles, DropdownMenuItemType.Profiles.toObservedType())
        assertEquals(ObservedType.Discussions, DropdownMenuItemType.Discussions.toObservedType())
        assertEquals(ObservedType.Tags, DropdownMenuItemType.Tags.toObservedType())
    }
}

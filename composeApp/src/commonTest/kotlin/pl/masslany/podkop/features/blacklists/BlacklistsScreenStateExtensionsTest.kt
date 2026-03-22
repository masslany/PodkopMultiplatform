package pl.masslany.podkop.features.blacklists

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import pl.masslany.podkop.business.blacklists.domain.models.BlacklistedDomain
import pl.masslany.podkop.business.blacklists.domain.models.BlacklistedTag
import pl.masslany.podkop.business.blacklists.domain.models.BlacklistedUser
import pl.masslany.podkop.business.common.domain.models.common.Gender
import pl.masslany.podkop.business.common.domain.models.common.NameColor
import pl.masslany.podkop.business.profile.domain.models.UserAutoCompleteItem
import pl.masslany.podkop.business.tags.domain.models.TagsAutoCompleteItem
import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.avatar.AvatarType

class BlacklistsScreenStateExtensionsTest {

    @Test
    fun `blacklisted user maps to item state with avatar and name color`() {
        val result = BlacklistedUser(
            username = "kobiaszu",
            createdAt = "2026-03-22 11:20:22",
            gender = Gender.Male,
            color = NameColor.Orange,
            avatarUrl = "https://wykop.pl/avatar.jpg",
        ).toItemState()

        assertEquals("kobiaszu", result.username)
        assertEquals(NameColorType.Orange, result.nameColorType)
        assertEquals("https://wykop.pl/avatar.jpg", assertIs<AvatarType.NetworkImage>(result.avatarState.type).url)
    }

    @Test
    fun `blacklisted tag maps to item state`() {
        val result = BlacklistedTag(
            name = "heheszki",
            createdAt = "2026-03-22 11:20:22",
        ).toItemState()

        assertEquals("heheszki", result.name)
        assertEquals("#heheszki", result.displayLabel)
    }

    @Test
    fun `blacklisted domain maps to item state`() {
        val result = BlacklistedDomain(
            domain = "devkop.pl",
            createdAt = "2026-03-22 11:20:22",
        ).toItemState()

        assertEquals("devkop.pl", result.domain)
        assertEquals("domain:devkop.pl", result.key)
    }

    @Test
    fun `user autocomplete item maps to suggestion state with no avatar fallback`() {
        val result = UserAutoCompleteItem(
            username = "m__b",
            avatarUrl = "",
            gender = Gender.Unspecified,
            color = NameColor.Burgundy,
        ).toSuggestionItemState()

        assertEquals("m__b", result.username)
        assertEquals(NameColorType.Burgundy, result.nameColorType)
        assertEquals(AvatarType.NoAvatar, result.avatarState.type)
    }

    @Test
    fun `tag autocomplete item maps followers count`() {
        val result = TagsAutoCompleteItem(
            name = "android",
            observedQuantity = 321,
        ).toSuggestionItemState()

        assertEquals("android", result.name)
        assertEquals(321, result.followers)
    }
}

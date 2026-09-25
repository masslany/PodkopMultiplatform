package pl.masslany.podkop.business.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import pl.masslany.podkop.business.common.domain.models.common.Gender
import pl.masslany.podkop.business.common.domain.models.common.NameColor
import pl.masslany.podkop.business.profile.domain.models.Profile
import pl.masslany.podkop.business.profile.domain.models.ProfilePermissions
import pl.masslany.podkop.business.profile.domain.models.Summary
import pl.masslany.podkop.business.profile.domain.models.permissionsFor

class ProfilePermissionsTest {
    private fun profile(canManageObservation: Boolean = true) = Profile(
        name = "Ewa",
        avatarUrl = "",
        rankPosition = null,
        gender = Gender.Unspecified,
        color = NameColor.Orange,
        backgroundUrl = "",
        summary = Summary(0, 0, 0, 0, 0, 0),
        memberSince = null,
        isObserved = false,
        isBlacklisted = false,
        canManageObservation = canManageObservation,
    )

    @Test
    fun `guests can do nothing`() {
        assertEquals(
            ProfilePermissions(false, false, false, false),
            profile().permissionsFor(isLoggedIn = false, viewerUsername = null),
        )
    }

    @Test
    fun `own profile is matched case-insensitively and allows no actions`() {
        assertEquals(
            ProfilePermissions(true, false, false, false),
            profile().permissionsFor(isLoggedIn = true, viewerUsername = "ewa"),
        )
    }

    @Test
    fun `another profile respects the server observation capability`() {
        assertEquals(
            ProfilePermissions(false, false, true, true),
            profile(canManageObservation = false).permissionsFor(isLoggedIn = true, viewerUsername = "adam"),
        )
    }

    @Test
    fun `unknown viewer can still message when observation is allowed`() {
        assertEquals(
            ProfilePermissions(false, true, true, true),
            profile().permissionsFor(isLoggedIn = true, viewerUsername = null),
        )
        assertEquals(
            ProfilePermissions(false, false, true, false),
            profile(canManageObservation = false).permissionsFor(isLoggedIn = true, viewerUsername = null),
        )
    }
}

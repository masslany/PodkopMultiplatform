package pl.masslany.podkop.common.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * Base contract for any screen in the app.
 */
interface NavTarget

/**
 * Marker: Targets implementing this will open a non-dismissible full-screen overlay.
 * Used for Critical Errors, Global Loading.
 */
interface OverlayTarget : NavTarget

/**
 * Data class defining a tab in the Bottom Navigation Bar.
 */
data class TopLevelDestination(
    val root: NavTarget,
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
    val badgeCount: Int = 0,
    val enabled: Boolean = true,
)

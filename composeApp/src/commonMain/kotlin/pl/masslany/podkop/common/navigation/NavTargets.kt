package pl.masslany.podkop.common.navigation

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

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
 * Marker: Represents the "Container" for the logged-in experience.
 * When the Root Stack sees this, it renders the Tab/BottomBar UI.
 */
interface MainAppTarget : NavTarget

/**
 * Data class defining a tab in the Bottom Navigation Bar.
 */
data class TopLevelDestination(
    val root: NavTarget,
    val iconRes: DrawableResource,
    val labelRes: StringResource,
    val badgeCount: Int = 0,
    val enabled: Boolean = true,
)
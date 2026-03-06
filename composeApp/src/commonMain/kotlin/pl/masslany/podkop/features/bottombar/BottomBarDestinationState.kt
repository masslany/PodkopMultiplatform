package pl.masslany.podkop.features.bottombar

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import pl.masslany.podkop.common.navigation.NavTarget

data class BottomBarDestinationState(
    val screen: NavTarget,
    val isSelected: Boolean,
    val isEnabled: Boolean,
    val iconRes: DrawableResource,
    val labelRes: StringResource,
    val badgeCount: Int,
)

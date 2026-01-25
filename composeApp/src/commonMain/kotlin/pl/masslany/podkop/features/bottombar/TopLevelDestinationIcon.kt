package pl.masslany.podkop.features.bottombar

import org.jetbrains.compose.resources.DrawableResource
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.ic_nav_letter_m
import podkop.composeapp.generated.resources.ic_nav_shovel


enum class TopLevelDestinationIcon(
    val iconRes: DrawableResource,
) {
    Links(iconRes = Res.drawable.ic_nav_letter_m),
    Upcoming(iconRes = Res.drawable.ic_nav_shovel),
}
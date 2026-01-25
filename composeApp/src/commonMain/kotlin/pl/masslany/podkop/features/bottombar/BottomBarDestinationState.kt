package pl.masslany.podkop.features.bottombar

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import pl.masslany.podkop.common.navigation.NavTarget
import pl.masslany.podkop.features.links.LinksScreen
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.home
import podkop.composeapp.generated.resources.ic_nav_shovel

data class BottomBarDestinationState(
    val screen: NavTarget,
    val isSelected: Boolean,
    val isEnabled: Boolean,
    val iconRes: DrawableResource,
    val labelRes: StringResource,
) {
    companion object {
        val initial = BottomBarDestinationState(
            screen = LinksScreen,
            isSelected = false,
            isEnabled = true,
            iconRes = Res.drawable.ic_nav_shovel,
            labelRes = Res.string.home,
        )
    }
}

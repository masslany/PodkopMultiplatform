package pl.masslany.podkop.features.bottombar

import androidx.compose.runtime.Stable
import pl.masslany.podkop.common.navigation.NavTarget

@Stable
interface BottomBarActions {
    fun onScreenChanged(screen: NavTarget)
}

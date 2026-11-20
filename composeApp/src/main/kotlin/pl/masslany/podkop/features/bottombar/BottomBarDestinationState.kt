package pl.masslany.podkop.features.bottombar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import pl.masslany.podkop.common.navigation.NavTarget

data class BottomBarDestinationState(
    val screen: NavTarget,
    val isSelected: Boolean,
    val isEnabled: Boolean,
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
    val badgeCount: Int,
)

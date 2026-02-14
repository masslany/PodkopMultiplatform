package pl.masslany.podkop.features.topbar

import androidx.compose.runtime.Stable

@Stable
interface TopBarActions {
    fun onTopBarBackClicked()

    fun onTopBarProfileClicked()

    fun onTopBarSearchClicked()
}

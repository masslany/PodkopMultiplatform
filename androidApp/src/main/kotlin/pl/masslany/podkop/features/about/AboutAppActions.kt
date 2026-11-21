package pl.masslany.podkop.features.about

import androidx.compose.runtime.Stable

@Stable
interface AboutAppActions {
    fun onCloseClicked()

    fun onOpenLinkClicked(url: String)
}

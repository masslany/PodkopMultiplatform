package pl.masslany.podkop.features.profile

import androidx.compose.runtime.Stable
import pl.masslany.podkop.features.topbar.TopBarActions

@Stable
interface ProfileActions : TopBarActions {
    fun onLoginClicked()
}

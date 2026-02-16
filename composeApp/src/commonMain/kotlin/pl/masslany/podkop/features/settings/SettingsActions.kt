package pl.masslany.podkop.features.settings

import androidx.compose.runtime.Stable
import pl.masslany.podkop.features.topbar.TopBarActions

@Stable
interface SettingsActions : TopBarActions {

    fun onAutoplayGifsChanged(enabled: Boolean)

    fun onDebugToolsClicked()
}

package pl.masslany.podkop.features.settings.preview

import pl.masslany.podkop.common.preview.NoOpTopBarActions
import pl.masslany.podkop.common.settings.ThemeOverride
import pl.masslany.podkop.features.settings.SettingsActions
import pl.masslany.podkop.features.topbar.TopBarActions

object NoOpSettingsActions : SettingsActions, TopBarActions by NoOpTopBarActions {
    override fun onAutoplayGifsChanged(enabled: Boolean) = Unit
    override fun onThemeOverrideChanged(value: ThemeOverride) = Unit
    override fun onDynamicColorsChanged(enabled: Boolean) = Unit
    override fun onDebugToolsClicked() = Unit
    override fun onLogoutClicked() = Unit
}

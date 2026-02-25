package pl.masslany.podkop.common.preview

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pl.masslany.podkop.common.settings.AppSettings
import pl.masslany.podkop.common.settings.ThemeOverride

class FakeAppSettings(
    autoplayGifsInitial: Boolean = true,
    themeOverrideInitial: ThemeOverride = ThemeOverride.AUTO,
    dynamicColorsEnabledInitial: Boolean = true,
) : AppSettings {
    private val _autoplayGifs = MutableStateFlow(autoplayGifsInitial)
    private val _themeOverride = MutableStateFlow(themeOverrideInitial)
    private val _dynamicColorsEnabled = MutableStateFlow(dynamicColorsEnabledInitial)

    override val autoplayGifs: Flow<Boolean> = _autoplayGifs
    override val themeOverride: Flow<ThemeOverride> = _themeOverride
    override val dynamicColorsEnabled: Flow<Boolean> = _dynamicColorsEnabled

    override suspend fun setAutoplayGifs(enabled: Boolean) {
        _autoplayGifs.value = enabled
    }

    override suspend fun setThemeOverride(value: ThemeOverride) {
        _themeOverride.value = value
    }

    override suspend fun setDynamicColorsEnabled(enabled: Boolean) {
        _dynamicColorsEnabled.value = enabled
    }
}

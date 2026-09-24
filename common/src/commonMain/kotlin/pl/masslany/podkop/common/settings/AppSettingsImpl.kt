package pl.masslany.podkop.common.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import pl.masslany.podkop.common.persistence.api.KeyValueStorage

class AppSettingsImpl(private val keyValueStorage: KeyValueStorage) : AppSettings {
    override val autoplayGifs: Flow<Boolean> =
        keyValueStorage.observeBoolean(AUTOPLAY_GIFS_KEY)
            .map { it ?: true }
            .distinctUntilChanged()

    override val themeOverride: Flow<ThemeOverride> =
        keyValueStorage.observeString(THEME_OVERRIDE_KEY)
            .map { rawValue -> rawValue?.let(::themeOverrideFromStorageValue) ?: ThemeOverride.AUTO }
            .distinctUntilChanged()

    override val dynamicColorsEnabled: Flow<Boolean> =
        keyValueStorage.observeBoolean(DYNAMIC_COLORS_ENABLED_KEY)
            .map { it ?: true }
            .distinctUntilChanged()

    override suspend fun setAutoplayGifs(enabled: Boolean) {
        keyValueStorage.putBoolean(AUTOPLAY_GIFS_KEY, enabled)
    }

    override suspend fun setThemeOverride(value: ThemeOverride) {
        keyValueStorage.putString(THEME_OVERRIDE_KEY, value.name)
    }

    override suspend fun setDynamicColorsEnabled(enabled: Boolean) {
        keyValueStorage.putBoolean(DYNAMIC_COLORS_ENABLED_KEY, enabled)
    }

    private fun themeOverrideFromStorageValue(value: String): ThemeOverride =
        ThemeOverride.entries.firstOrNull { entry -> entry.name == value } ?: ThemeOverride.AUTO

    private companion object {
        const val AUTOPLAY_GIFS_KEY = "AUTOPLAY_GIFS"
        const val THEME_OVERRIDE_KEY = "THEME_OVERRIDE"
        const val DYNAMIC_COLORS_ENABLED_KEY = "DYNAMIC_COLORS_ENABLED"
    }
}

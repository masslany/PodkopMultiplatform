package pl.masslany.podkop.common.settings

import kotlinx.coroutines.flow.Flow

enum class ThemeOverride {
    AUTO,
    LIGHT,
    DARK,
}

interface AppSettings {
    val autoplayGifs: Flow<Boolean>
    val themeOverride: Flow<ThemeOverride>
    val dynamicColorsEnabled: Flow<Boolean>
    val playVideosInline: Flow<Boolean>

    suspend fun setAutoplayGifs(enabled: Boolean)

    suspend fun setThemeOverride(value: ThemeOverride)

    suspend fun setDynamicColorsEnabled(enabled: Boolean)

    suspend fun setPlayVideosInline(enabled: Boolean)
}

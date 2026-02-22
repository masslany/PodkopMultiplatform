package pl.masslany.podkop.common.settings

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow

enum class ThemeOverride {
    AUTO,
    LIGHT,
    DARK,
}

@Stable
interface AppSettings {
    val autoplayGifs: Flow<Boolean>
    val themeOverride: Flow<ThemeOverride>
    val dynamicColorsEnabled: Flow<Boolean>

    suspend fun setAutoplayGifs(enabled: Boolean)

    suspend fun setThemeOverride(value: ThemeOverride)

    suspend fun setDynamicColorsEnabled(enabled: Boolean)
}

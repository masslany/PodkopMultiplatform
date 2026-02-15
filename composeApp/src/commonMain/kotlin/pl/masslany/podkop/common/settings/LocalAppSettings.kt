package pl.masslany.podkop.common.settings

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppSettings = staticCompositionLocalOf<AppSettings> {
    error("No AppSettings provided")
}

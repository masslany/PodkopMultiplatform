package pl.masslany.podkop.common.platform

import androidx.compose.runtime.Composable

expect class PlatformClipboard {
    suspend fun setText(text: String)
}

@Composable
expect fun rememberPlatformClipboard(): PlatformClipboard

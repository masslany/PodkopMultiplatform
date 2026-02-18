package pl.masslany.podkop.common.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

internal actual val supportsPlatformGifImage: Boolean = false

@Composable
internal actual fun PlatformGifImage(
    modifier: Modifier,
    url: String,
    onSuccess: () -> Unit,
    onError: () -> Unit,
) {
    // Not used on Android.
}

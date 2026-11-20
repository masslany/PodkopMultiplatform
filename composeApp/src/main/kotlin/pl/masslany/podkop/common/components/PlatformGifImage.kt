package pl.masslany.podkop.common.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

internal val supportsPlatformGifImage: Boolean = false

@Composable
internal fun PlatformGifImage(
    modifier: Modifier = Modifier,
    url: String,
    onSuccess: () -> Unit,
    onError: () -> Unit,
) {
    // Not used on Android.
}

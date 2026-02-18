package pl.masslany.podkop.common.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

internal expect val supportsPlatformGifImage: Boolean

@Composable
internal expect fun PlatformGifImage(
    modifier: Modifier = Modifier,
    url: String,
    onSuccess: () -> Unit,
    onError: () -> Unit,
)

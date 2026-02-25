package pl.masslany.podkop.common.navigation

import androidx.compose.runtime.Composable
import androidx.navigationevent.DirectNavigationEventInput

@Composable
internal actual fun rememberPlatformBackNavigationInput(enabled: Boolean): DirectNavigationEventInput? = null

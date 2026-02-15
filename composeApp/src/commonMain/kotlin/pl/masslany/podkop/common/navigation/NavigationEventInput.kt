package pl.masslany.podkop.common.navigation

import androidx.compose.runtime.Composable
import androidx.navigationevent.DirectNavigationEventInput

@Composable
expect fun rememberPlatformBackNavigationInput(enabled: Boolean): DirectNavigationEventInput?

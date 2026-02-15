package pl.masslany.podkop.common.navigation

import androidx.compose.ui.Modifier
import androidx.navigationevent.DirectNavigationEventInput

actual fun Modifier.edgeSwipeBackGesture(
    enabled: Boolean,
    navigationInput: DirectNavigationEventInput?,
): Modifier = this

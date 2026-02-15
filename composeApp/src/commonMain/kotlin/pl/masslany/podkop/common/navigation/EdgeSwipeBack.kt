package pl.masslany.podkop.common.navigation

import androidx.compose.ui.Modifier
import androidx.navigationevent.DirectNavigationEventInput

expect fun Modifier.edgeSwipeBackGesture(
    enabled: Boolean,
    navigationInput: DirectNavigationEventInput?,
    onBack: () -> Unit,
): Modifier

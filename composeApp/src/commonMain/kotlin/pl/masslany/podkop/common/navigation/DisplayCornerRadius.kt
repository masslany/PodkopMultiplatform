package pl.masslany.podkop.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

/** The radius of the display's corners, so a shrinking full-screen surface keeps their shape. */
@Composable
internal expect fun rememberDisplayCornerRadius(): Dp

package pl.masslany.podkop.common.snackbar

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("SnackbarHostState is not provided")
}

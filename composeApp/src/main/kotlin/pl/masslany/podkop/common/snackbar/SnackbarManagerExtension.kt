package pl.masslany.podkop.common.snackbar

import pl.masslany.podkop.composeapp.R

fun SnackbarManager.tryEmitGenericError(): Boolean = tryEmit(
    event = SnackbarEvent(
        message = SnackbarMessage.Resource(R.string.snackbar_generic_error),
        isFinite = true,
    ),
)

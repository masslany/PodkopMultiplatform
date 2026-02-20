package pl.masslany.podkop.common.snackbar

import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.snackbar_generic_error

fun SnackbarManager.tryEmitGenericError(): Boolean = tryEmit(
    event = SnackbarEvent(
        message = SnackbarMessage.Resource(Res.string.snackbar_generic_error),
        isFinite = true,
    ),
)

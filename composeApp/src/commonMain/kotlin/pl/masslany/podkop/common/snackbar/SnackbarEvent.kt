package pl.masslany.podkop.common.snackbar

data class SnackbarEvent(
    val message: SnackbarMessage,
    val actionLabel: SnackbarMessage? = null,
    val withDismissAction: Boolean = false,
    val isFinite: Boolean = true,
)

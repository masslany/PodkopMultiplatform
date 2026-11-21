package pl.masslany.podkop.common.snackbar

import androidx.annotation.StringRes

sealed interface SnackbarMessage {
    data class Resource(@StringRes val resource: Int, val args: List<Any> = emptyList()) : SnackbarMessage

    data class Raw(val value: String) : SnackbarMessage
}

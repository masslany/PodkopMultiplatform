package pl.masslany.podkop.common.snackbar

import org.jetbrains.compose.resources.StringResource

sealed interface SnackbarMessage {
    data class Resource(val resource: StringResource, val args: List<Any> = emptyList()) : SnackbarMessage

    data class Raw(val value: String) : SnackbarMessage
}

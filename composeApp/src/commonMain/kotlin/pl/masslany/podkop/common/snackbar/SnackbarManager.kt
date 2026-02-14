package pl.masslany.podkop.common.snackbar

import kotlinx.coroutines.flow.SharedFlow

interface SnackbarManager {
    val events: SharedFlow<SnackbarEvent>

    suspend fun emit(event: SnackbarEvent)

    fun tryEmit(event: SnackbarEvent): Boolean
}

package pl.masslany.podkop.common.snackbar

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class SnackbarManagerImpl : SnackbarManager {

    private val eventsFlow = MutableSharedFlow<SnackbarEvent>(
        replay = REPLAY_COUNT,
        extraBufferCapacity = BUFFER_CAPACITY,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val events: SharedFlow<SnackbarEvent> = eventsFlow.asSharedFlow()

    override suspend fun emit(event: SnackbarEvent) {
        eventsFlow.emit(event)
    }

    override fun tryEmit(event: SnackbarEvent): Boolean = eventsFlow.tryEmit(event)

    private companion object {
        const val REPLAY_COUNT = 0
        const val BUFFER_CAPACITY = 32
    }
}

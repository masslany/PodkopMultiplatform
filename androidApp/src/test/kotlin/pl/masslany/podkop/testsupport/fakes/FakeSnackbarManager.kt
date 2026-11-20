package pl.masslany.podkop.testsupport.fakes

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import pl.masslany.podkop.common.snackbar.SnackbarEvent
import pl.masslany.podkop.common.snackbar.SnackbarManager

class FakeSnackbarManager : SnackbarManager {
    private val mutableEvents = MutableSharedFlow<SnackbarEvent>(extraBufferCapacity = 16)

    val emittedEvents = mutableListOf<SnackbarEvent>()

    override val events: SharedFlow<SnackbarEvent> = mutableEvents

    override suspend fun emit(event: SnackbarEvent) {
        emittedEvents += event
        mutableEvents.emit(event)
    }

    override fun tryEmit(event: SnackbarEvent): Boolean {
        emittedEvents += event
        return mutableEvents.tryEmit(event)
    }
}

package pl.masslany.podkop.common.deeplink

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface AuthSessionEvent {
    data object TokensUpdated : AuthSessionEvent
}

interface AuthSessionEvents {
    val events: SharedFlow<AuthSessionEvent>

    fun tryEmit(event: AuthSessionEvent): Boolean
}

internal class AuthSessionEventsImpl : AuthSessionEvents {
    private val eventsFlow = MutableSharedFlow<AuthSessionEvent>(
        extraBufferCapacity = 1,
    )

    override val events: SharedFlow<AuthSessionEvent> = eventsFlow.asSharedFlow()

    override fun tryEmit(event: AuthSessionEvent): Boolean = eventsFlow.tryEmit(event)
}

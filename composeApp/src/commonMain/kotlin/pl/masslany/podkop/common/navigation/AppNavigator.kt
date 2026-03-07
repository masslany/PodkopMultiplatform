package pl.masslany.podkop.common.navigation

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppNavigator(
    private val configProvider: NavigationConfigProvider,
    private val scope: CoroutineScope,
    private val externalBrowser: ExternalBrowser,
) {
    private val destinationBackHandlers = mutableMapOf<NavTarget, () -> Boolean>()

    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    private val _state = MutableStateFlow(NavigationState())
    val state = _state.asStateFlow()

    val results = MutableSharedFlow<Pair<String, Any>>(extraBufferCapacity = 1)

    init {
        scope.launch {
            val startTarget = configProvider.resolveStartDestination()
            _state.update { it.copy(rootStack = persistentListOf(startTarget), overlay = OverlayState.None) }
            _isReady.value = true
        }
    }

    // --- Navigation ---

    fun navigateTo(target: NavTarget) {
        _state.update { previousState ->
            if (target is OverlayTarget) {
                return@update previousState.copy(overlay = OverlayState.Blocking(target))
            }

            pushToActiveStack(previousState, target)
        }
    }

    fun registerBackHandler(destination: NavTarget, handler: () -> Boolean) {
        destinationBackHandlers[destination] = handler
    }

    fun unregisterBackHandler(destination: NavTarget) {
        destinationBackHandlers.remove(destination)
    }

    fun dismissOverlay() {
        _state.update { it.copy(overlay = OverlayState.None) }
    }

    /**
     * Navigates back and publishes the result.
     */
    fun sendResult(key: String, result: Any) {
        back()
        publishResult(key = key, result = result)
    }

    fun publishResult(key: String, result: Any) {
        results.tryEmit(key to result)
    }

    /**
     * Shows a dialog and suspends until a result with the matching [key] is received.
     * usage: val result = navigator.awaitResult<Boolean>(MyDialog(key = "123"))
     */
    suspend inline fun <reified T> awaitResult(target: NavTarget, key: String): T {
        // 1. Show it
        navigateTo(target)

        // 2. Wait for the specific key
        val resultPair = results.first { it.first == key }

        // 3. Cast and return
        return resultPair.second as T
    }

    /**
     * Handles Back Press.
     * @return true if consumed, false if Activity should finish.
     */
    fun back(): Boolean {
        val currentState = _state.value
        val currentDestination = currentState.rootStack.lastOrNull()
        val destinationBackHandler = currentDestination?.let { destinationBackHandlers[it] }
        if (destinationBackHandler?.invoke() == true) {
            return true
        }

        if (currentState.rootStack.size <= 1) {
            return false
        }

        _state.update { previousState ->
            if (previousState.rootStack.size <= 1) {
                previousState
            } else {
                previousState.copy(
                    rootStack = previousState.rootStack.dropLast(1).toPersistentList(),
                )
            }
        }
        return true
    }

    fun openExternalLink(url: String) {
        externalBrowser.open(url)
    }

    // --- Helpers ---

    private fun pushToActiveStack(state: NavigationState, target: NavTarget): NavigationState {
        if (state.rootStack.lastOrNull() == target) return state
        return state.copy(rootStack = (state.rootStack.toMutableList() + target).toPersistentList())
    }
}

package pl.masslany.podkop.common.navigation

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
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
    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    private val _state = MutableStateFlow(NavigationState())
    val state = _state.asStateFlow()

    val results = MutableSharedFlow<Pair<String, Any>>(extraBufferCapacity = 1)

    init {
        scope.launch {
            val startTarget = configProvider.resolveStartDestination()

            if (startTarget == HomeScreen) {
                initializeHome()
            } else {
                _state.update { it.copy(rootStack = persistentListOf(startTarget)) }
                _isReady.value = true
            }
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

    fun switchTab(root: NavTarget) {
        _state.update { previousState ->
            if (previousState.homeState != null) {
                previousState.copy(homeState = previousState.homeState.copy(currentTabRoot = root))
            } else {
                previousState
            }
        }
    }

    fun dismissOverlay() {
        _state.update { it.copy(overlay = OverlayState.None) }
    }

    /**
     * Navigates back and publishes the result.
     */
    fun sendResult(key: String, result: Any) {
        back()
        // Emit to the flow so the awaiting caller gets it
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
        var consumed = false
        _state.update { previousState ->
            if (previousState.rootStack.size > 1) {
                consumed = true
                previousState.copy(
                    rootStack = previousState.rootStack.dropLast(1).toPersistentList(),
                )
            } else {
                val homeState = previousState.homeState
                val firstTab = homeState?.availableDestinations?.firstOrNull()?.root

                if (
                    previousState.rootStack.lastOrNull() == HomeScreen &&
                    homeState != null &&
                    firstTab != null &&
                    homeState.currentTabRoot != firstTab
                ) {
                    consumed = true
                    previousState.copy(homeState = homeState.copy(currentTabRoot = firstTab))
                } else {
                    consumed = false
                    previousState
                }
            }
        }
        return consumed
    }

    fun openExternalLink(url: String) {
        externalBrowser.open(url)
    }

    // --- Helpers ---

    private fun pushToActiveStack(state: NavigationState, target: NavTarget): NavigationState {
        if (state.rootStack.lastOrNull() == target) return state
        return state.copy(rootStack = (state.rootStack.toMutableList() + target).toPersistentList())
    }

    private suspend fun initializeHome() {
        val destinations = configProvider.topLevelDestinations.first()
        val startDestination = destinations.firstOrNull()?.root
        val stacks = destinations.associate { it.root to persistentListOf(it.root) }.toPersistentMap()

        _state.update { previousState ->
            previousState.copy(
                rootStack = persistentListOf(HomeScreen),
                homeState = startDestination?.let {
                    HomeState(
                        availableDestinations = destinations,
                        currentTabRoot = it,
                        stacks = stacks,
                    )
                },
                overlay = OverlayState.None,
            )
        }
        _isReady.value = true
    }
}

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

            if (startTarget is MainAppTarget) {
                initializeMainApp(startTarget)
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

    /**
     * Completely switches the root context (e.g. Login -> Onboarding -> Main).
     */
    fun setRoot(target: NavTarget) {
        if (target is MainAppTarget) {
            scope.launch { initializeMainApp(target) }
        } else {
            _state.update {
                it.copy(rootStack = persistentListOf(target), tabState = null, overlay = OverlayState.None)
            }
        }
    }

    fun switchTab(root: NavTarget) {
        _state.update { previousState ->
            if (previousState.tabState != null) {
                previousState.copy(tabState = previousState.tabState.copy(currentTabRoot = root))
            } else {
                previousState
            }
        }
    }

    fun setBottomBarVisible(visible: Boolean) {
        println("MEOW setBottomBarVisible $visible")
        if (_state.value.isBottomBarVisible != visible) {
            println("MEOW setBottomBarVisible in if")
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
            // 1. Tab Logic
            if (previousState.isTabMode && previousState.tabState != null) {
                val currentRoot = previousState.tabState.currentTabRoot
                val stack = previousState.tabState.stacks[currentRoot]!!

                if (stack.size > 1) {
                    // There are screens on the current tab's stack - pop the last one
                    consumed = true
                    val newStacks = previousState.tabState.stacks +
                        (currentRoot to stack.dropLast(1).toPersistentList())
                    previousState.copy(tabState = previousState.tabState.copy(stacks = newStacks.toPersistentMap()))
                } else {
                    // We are at the root of a tab (e.g., "Upcoming")
                    val firstTab = previousState.tabState.availableTabs.firstOrNull()?.root
                    if (firstTab != null && currentRoot != firstTab) {
                        // If not on the first tab, switch back to the first tab (e.g., "Home")
                        consumed = true
                        previousState.copy(tabState = previousState.tabState.copy(currentTabRoot = firstTab))
                    } else {
                        // Already on the first tab's root, let the system handle exit
                        consumed = false
                        previousState
                    }
                }
            }
            // 2. Root Linear Logic (Non-tab mode)
            else {
                if (previousState.rootStack.size > 1) {
                    consumed = true
                    previousState.copy(
                        rootStack = previousState.rootStack.dropLast(1).toPersistentList(),
                    )
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
        return if (state.isTabMode && state.tabState != null) {
            val currentRoot = state.tabState.currentTabRoot
            val stack = state.tabState.stacks[currentRoot] ?: persistentListOf(currentRoot)
            if (stack.lastOrNull() == target) return state // Single Top

            val newStacks = state.tabState.stacks + (currentRoot to (stack + target).toPersistentList())
            state.copy(tabState = state.tabState.copy(stacks = newStacks.toPersistentMap()))
        } else {
            if (state.rootStack.lastOrNull() == target) return state
            state.copy(rootStack = (state.rootStack.toMutableList() + target).toPersistentList())
        }
    }

    private suspend fun initializeMainApp(mainTarget: MainAppTarget) {
        val tabs = configProvider.topLevelDestinations.first()
        if (tabs.isEmpty()) return

        val startTab = tabs.first().root
        val initialStacks = tabs.associate { it.root to persistentListOf(it.root) }.toPersistentMap()

        _state.update {
            it.copy(
                rootStack = persistentListOf(mainTarget),
                tabState = TabState(tabs, startTab, initialStacks),
                overlay = OverlayState.None,
            )
        }
        _isReady.value = true
    }
}

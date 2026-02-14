package pl.masslany.podkop.features.home

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.masslany.podkop.common.navigation.NavTarget
import pl.masslany.podkop.common.navigation.NavigationConfigProvider
import pl.masslany.podkop.common.navigation.TopLevelDestination

data class HomeNavigatorState(
    val destinations: ImmutableList<TopLevelDestination> = persistentListOf(),
    val currentTabRoot: NavTarget? = null,
    val stacks: ImmutableMap<NavTarget, ImmutableList<NavTarget>> = persistentMapOf(),
)

class HomeNavigator(private val configProvider: NavigationConfigProvider) : AutoCloseable {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _state = MutableStateFlow(HomeNavigatorState())
    val state = _state.asStateFlow()

    init {
        scope.launch {
            configProvider.topLevelDestinations.collect { destinations ->
                updateDestinations(destinations)
            }
        }
    }

    fun onTabChanged(root: NavTarget) {
        _state.update { previous ->
            if (root == previous.currentTabRoot) return@update previous
            if (root !in previous.stacks) return@update previous
            previous.copy(currentTabRoot = root)
        }
    }

    fun onBack(): Boolean {
        val currentState = _state.value
        val firstTab = currentState.destinations.firstOrNull()?.root ?: return false
        val currentTab = currentState.currentTabRoot ?: return false
        if (currentTab == firstTab) return false

        _state.update { it.copy(currentTabRoot = firstTab) }
        return true
    }

    override fun close() {
        scope.cancel()
    }

    private fun updateDestinations(destinations: ImmutableList<TopLevelDestination>) {
        if (destinations.isEmpty()) {
            _state.value = HomeNavigatorState()
            return
        }

        _state.update { previous ->
            val firstTab = destinations.first().root
            val stacks = destinations
                .associate { destination ->
                    destination.root to (previous.stacks[destination.root] ?: persistentListOf(destination.root))
                }
                .toPersistentMap()
            val currentTab = previous.currentTabRoot
                ?.takeIf { it in stacks }
                ?: firstTab

            HomeNavigatorState(
                destinations = destinations,
                currentTabRoot = currentTab,
                stacks = stacks,
            )
        }
    }
}

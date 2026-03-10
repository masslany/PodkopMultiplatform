package pl.masslany.podkop.features.home

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
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
import pl.masslany.podkop.features.entries.EntriesScreen
import pl.masslany.podkop.features.entrydetails.EntryDetailsScreen
import pl.masslany.podkop.features.linkdetails.LinkDetailsScreen
import pl.masslany.podkop.features.links.LinksScreen
import pl.masslany.podkop.features.upcoming.UpcomingScreen

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

    fun restoreState(serializedState: String?) {
        val restoredState = serializedState
            ?.let(HomeNavigatorStateSerializer::deserialize)
            ?: return

        _state.update { previous ->
            previous.copy(
                currentTabRoot = restoredState.currentTabRoot,
                stacks = restoredState.stacks,
            )
        }
    }

    fun serializeState(): String? = HomeNavigatorStateSerializer.serialize(_state.value)

    fun onTabChanged(root: NavTarget) {
        _state.update { previous ->
            if (root == previous.currentTabRoot) return@update previous
            if (root !in previous.stacks) return@update previous
            previous.copy(currentTabRoot = root)
        }
    }

    fun navigateToLinkDetails(id: Int) {
        navigateToCurrentTab(LinkDetailsScreen(id))
    }

    fun navigateToEntryDetails(id: Int) {
        navigateToEntryDetails(EntryDetailsScreen.forEntry(id))
    }

    fun navigateToEntryDetails(screen: EntryDetailsScreen) {
        navigateToCurrentTab(screen)
    }

    fun clearInlineDetailsDestinations() {
        _state.update { previous ->
            val updatedStacks = previous.stacks
                .mapValues { (root, stack) ->
                    val filteredStack = stack
                        .filterNot { destination -> destination.isInlineDetailsTarget() }
                        .toPersistentList()

                    if (filteredStack.isEmpty()) {
                        persistentListOf(root)
                    } else {
                        filteredStack
                    }
                }
                .toPersistentMap()

            if (updatedStacks == previous.stacks) {
                previous
            } else {
                previous.copy(stacks = updatedStacks)
            }
        }
    }

    fun detachCurrentInlineDetailsDestination(): NavTarget? {
        var detachedDestination: NavTarget? = null

        _state.update { previous ->
            val currentTab = previous.currentTabRoot ?: return@update previous
            val currentStack = previous.stacks[currentTab] ?: persistentListOf(currentTab)
            val inlineDetailsDestination = currentStack.lastOrNull()
                ?.takeIf { destination -> destination.isInlineDetailsTarget() }
                ?: return@update previous

            detachedDestination = inlineDetailsDestination
            val filteredStack = currentStack
                .filterNot { destination -> destination.isInlineDetailsTarget() }
            val updatedStack = if (filteredStack.isEmpty()) {
                persistentListOf(currentTab)
            } else {
                filteredStack.toPersistentList()
            }
            val updatedStacks = previous.stacks
                .toMutableMap()
                .apply {
                    this[currentTab] = updatedStack
                }
                .toPersistentMap()

            previous.copy(stacks = updatedStacks)
        }

        return detachedDestination
    }

    fun onBack(): Boolean {
        val currentState = _state.value
        val currentTab = currentState.currentTabRoot ?: return false
        val currentStack = currentState.stacks[currentTab] ?: persistentListOf(currentTab)

        if (currentStack.size > 1) {
            _state.update { previous ->
                val stack = previous.stacks[currentTab] ?: return@update previous
                val updatedStacks = previous.stacks
                    .toMutableMap()
                    .apply {
                        this[currentTab] = stack.dropLast(1).toPersistentList()
                    }
                    .toPersistentMap()
                previous.copy(
                    stacks = updatedStacks,
                )
            }
            return true
        }

        val firstTab = currentState.destinations.firstOrNull()?.root ?: return false
        if (currentTab == firstTab) {
            return false
        }

        _state.update { previous ->
            previous.copy(currentTabRoot = firstTab)
        }
        return true
    }

    fun currentStack(): ImmutableList<NavTarget> {
        val currentTab = _state.value.currentTabRoot ?: return persistentListOf()
        return _state.value.stacks[currentTab] ?: persistentListOf(currentTab)
    }

    override fun close() {
        scope.cancel()
    }

    private fun navigateToCurrentTab(target: NavTarget) {
        _state.update { previous ->
            val currentTab = previous.currentTabRoot ?: return@update previous
            if (target.isInlineDetailsTarget() && !currentTab.supportsInlineDetails()) {
                return@update previous
            }

            val currentStack = previous.stacks[currentTab] ?: persistentListOf(currentTab)
            val normalizedStack = if (target.isInlineDetailsTarget()) {
                currentStack.filterNot { destination -> destination.isInlineDetailsTarget() }
            } else {
                currentStack
            }

            if (normalizedStack.lastOrNull() == target) {
                return@update previous
            }

            val updatedStacks = previous.stacks
                .toMutableMap()
                .apply {
                    this[currentTab] = (normalizedStack + target).toPersistentList()
                }
                .toPersistentMap()

            previous.copy(
                stacks = updatedStacks,
            )
        }
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

private fun NavTarget.supportsInlineDetails(): Boolean =
    this is LinksScreen || this is UpcomingScreen || this is EntriesScreen

private fun NavTarget.isInlineDetailsTarget(): Boolean = this is LinkDetailsScreen || this is EntryDetailsScreen

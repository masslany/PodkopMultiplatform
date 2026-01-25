package pl.masslany.podkop.common.navigation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

interface NavigationConfigProvider {
    /**
     * Called at startup to decide where to go.
     * e.g. Check Auth Token -> Return Login or MainApp
     */
    suspend fun resolveStartDestination(): NavTarget

    /**
     * Emits the list of tabs. Can be dynamic (e.g. feature flags).
     */
    val topLevelDestinations: Flow<ImmutableList<TopLevelDestination>>
}
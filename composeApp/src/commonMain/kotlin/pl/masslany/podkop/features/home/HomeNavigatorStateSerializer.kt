package pl.masslany.podkop.features.home

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import pl.masslany.podkop.common.navigation.NavTarget
import pl.masslany.podkop.common.navigation.NavigationBackstackSerializer

internal data class RestoredHomeNavigatorState(
    val currentTabRoot: NavTarget,
    val stacks: ImmutableMap<NavTarget, ImmutableList<NavTarget>>,
)

internal object HomeNavigatorStateSerializer {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun serialize(state: HomeNavigatorState): String? {
        val currentTabRoot = state.currentTabRoot ?: return null

        return runCatching {
            json.encodeToString(
                HomeNavigatorSavedState(
                    currentTabRootPayload = requireNotNull(serializeTarget(currentTabRoot)),
                    stackPayloads = state.stacks.map { (root, backStack) ->
                        HomeNavigatorStackPayload(
                            rootPayload = requireNotNull(serializeTarget(root)),
                            backStackPayload = requireNotNull(NavigationBackstackSerializer.serialize(backStack)),
                        )
                    },
                ),
            )
        }.getOrNull()
    }

    fun deserialize(payload: String): RestoredHomeNavigatorState? = runCatching {
        val savedState = json.decodeFromString<HomeNavigatorSavedState>(payload)
        val currentTabRoot = deserializeTarget(savedState.currentTabRootPayload) ?: return null
        val stacks = savedState.stackPayloads.associate { stackPayload ->
            val root = requireNotNull(deserializeTarget(stackPayload.rootPayload))
            val backStack = NavigationBackstackSerializer.deserialize(stackPayload.backStackPayload)
                ?.toPersistentList()
                ?.takeIf(List<NavTarget>::isNotEmpty)
                ?: return null
            root to backStack
        }.toPersistentMap()

        RestoredHomeNavigatorState(
            currentTabRoot = currentTabRoot,
            stacks = stacks,
        )
    }.getOrNull()

    private fun serializeTarget(target: NavTarget): String? = NavigationBackstackSerializer
        .serialize(listOf(target))

    private fun deserializeTarget(payload: String): NavTarget? = NavigationBackstackSerializer
        .deserialize(payload)
        ?.singleOrNull()
}

@Serializable
private data class HomeNavigatorSavedState(
    val currentTabRootPayload: String,
    val stackPayloads: List<HomeNavigatorStackPayload>,
)

@Serializable
private data class HomeNavigatorStackPayload(val rootPayload: String, val backStackPayload: String)

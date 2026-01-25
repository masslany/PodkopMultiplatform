package pl.masslany.podkop.common.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationContent(
    state: NavigationState,
    onBack: () -> Unit,
    entryProvider: (NavTarget) -> NavEntry<NavTarget>,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val rootTarget = state.rootStack.lastOrNull()

        if (rootTarget is MainAppTarget && state.tabState != null) {
            val holder = rememberSaveableStateHolder()
            // Save state for switched-out tabs
            holder.SaveableStateProvider(state.tabState.currentTabRoot.toString()) {
                val stack = state.tabState.stacks[state.tabState.currentTabRoot]
                    ?: persistentListOf()
                if (stack.isNotEmpty()) {
                    GenericNavDisplay(
                        backStack = stack,
                        entryProvider = entryProvider,
                        onBack = { onBack() },
                    )
                }
            }
        } else if (rootTarget != null) {
            GenericNavDisplay(
                backStack = state.rootStack,
                entryProvider = entryProvider,
                onBack = { onBack() },
            )
        }
    }

    when (val ov = state.overlay) {
        OverlayState.None -> Unit
        is OverlayState.Blocking -> {
            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                )
            ) {
                GenericNavDisplay(
                    backStack = persistentListOf(ov.target),
                    entryProvider = entryProvider,
                    onBack = { },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun GenericNavDisplay(
    backStack: ImmutableList<NavTarget>,
    entryProvider: (NavTarget) -> NavEntry<NavTarget>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bottomSheetStrategy = remember { BottomSheetSceneStrategy<NavTarget>() }
    val dialogSceneStrategy = remember { DialogSceneStrategy<NavTarget>() }

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        sceneStrategy = bottomSheetStrategy then dialogSceneStrategy,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = { entryProvider(it) },
        onBack = onBack,
    )
}


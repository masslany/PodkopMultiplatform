package pl.masslany.podkop.common.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    Box(modifier = Modifier.fillMaxSize()) {
        if (state.rootStack.isNotEmpty()) {
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
                ),
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
    val canNavigateBack = backStack.size > 1
    val navigationInput = rememberPlatformBackNavigationInput(enabled = canNavigateBack)

    NavDisplay(
        modifier = modifier.edgeSwipeBackGesture(
            enabled = canNavigateBack,
            navigationInput = navigationInput,
            onBack = onBack,
        ),
        backStack = backStack,
        sceneStrategy = bottomSheetStrategy then dialogSceneStrategy,
        transitionSpec = {
            ContentTransform(
                targetContentEnter = EnterTransition.None,
                initialContentExit = ExitTransition.None,
            )
        },
        popTransitionSpec = {
            ContentTransform(
                targetContentEnter = EnterTransition.None,
                initialContentExit = ExitTransition.None,
            )
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = { entryProvider(it) },
        onBack = onBack,
    )
}

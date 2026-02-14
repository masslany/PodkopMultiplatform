package pl.masslany.podkop.features.home

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import kotlinx.collections.immutable.ImmutableList
import org.koin.compose.viewmodel.koinViewModel
import pl.masslany.podkop.common.navigation.BottomSheetSceneStrategy
import pl.masslany.podkop.common.navigation.NavTarget
import pl.masslany.podkop.common.navigation.bottombar.BottomBarScrollBehavior
import pl.masslany.podkop.common.navigation.bottombar.LocalBottomBarScrollBehavior
import pl.masslany.podkop.common.snackbar.LocalAppSnackbarHostState
import pl.masslany.podkop.features.bottombar.BottomBarRoot
import pl.masslany.podkop.features.entries.EntriesScreen
import pl.masslany.podkop.features.entries.EntriesScreenRoot
import pl.masslany.podkop.features.links.LinksScreen
import pl.masslany.podkop.features.links.LinksScreenRoot
import pl.masslany.podkop.features.upcoming.UpcomingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenRoot(
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<HomeViewModel>()
    val snackbarHostState = LocalAppSnackbarHostState.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bottomBarBehavior = remember { BottomBarScrollBehavior() }

    CompositionLocalProvider(LocalBottomBarScrollBehavior provides bottomBarBehavior) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            bottomBar = {
                if (state.destinations.isNotEmpty()) {
                    BottomBarRoot(
                        destinations = state.destinations,
                        onScreenChanged = viewModel::onTabChanged,
                        modifier = Modifier
                            .onSizeChanged {
                                bottomBarBehavior.heightPx = it.height.toFloat()
                            }
                            .graphicsLayer {
                                translationY = bottomBarBehavior.offsetPx
                            },
                    )
                }
            },
        ) { contentPadding ->
            val holder = rememberSaveableStateHolder()
            holder.SaveableStateProvider(state.currentTabKey) {
                HomeNavDisplay(
                    backStack = state.currentStack,
                    contentPadding = contentPadding,
                )
            }
        }
    }
}

@Composable
private fun HomeNavDisplay(
    backStack: ImmutableList<NavTarget>,
    contentPadding: PaddingValues,
) {
    val bottomSheetStrategy = remember { BottomSheetSceneStrategy<NavTarget>() }
    val dialogSceneStrategy = remember { DialogSceneStrategy<NavTarget>() }
    val provider = entryProvider {
        entry<LinksScreen> {
            LinksScreenRoot(
                isUpcoming = false,
                paddingValues = contentPadding,
            )
        }

        entry<UpcomingScreen> {
            LinksScreenRoot(
                isUpcoming = true,
                paddingValues = contentPadding,
            )
        }

        entry<EntriesScreen> {
            EntriesScreenRoot(
                paddingValues = contentPadding,
            )
        }
    }

    if (backStack.isNotEmpty()) {
        NavDisplay(
            modifier = Modifier.fillMaxSize(),
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
            entryProvider = { provider(it) },
            onBack = { },
        )
    }
}

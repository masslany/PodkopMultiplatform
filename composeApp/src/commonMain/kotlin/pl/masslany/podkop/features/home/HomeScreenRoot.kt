package pl.masslany.podkop.features.home

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pl.masslany.podkop.common.extensions.rememberWindowSizeClass
import pl.masslany.podkop.common.navigation.BottomSheetSceneStrategy
import pl.masslany.podkop.common.navigation.HomeListDetailSceneStrategy
import pl.masslany.podkop.common.navigation.NavTarget
import pl.masslany.podkop.common.navigation.bottombar.BottomBarScrollBehavior
import pl.masslany.podkop.common.navigation.bottombar.LocalBottomBarScrollBehavior
import pl.masslany.podkop.common.snackbar.LocalAppSnackbarHostState
import pl.masslany.podkop.features.bottombar.BottomBarRoot
import pl.masslany.podkop.features.bottombar.SideBarRoot
import pl.masslany.podkop.features.entries.EntriesScreen
import pl.masslany.podkop.features.entries.EntriesScreenRoot
import pl.masslany.podkop.features.entrydetails.EntryDetailsScreen
import pl.masslany.podkop.features.entrydetails.EntryDetailsScreenRoot
import pl.masslany.podkop.features.linkdetails.LinkDetailsScreen
import pl.masslany.podkop.features.linkdetails.LinkDetailsScreenRoot
import pl.masslany.podkop.features.links.LinksScreen
import pl.masslany.podkop.features.links.LinksScreenRoot
import pl.masslany.podkop.features.upcoming.UpcomingScreen
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.home_link_details_placeholder_body
import podkop.composeapp.generated.resources.home_link_details_placeholder_title

private const val HomeKeyLinksList = "home:list:links"
private const val HomeKeyUpcomingList = "home:list:upcoming"
private const val HomeKeyEntriesList = "home:list:entries"
private const val HomeKeyLinkDetailsPrefix = "home:details:link:"
private const val HomeKeyEntryDetailsPrefix = "home:details:entry:"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenRoot(
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<HomeViewModel>()
    val snackbarHostState = LocalAppSnackbarHostState.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val windowSizeClass = rememberWindowSizeClass()
    val useInlineDetails = windowSizeClass.widthSizeClass > WindowWidthSizeClass.Medium
    val useBottomBar = windowSizeClass.widthSizeClass <= WindowWidthSizeClass.Medium
    val bottomBarBehavior = rememberSaveable(saver = BottomBarScrollBehavior.Saver) {
        BottomBarScrollBehavior()
    }
    val hasInlineDetailsInCurrentStack = remember(state.currentStack) {
        state.currentStack.any { target -> target.isInlineHomeDetailsTarget() }
    }
    val inlineSceneEnabled = useInlineDetails || hasInlineDetailsInCurrentStack

    LaunchedEffect(useInlineDetails) {
        viewModel.onInlineDetailsModeChanged(enabled = useInlineDetails)
    }

    LaunchedEffect(useBottomBar) {
        if (!useBottomBar) {
            bottomBarBehavior.reset()
        }
    }
    val holder = rememberSaveableStateHolder()
    val currentStackState = rememberUpdatedState(newValue = state.currentStack)
    val currentTabKeyState = rememberUpdatedState(newValue = state.currentTabKey)
    val inlineSceneEnabledState = rememberUpdatedState(newValue = inlineSceneEnabled)
    val useInlineDetailsState = rememberUpdatedState(newValue = useInlineDetails)

    CompositionLocalProvider(LocalBottomBarScrollBehavior provides bottomBarBehavior) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            bottomBar = {
                if (useBottomBar && state.destinations.isNotEmpty()) {
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
            val contentPaddingState = rememberUpdatedState(newValue = contentPadding)
            val homeNavContent = remember(holder, viewModel) {
                movableContentOf {
                    holder.SaveableStateProvider(currentTabKeyState.value) {
                        HomeNavDisplay(
                            backStack = currentStackState.value,
                            contentPadding = contentPaddingState.value,
                            inlineSceneEnabled = inlineSceneEnabledState.value,
                            onBack = viewModel::onBackPressedInHome,
                            onLinkClicked = { id ->
                                viewModel.onLinkClicked(
                                    id = id,
                                    useInlineDetails = useInlineDetailsState.value,
                                )
                            },
                            onEntryClicked = { id ->
                                viewModel.onEntryClicked(
                                    id = id,
                                    useInlineDetails = useInlineDetailsState.value,
                                )
                            },
                        )
                    }
                }
            }

            if (useBottomBar || state.destinations.isEmpty()) {
                homeNavContent()
            } else {
                Row(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    SideBarRoot(
                        destinations = state.destinations,
                        onScreenChanged = viewModel::onTabChanged,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(
                                top = contentPadding.calculateTopPadding(),
                                bottom = contentPadding.calculateBottomPadding(),
                            ),
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    ) {
                        homeNavContent()
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeNavDisplay(
    backStack: ImmutableList<NavTarget>,
    contentPadding: PaddingValues,
    inlineSceneEnabled: Boolean,
    onBack: () -> Unit,
    onLinkClicked: (Int) -> Unit,
    onEntryClicked: (Int) -> Unit,
) {
    val bottomSheetStrategy = remember { BottomSheetSceneStrategy<NavTarget>() }
    val dialogSceneStrategy = remember { DialogSceneStrategy<NavTarget>() }
    val listDetailSceneStrategy = remember(inlineSceneEnabled) {
        HomeListDetailSceneStrategy<NavTarget>(
            enabled = inlineSceneEnabled,
            isListTarget = { target ->
                target == HomeKeyLinksList ||
                    target == HomeKeyUpcomingList ||
                    target == HomeKeyEntriesList
            },
            isDetailTarget = { target ->
                (target as? String)?.startsWith(HomeKeyLinkDetailsPrefix) == true ||
                    (target as? String)?.startsWith(HomeKeyEntryDetailsPrefix) == true
            },
            detailsPlaceholder = {
                HomeLinkDetailsPlaceholder()
            },
        )
    }
    val provider = entryProvider {
        entry<LinksScreen>(
            clazzContentKey = { HomeKeyLinksList },
        ) {
            LinksScreenRoot(
                isUpcoming = false,
                paddingValues = contentPadding,
                onLinkClicked = onLinkClicked,
            )
        }

        entry<UpcomingScreen>(
            clazzContentKey = { HomeKeyUpcomingList },
        ) {
            LinksScreenRoot(
                isUpcoming = true,
                paddingValues = contentPadding,
                onLinkClicked = onLinkClicked,
            )
        }

        entry<EntriesScreen>(
            clazzContentKey = { HomeKeyEntriesList },
        ) {
            EntriesScreenRoot(
                paddingValues = contentPadding,
                onEntryClicked = onEntryClicked,
            )
        }

        entry<LinkDetailsScreen>(
            clazzContentKey = { screen ->
                "$HomeKeyLinkDetailsPrefix${screen.id}"
            },
        ) {
            LinkDetailsScreenRoot(
                id = it.id,
                paddingValues = contentPadding,
                showTopBar = !inlineSceneEnabled,
            )
        }

        entry<EntryDetailsScreen>(
            clazzContentKey = { screen ->
                "$HomeKeyEntryDetailsPrefix${screen.id}"
            },
        ) {
            EntryDetailsScreenRoot(
                id = it.id,
                paddingValues = contentPadding,
                showTopBar = !inlineSceneEnabled,
            )
        }
    }

    if (backStack.isNotEmpty()) {
        NavDisplay(
            modifier = Modifier.fillMaxSize(),
            backStack = backStack,
            sceneStrategy = bottomSheetStrategy then dialogSceneStrategy then listDetailSceneStrategy,
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
            predictivePopTransitionSpec = { _ ->
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
            onBack = onBack,
        )
    }
}

private fun NavTarget.isInlineHomeDetailsTarget(): Boolean = this is LinkDetailsScreen || this is EntryDetailsScreen

@Composable
private fun HomeLinkDetailsPlaceholder(
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        OutlinedCard {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(resource = Res.string.home_link_details_placeholder_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(resource = Res.string.home_link_details_placeholder_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

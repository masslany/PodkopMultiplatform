package pl.masslany.podkop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.DialogSceneStrategy
import org.koin.compose.koinInject
import pl.masslany.podkop.common.components.dialog.DefaultGenericDialog
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.navigation.GenericDialog
import pl.masslany.podkop.common.navigation.NavigationContent
import pl.masslany.podkop.common.navigation.bottombar.BottomBarScrollBehavior
import pl.masslany.podkop.common.navigation.bottombar.LocalBottomBarScrollBehavior
import pl.masslany.podkop.common.theme.PodkopTheme
import pl.masslany.podkop.features.bottombar.BottomBarRoot
import pl.masslany.podkop.features.entries.EntriesScreen
import pl.masslany.podkop.features.entries.EntriesScreenRoot
import pl.masslany.podkop.features.links.LinksScreen
import pl.masslany.podkop.features.links.LinksScreenRoot
import pl.masslany.podkop.features.upcoming.UpcomingScreen

@Composable
fun App() {
    PodkopTheme {
        val appNavigator = koinInject<AppNavigator>()

        MainApp(
            appNavigator = appNavigator,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainApp(
    modifier: Modifier = Modifier,
    appNavigator: AppNavigator,
) {
    val state by appNavigator.state.collectAsStateWithLifecycle()
    val bottomBarBehavior = remember { BottomBarScrollBehavior() }

    CompositionLocalProvider(LocalBottomBarScrollBehavior provides bottomBarBehavior) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            bottomBar = {
                if (state.isTabMode && state.tabState != null) {
                    BottomBarRoot(
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
            Box(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                NavigationContent(
                    state = state,
                    onBack = { appNavigator.back() },
                    entryProvider = entryProvider {
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
                        entry<GenericDialog>(
                            metadata = DialogSceneStrategy.dialog(
                                DialogProperties(),
                            ),
                        ) {
                            DefaultGenericDialog(
                                dialog = it,
                                navigator = appNavigator,
                            )
                        }
                    },
                )
            }
        }
    }
}

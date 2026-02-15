package pl.masslany.podkop

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.DialogSceneStrategy
import org.jetbrains.compose.resources.getString
import org.koin.compose.koinInject
import pl.masslany.podkop.business.startup.api.StartupManager
import pl.masslany.podkop.business.startup.models.AppState
import pl.masslany.podkop.common.components.dialog.DefaultGenericDialog
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.navigation.GenericDialog
import pl.masslany.podkop.common.navigation.HomeScreen
import pl.masslany.podkop.common.navigation.NavigationContent
import pl.masslany.podkop.common.snackbar.LocalAppSnackbarHostState
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.SnackbarMessage
import pl.masslany.podkop.common.theme.PodkopTheme
import pl.masslany.podkop.features.entrydetails.EntryDetailsScreen
import pl.masslany.podkop.features.entrydetails.EntryDetailsScreenRoot
import pl.masslany.podkop.features.home.HomeScreenRoot
import pl.masslany.podkop.features.imageviewer.ImageViewerScreen
import pl.masslany.podkop.features.imageviewer.ImageViewerScreenRoot

@Composable
fun App() {
    PodkopTheme {
        val startupManager = koinInject<StartupManager>()
        val appNavigator = koinInject<AppNavigator>()
        val snackbarManager = koinInject<SnackbarManager>()
        val startupState by startupManager.state.collectAsStateWithLifecycle()
        val state by appNavigator.state.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }

        if (startupState !is AppState.Ready) {
            return@PodkopTheme
        }

        LaunchedEffect(snackbarManager) {
            snackbarManager.events.collect { event ->
                snackbarHostState.showSnackbar(
                    message = event.message.resolveMessage(),
                    actionLabel = event.actionLabel?.resolveMessage(),
                    withDismissAction = event.withDismissAction,
                    duration = if (event.isFinite) SnackbarDuration.Short else SnackbarDuration.Indefinite,
                )
            }
        }

        CompositionLocalProvider(LocalAppSnackbarHostState provides snackbarHostState) {
            NavigationContent(
                state = state,
                onBack = { appNavigator.back() },
                entryProvider = entryProvider {
                    entry<HomeScreen> {
                        HomeScreenRoot()
                    }

                    entry<EntryDetailsScreen> {
                        EntryDetailsScreenRoot(
                            id = it.id,
                            paddingValues = WindowInsets.systemBars.asPaddingValues(),
                        )
                    }

                    entry<ImageViewerScreen> {
                        ImageViewerScreenRoot(
                            imageUrl = it.imageUrl,
                            paddingValues = WindowInsets.systemBars.asPaddingValues(),
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

private suspend fun SnackbarMessage.resolveMessage(): String = when (this) {
    is SnackbarMessage.Raw -> value
    is SnackbarMessage.Resource -> getString(resource = resource, *args.toTypedArray())
}

package pl.masslany.podkop

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.DialogSceneStrategy
import org.koin.compose.koinInject
import pl.masslany.podkop.common.components.dialog.DefaultGenericDialog
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.navigation.GenericDialog
import pl.masslany.podkop.common.navigation.HomeScreen
import pl.masslany.podkop.common.navigation.NavigationContent
import pl.masslany.podkop.common.theme.PodkopTheme
import pl.masslany.podkop.features.entrydetails.EntryDetailsScreen
import pl.masslany.podkop.features.entrydetails.EntryDetailsScreenRoot
import pl.masslany.podkop.features.home.HomeScreenRoot

@Composable
fun App() {
    PodkopTheme {
        val appNavigator = koinInject<AppNavigator>()
        val state by appNavigator.state.collectAsStateWithLifecycle()

        NavigationContent(
            state = state,
            onBack = { appNavigator.back() },
            entryProvider = entryProvider {
                entry<HomeScreen> {
                    HomeScreenRoot(
                        state = state.homeState,
                    )
                }

                entry<EntryDetailsScreen> {
                    EntryDetailsScreenRoot(
                        id = it.id,
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

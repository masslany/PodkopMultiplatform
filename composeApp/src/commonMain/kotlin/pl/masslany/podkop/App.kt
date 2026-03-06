package pl.masslany.podkop

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.DialogSceneStrategy
import org.jetbrains.compose.resources.getString
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import pl.masslany.podkop.business.startup.models.AppState
import pl.masslany.podkop.common.components.dialog.DefaultGenericDialog
import pl.masslany.podkop.common.composer.composermedia.ComposerMediaAttachBottomSheetScreen
import pl.masslany.podkop.common.composer.composermedia.ComposerMediaAttachBottomSheetScreenRoot
import pl.masslany.podkop.common.composer.composermedia.ComposerMediaPickLocalScreen
import pl.masslany.podkop.common.composer.composermedia.ComposerMediaPickLocalScreenRoot
import pl.masslany.podkop.common.composer.composermedia.ComposerMediaUrlDialogScreen
import pl.masslany.podkop.common.composer.composermedia.ComposerMediaUrlDialogScreenRoot
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.navigation.BottomSheetSceneStrategy
import pl.masslany.podkop.common.navigation.GenericDialog
import pl.masslany.podkop.common.navigation.HomeScreen
import pl.masslany.podkop.common.navigation.NavigationContent
import pl.masslany.podkop.common.settings.AppSettings
import pl.masslany.podkop.common.settings.LocalAppSettings
import pl.masslany.podkop.common.settings.ThemeOverride
import pl.masslany.podkop.common.snackbar.LocalAppSnackbarHostState
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.SnackbarMessage
import pl.masslany.podkop.common.theme.PodkopTheme
import pl.masslany.podkop.common.theme.SystemAppearance
import pl.masslany.podkop.features.about.AboutAppScreen
import pl.masslany.podkop.features.about.AboutAppScreenRoot
import pl.masslany.podkop.features.composer.ComposerBottomSheetScreen
import pl.masslany.podkop.features.composer.ComposerBottomSheetScreenRoot
import pl.masslany.podkop.features.debug.DebugScreen
import pl.masslany.podkop.features.debug.DebugScreenRoot
import pl.masslany.podkop.features.entrydetails.EntryDetailsScreen
import pl.masslany.podkop.features.entrydetails.EntryDetailsScreenRoot
import pl.masslany.podkop.features.favorites.FavoritesScreen
import pl.masslany.podkop.features.favorites.FavoritesScreenRoot
import pl.masslany.podkop.features.hits.HitsScreen
import pl.masslany.podkop.features.hits.HitsScreenRoot
import pl.masslany.podkop.features.home.HomeScreenRoot
import pl.masslany.podkop.features.imageviewer.ImageViewerScreen
import pl.masslany.podkop.features.imageviewer.ImageViewerScreenRoot
import pl.masslany.podkop.features.linkdetails.LinkDetailsScreen
import pl.masslany.podkop.features.linkdetails.LinkDetailsScreenRoot
import pl.masslany.podkop.features.profile.ProfileScreen
import pl.masslany.podkop.features.profile.ProfileScreenRoot
import pl.masslany.podkop.features.resourceactions.ResourceActionsBottomSheetScreen
import pl.masslany.podkop.features.resourceactions.ResourceActionsBottomSheetScreenRoot
import pl.masslany.podkop.features.resourceactions.ResourceScreenshotPreviewDialogScreen
import pl.masslany.podkop.features.resourceactions.ResourceScreenshotPreviewDialogScreenRoot
import pl.masslany.podkop.features.resourceactions.ResourceVotesBottomSheetScreen
import pl.masslany.podkop.features.resourceactions.ResourceVotesBottomSheetScreenRoot
import pl.masslany.podkop.features.search.SearchScreen
import pl.masslany.podkop.features.search.SearchScreenRoot
import pl.masslany.podkop.features.settings.SettingsScreen
import pl.masslany.podkop.features.settings.SettingsScreenRoot
import pl.masslany.podkop.features.tag.TagScreen
import pl.masslany.podkop.features.tag.TagScreenRoot

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val viewModel = koinViewModel<AppViewModel>()
    val appNavigator = koinInject<AppNavigator>()
    val snackbarManager = koinInject<SnackbarManager>()
    val appSettings = koinInject<AppSettings>()
    val themeOverride by appSettings.themeOverride.collectAsStateWithLifecycle(initialValue = ThemeOverride.AUTO)
    val dynamicColorsEnabled by appSettings.dynamicColorsEnabled.collectAsStateWithLifecycle(initialValue = true)
    val darkTheme = when (themeOverride) {
        ThemeOverride.AUTO -> isSystemInDarkTheme()
        ThemeOverride.LIGHT -> false
        ThemeOverride.DARK -> true
    }

    SystemAppearance(isDark = darkTheme)

    PodkopTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColorsEnabled,
    ) {
        val startupState by viewModel.startupState.collectAsStateWithLifecycle()
        val state by viewModel.navigationState.collectAsStateWithLifecycle()
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

        LifecycleStartEffect(viewModel) {
            viewModel.onAppForegrounded()
            onStopOrDispose {
                viewModel.onAppBackgrounded()
            }
        }

        CompositionLocalProvider(
            LocalAppSnackbarHostState provides snackbarHostState,
            LocalAppSettings provides appSettings,
        ) {
            NavigationContent(
                state = state,
                onBack = viewModel::onBack,
                entryProvider = entryProvider {
                    entry<HomeScreen> {
                        HomeScreenRoot()
                    }

                    entry<EntryDetailsScreen> {
                        EntryDetailsScreenRoot(
                            screen = it,
                            paddingValues = WindowInsets.systemBars.asPaddingValues(),
                        )
                    }

                    entry<LinkDetailsScreen> {
                        LinkDetailsScreenRoot(
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

                    entry<ProfileScreen> {
                        ProfileScreenRoot(
                            username = it.username,
                            paddingValues = WindowInsets.systemBars.asPaddingValues(),
                        )
                    }

                    entry<SettingsScreen> {
                        SettingsScreenRoot(
                            paddingValues = WindowInsets.systemBars.asPaddingValues(),
                        )
                    }

                    entry<AboutAppScreen>(
                        metadata = DialogSceneStrategy.dialog(
                            DialogProperties(
                                usePlatformDefaultWidth = false,
                            ),
                        ),
                    ) {
                        AboutAppScreenRoot()
                    }

                    entry<HitsScreen> {
                        HitsScreenRoot(
                            paddingValues = WindowInsets.systemBars.asPaddingValues(),
                        )
                    }

                    entry<FavoritesScreen> {
                        FavoritesScreenRoot(
                            paddingValues = WindowInsets.systemBars.asPaddingValues(),
                        )
                    }

                    entry<SearchScreen> {
                        SearchScreenRoot(
                            paddingValues = WindowInsets.systemBars.asPaddingValues(),
                        )
                    }

                    entry<DebugScreen> {
                        DebugScreenRoot(
                            paddingValues = WindowInsets.systemBars.asPaddingValues(),
                        )
                    }

                    entry<TagScreen> {
                        TagScreenRoot(
                            tag = it.tag,
                            paddingValues = WindowInsets.systemBars.asPaddingValues(),
                        )
                    }

                    entry<ResourceActionsBottomSheetScreen>(
                        metadata = BottomSheetSceneStrategy.bottomSheet(),
                    ) {
                        ResourceActionsBottomSheetScreenRoot(
                            screen = it,
                        )
                    }

                    entry<ComposerBottomSheetScreen>(
                        metadata = BottomSheetSceneStrategy.bottomSheet(
                            modalBottomSheetProperties = ModalBottomSheetProperties(
                                shouldDismissOnClickOutside = false,
                            ),
                            sheetGesturesEnabled = false,
                            hideDragHandle = true,
                        ),
                    ) {
                        ComposerBottomSheetScreenRoot(
                            screen = it,
                        )
                    }

                    entry<ResourceVotesBottomSheetScreen>(
                        metadata = BottomSheetSceneStrategy.bottomSheet(),
                    ) {
                        ResourceVotesBottomSheetScreenRoot(
                            screen = it,
                        )
                    }

                    entry<ResourceScreenshotPreviewDialogScreen>(
                        metadata = DialogSceneStrategy.dialog(
                            DialogProperties(
                                usePlatformDefaultWidth = false,
                            ),
                        ),
                    ) {
                        ResourceScreenshotPreviewDialogScreenRoot(
                            screen = it,
                        )
                    }

                    entry<ComposerMediaAttachBottomSheetScreen>(
                        metadata = BottomSheetSceneStrategy.bottomSheet(),
                    ) {
                        ComposerMediaAttachBottomSheetScreenRoot(
                            screen = it,
                            appNavigator = appNavigator,
                        )
                    }

                    entry<ComposerMediaUrlDialogScreen>(
                        metadata = DialogSceneStrategy.dialog(
                            DialogProperties(
                                usePlatformDefaultWidth = false,
                            ),
                        ),
                    ) {
                        ComposerMediaUrlDialogScreenRoot(
                            screen = it,
                            appNavigator = appNavigator,
                        )
                    }

                    entry<ComposerMediaPickLocalScreen>(
                        metadata = DialogSceneStrategy.dialog(
                            DialogProperties(
                                usePlatformDefaultWidth = false,
                            ),
                        ),
                    ) {
                        ComposerMediaPickLocalScreenRoot(
                            screen = it,
                            appNavigator = appNavigator,
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

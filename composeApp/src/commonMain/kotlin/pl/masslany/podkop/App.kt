package pl.masslany.podkop

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.DialogSceneStrategy
import org.jetbrains.compose.resources.getString
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import pl.masslany.podkop.business.startup.models.AppState
import pl.masslany.podkop.common.components.GenericErrorScreen
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
import pl.masslany.podkop.common.navigation.SetDialogDestinationToEdgeToEdge
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
import pl.masslany.podkop.features.linksubmission.AddLinkScreen
import pl.masslany.podkop.features.linksubmission.LinkDraftScreen
import pl.masslany.podkop.features.linksubmission.addlink.AddLinkScreenRoot
import pl.masslany.podkop.features.linksubmission.linkdraft.LinkDraftScreenRoot
import pl.masslany.podkop.features.notifications.NotificationsScreen
import pl.masslany.podkop.features.notifications.NotificationsScreenRoot
import pl.masslany.podkop.features.privatemessages.ConversationScreen
import pl.masslany.podkop.features.privatemessages.NewConversationScreen
import pl.masslany.podkop.features.privatemessages.PrivateMessagesScreen
import pl.masslany.podkop.features.privatemessages.conversation.ConversationScreenRoot
import pl.masslany.podkop.features.privatemessages.inbox.PrivateMessagesScreenRoot
import pl.masslany.podkop.features.privatemessages.newconversation.NewConversationScreenRoot
import pl.masslany.podkop.features.profile.ProfileScreen
import pl.masslany.podkop.features.profile.ProfileScreenRoot
import pl.masslany.podkop.features.resourceactions.ResourceActionsBottomSheetScreen
import pl.masslany.podkop.features.resourceactions.ResourceActionsBottomSheetScreenRoot
import pl.masslany.podkop.features.resourceactions.ResourceScreenshotPreviewDialogScreen
import pl.masslany.podkop.features.resourceactions.ResourceScreenshotPreviewDialogScreenRoot
import pl.masslany.podkop.features.resourceactions.ResourceTextSelectionDialogScreen
import pl.masslany.podkop.features.resourceactions.ResourceTextSelectionDialogScreenRoot
import pl.masslany.podkop.features.resourceactions.ResourceVotesBottomSheetScreen
import pl.masslany.podkop.features.resourceactions.ResourceVotesBottomSheetScreenRoot
import pl.masslany.podkop.features.search.SearchScreen
import pl.masslany.podkop.features.search.SearchScreenRoot
import pl.masslany.podkop.features.settings.SettingsScreen
import pl.masslany.podkop.features.settings.SettingsScreenRoot
import pl.masslany.podkop.features.tag.TagScreen
import pl.masslany.podkop.features.tag.TagScreenRoot

private val GlobalSnackbarTopOffset = 80.dp

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

        LaunchedEffect(appNavigator) {
            appNavigator.initialize()
        }

        if (startupState !is AppState.Ready) {
            StartupStateScreen(
                startupState = startupState,
                onRetryClicked = viewModel::onStartupRetryClicked,
            )
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
            val safeDrawingPaddingValues = WindowInsets.safeDrawing.asPaddingValues()

            Box(modifier = Modifier.fillMaxSize()) {
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
                                paddingValues = safeDrawingPaddingValues,
                            )
                        }

                        entry<LinkDetailsScreen> {
                            LinkDetailsScreenRoot(
                                id = it.id,
                                paddingValues = safeDrawingPaddingValues,
                            )
                        }

                        entry<ImageViewerScreen> {
                            ImageViewerScreenRoot(
                                imageUrl = it.imageUrl,
                                paddingValues = safeDrawingPaddingValues,
                            )
                        }

                        entry<ProfileScreen> {
                            ProfileScreenRoot(
                                username = it.username,
                                paddingValues = safeDrawingPaddingValues,
                            )
                        }

                        entry<AddLinkScreen> {
                            AddLinkScreenRoot()
                        }

                        entry<LinkDraftScreen> {
                            LinkDraftScreenRoot(screen = it)
                        }

                        entry<SettingsScreen> {
                            SettingsScreenRoot(
                                paddingValues = safeDrawingPaddingValues,
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
                                paddingValues = safeDrawingPaddingValues,
                            )
                        }

                        entry<FavoritesScreen> {
                            FavoritesScreenRoot(
                                paddingValues = safeDrawingPaddingValues,
                            )
                        }

                        entry<SearchScreen> {
                            SearchScreenRoot(
                                paddingValues = safeDrawingPaddingValues,
                            )
                        }

                        entry<NotificationsScreen> {
                            NotificationsScreenRoot(
                                paddingValues = safeDrawingPaddingValues,
                            )
                        }

                        entry<PrivateMessagesScreen> {
                            PrivateMessagesScreenRoot(
                                paddingValues = safeDrawingPaddingValues,
                            )
                        }

                        entry<NewConversationScreen> {
                            NewConversationScreenRoot(
                                paddingValues = safeDrawingPaddingValues,
                            )
                        }

                        entry<ConversationScreen> {
                            ConversationScreenRoot(
                                screen = it,
                                paddingValues = safeDrawingPaddingValues,
                            )
                        }

                        entry<DebugScreen> {
                            DebugScreenRoot(
                                paddingValues = safeDrawingPaddingValues,
                            )
                        }

                        entry<TagScreen> {
                            TagScreenRoot(
                                tag = it.tag,
                                paddingValues = safeDrawingPaddingValues,
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
                                skipPartiallyExpanded = true,
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
                            SetDialogDestinationToEdgeToEdge()
                            ResourceScreenshotPreviewDialogScreenRoot(
                                screen = it,
                            )
                        }

                        entry<ResourceTextSelectionDialogScreen>(
                            metadata = DialogSceneStrategy.dialog(
                                DialogProperties(
                                    usePlatformDefaultWidth = false,
                                ),
                            ),
                        ) {
                            SetDialogDestinationToEdgeToEdge()
                            ResourceTextSelectionDialogScreenRoot(
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

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 16.dp)
                        .padding(
                            top = safeDrawingPaddingValues.calculateTopPadding() + GlobalSnackbarTopOffset,
                        ),
                )
            }
        }
    }
}

@Composable
private fun StartupStateScreen(
    startupState: AppState,
    onRetryClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        when (startupState) {
            AppState.Initializing -> {
                Column(
                    modifier = Modifier
                        .widthIn(max = 320.dp)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            }

            AppState.Error -> {
                GenericErrorScreen(
                    onRefreshClicked = onRetryClicked,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            AppState.Ready -> Unit
        }
    }
}

private suspend fun SnackbarMessage.resolveMessage(): String = when (this) {
    is SnackbarMessage.Raw -> value
    is SnackbarMessage.Resource -> getString(resource = resource, *args.toTypedArray())
}

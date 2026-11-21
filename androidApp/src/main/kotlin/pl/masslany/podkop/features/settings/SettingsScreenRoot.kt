package pl.masslany.podkop.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pl.masslany.podkop.R
import pl.masslany.podkop.common.components.ScreenLoadingIndicator
import pl.masslany.podkop.common.components.SegmentedSection
import pl.masslany.podkop.common.components.segmentedRowColors
import pl.masslany.podkop.common.extensions.toWindowInsets
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.common.settings.ThemeOverride
import pl.masslany.podkop.features.privatemessages.inbox.PrivateMessagesNotificationPermissionEffect
import pl.masslany.podkop.features.settings.preview.NoOpSettingsActions
import pl.masslany.podkop.features.settings.preview.SettingsScreenStateProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreenRoot(
    paddingValues: PaddingValues,
) {
    val viewModel = koinViewModel<SettingsViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    PrivateMessagesNotificationPermissionEffect(
        shouldRequestPermission = state.shouldRequestNotificationPermission,
        onPermissionResult = viewModel::onNotificationPermissionResult,
    )

    SettingsScreenContent(
        paddingValues = paddingValues,
        state = state,
        actions = viewModel,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    paddingValues: PaddingValues,
    state: SettingsScreenState,
    actions: SettingsActions,
    modifier: Modifier = Modifier,
) {
    val topBarInsets = paddingValues.toWindowInsets(includeBottom = false)
    val contentInsets = paddingValues.toWindowInsets(includeTop = false)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.topbar_label_settings))
                },
                navigationIcon = {
                    IconButton(onClick = actions::onTopBarBackClicked, shapes = IconButtonDefaults.shapes()) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_back),
                            contentDescription = stringResource(
                                id = R.string.accessibility_topbar_back,
                            ),
                        )
                    }
                },
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
                windowInsets = topBarInsets,
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = contentInsets,
    ) { innerPaddingValues ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPaddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ScreenLoadingIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPaddingValues)
                    .padding(
                        horizontal = 16.dp,
                        vertical = 12.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.showPrivateMessagesBackgroundNotifications) {
                    SegmentedSection(
                        title = stringResource(id = R.string.settings_headline_notifications),
                    ) {
                        SwitchSettingRow(
                            index = 0,
                            count = 1,
                            label = stringResource(id = R.string.settings_body_pm_notifications_toggle),
                            supportingText = stringResource(
                                id = if (state.areSystemNotificationsEnabled) {
                                    R.string.settings_body_pm_notifications_enabled
                                } else {
                                    R.string.settings_body_pm_notifications_disabled
                                },
                            ),
                            checked = state.privateMessagesBackgroundNotificationsEnabled,
                            onCheckedChange = actions::onPrivateMessagesBackgroundNotificationsChanged,
                        )
                    }
                }

                SegmentedSection(
                    title = stringResource(id = R.string.settings_headline_theme),
                ) {
                    ThemeOverrideSelector(
                        index = 0,
                        count = 2,
                        selectedTheme = state.themeOverride,
                        onThemeSelected = actions::onThemeOverrideChanged,
                    )

                    SwitchSettingRow(
                        index = 1,
                        count = 2,
                        label = stringResource(id = R.string.settings_body_dynamic_colors),
                        supportingText = null,
                        checked = state.dynamicColorsEnabled,
                        onCheckedChange = actions::onDynamicColorsChanged,
                    )
                }

                SegmentedSection(
                    title = stringResource(id = R.string.settings_headline_media),
                ) {
                    SwitchSettingRow(
                        index = 0,
                        count = 2,
                        label = stringResource(id = R.string.settings_body_gif_autoplay),
                        supportingText = null,
                        checked = state.autoplayGifs,
                        onCheckedChange = actions::onAutoplayGifsChanged,
                    )
                    SwitchSettingRow(
                        index = 1,
                        count = 2,
                        label = stringResource(id = R.string.settings_body_play_videos_inline),
                        supportingText = stringResource(
                            id = R.string.settings_body_play_videos_inline_supporting,
                        ),
                        checked = state.playVideosInline,
                        onCheckedChange = actions::onPlayVideosInlineChanged,
                    )
                }

                SegmentedSection(
                    title = stringResource(id = R.string.settings_headline_comments),
                ) {
                    SwitchSettingRow(
                        index = 0,
                        count = 1,
                        label = stringResource(id = R.string.settings_body_threaded_entry_comments),
                        supportingText = stringResource(
                            id = R.string.settings_body_threaded_entry_comments_supporting,
                        ),
                        checked = state.threadedEntryComments,
                        onCheckedChange = actions::onThreadedEntryCommentsChanged,
                    )
                }

                SegmentedSection(
                    title = stringResource(id = R.string.settings_headline_privacy),
                ) {
                    SwitchSettingRow(
                        index = 0,
                        count = 2,
                        label = stringResource(id = R.string.settings_body_analytics),
                        supportingText = null,
                        checked = state.analyticsEnabled,
                        onCheckedChange = actions::onAnalyticsCollectionChanged,
                    )
                    SwitchSettingRow(
                        index = 1,
                        count = 2,
                        label = stringResource(id = R.string.settings_body_crash_reporting),
                        supportingText = null,
                        checked = state.crashReportingEnabled,
                        onCheckedChange = actions::onCrashReportingChanged,
                    )
                }

                if (state.showDebugTools) {
                    SegmentedSection(
                        title = stringResource(id = R.string.settings_headline_debug),
                    ) {
                        ActionSettingRow(
                            index = 0,
                            count = 2,
                            label = stringResource(id = R.string.settings_body_open_debug),
                            buttonLabel = stringResource(id = R.string.settings_button_open_debug),
                            onClick = actions::onDebugToolsClicked,
                        )
                        ActionSettingRow(
                            index = 1,
                            count = 2,
                            label = stringResource(id = R.string.settings_action_copy_diagnostics),
                            buttonLabel = stringResource(id = R.string.settings_button_copy),
                            onClick = actions::onCopyDiagnosticsClicked,
                        )
                    }
                }
                SegmentedSection(
                    title = stringResource(id = R.string.settings_headline_support),
                ) {
                    ActionSettingRow(
                        index = 0,
                        count = 1,
                        label = stringResource(id = R.string.settings_action_clear_cache),
                        buttonLabel = stringResource(id = R.string.settings_button_clear),
                        onClick = actions::onClearCacheClicked,
                    )
                }

                if (state.showAccountSection) {
                    SegmentedSection(
                        title = stringResource(id = R.string.settings_headline_account),
                    ) {
                        ActionSettingRow(
                            index = 0,
                            count = 1,
                            label = stringResource(id = R.string.settings_body_manage_blacklists),
                            buttonLabel = stringResource(id = R.string.settings_button_open),
                            onClick = actions::onManageBlacklistsClicked,
                        )
                        Button(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth()
                                .widthIn(max = 600.dp)
                                .align(Alignment.CenterHorizontally),
                            onClick = actions::onLogoutClicked,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            ),
                        ) {
                            Text(text = stringResource(id = R.string.profile_log_out_button))
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(
                        id = R.string.settings_body_version,
                        state.appVersion,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ThemeOverrideSelector(
    selectedTheme: ThemeOverride,
    onThemeSelected: (ThemeOverride) -> Unit,
    index: Int,
    count: Int,
) {
    val options = listOf(
        ThemeOverride.AUTO to stringResource(id = R.string.settings_option_theme_auto),
        ThemeOverride.LIGHT to stringResource(id = R.string.settings_option_theme_light),
        ThemeOverride.DARK to stringResource(id = R.string.settings_option_theme_dark),
    )

    SegmentedListItem(
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        colors = segmentedRowColors(),
        contentPadding = PaddingValues(16.dp),
    ) {
        ThemeOverrideButtons(
            options = options,
            selectedTheme = selectedTheme,
            onThemeSelected = onThemeSelected,
        )
    }
}

@Composable
private fun ThemeOverrideButtons(
    options: List<Pair<ThemeOverride, String>>,
    selectedTheme: ThemeOverride,
    onThemeSelected: (ThemeOverride) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        options.forEachIndexed { index, (themeOverride, label) ->
            ToggleButton(
                checked = selectedTheme == themeOverride,
                onCheckedChange = { onThemeSelected(themeOverride) },
                modifier = Modifier
                    .weight(1f)
                    .semantics { role = Role.RadioButton },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                colors = ToggleButtonDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            ) {
                Text(text = label)
            }
        }
    }
}

@Composable
private fun SwitchSettingRow(
    label: String,
    supportingText: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    index: Int,
    count: Int,
) {
    // A checked segment would otherwise morph into its selected pill shape and leave the group;
    // the switch already shows the state.
    val shapes = ListItemDefaults.segmentedShapes(index = index, count = count)
    SegmentedListItem(
        checked = checked,
        onCheckedChange = onCheckedChange,
        shapes = shapes.copy(selectedShape = shapes.shape),
        colors = segmentedRowColors(),
        supportingContent = supportingText?.let { text -> { Text(text = text) } },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = null,
            )
        },
    ) {
        Text(text = label)
    }
}

@Composable
private fun ActionSettingRow(
    label: String,
    buttonLabel: String,
    onClick: () -> Unit,
    index: Int,
    count: Int,
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        colors = segmentedRowColors(),
        trailingContent = {
            Text(
                text = buttonLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        },
    ) {
        Text(text = label)
    }
}

@Preview
@Composable
private fun SettingsScreenContentPreview(
    @PreviewParameter(SettingsScreenStateProvider::class) state: SettingsScreenState,
) {
    PodkopPreview(darkTheme = false) {
        SettingsScreenContent(
            paddingValues = PaddingValues(),
            state = state,
            actions = NoOpSettingsActions,
        )
    }
}

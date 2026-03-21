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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import pl.masslany.podkop.common.components.SectionCard
import pl.masslany.podkop.common.components.SectionCardDivider
import pl.masslany.podkop.common.extensions.toWindowInsets
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.common.settings.ThemeOverride
import pl.masslany.podkop.features.privatemessages.inbox.PrivateMessagesNotificationPermissionEffect
import pl.masslany.podkop.features.settings.preview.NoOpSettingsActions
import pl.masslany.podkop.features.settings.preview.SettingsScreenStateProvider
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_topbar_back
import podkop.composeapp.generated.resources.ic_arrow_back
import podkop.composeapp.generated.resources.profile_log_out_button
import podkop.composeapp.generated.resources.settings_action_clear_cache
import podkop.composeapp.generated.resources.settings_action_copy_diagnostics
import podkop.composeapp.generated.resources.settings_body_analytics
import podkop.composeapp.generated.resources.settings_body_crash_reporting
import podkop.composeapp.generated.resources.settings_body_dynamic_colors
import podkop.composeapp.generated.resources.settings_body_gif_autoplay
import podkop.composeapp.generated.resources.settings_body_open_debug
import podkop.composeapp.generated.resources.settings_body_pm_notifications_disabled
import podkop.composeapp.generated.resources.settings_body_pm_notifications_enabled
import podkop.composeapp.generated.resources.settings_body_pm_notifications_toggle
import podkop.composeapp.generated.resources.settings_body_version
import podkop.composeapp.generated.resources.settings_button_clear
import podkop.composeapp.generated.resources.settings_button_copy
import podkop.composeapp.generated.resources.settings_button_open_debug
import podkop.composeapp.generated.resources.settings_headline_account
import podkop.composeapp.generated.resources.settings_headline_debug
import podkop.composeapp.generated.resources.settings_headline_media
import podkop.composeapp.generated.resources.settings_headline_notifications
import podkop.composeapp.generated.resources.settings_headline_privacy
import podkop.composeapp.generated.resources.settings_headline_support
import podkop.composeapp.generated.resources.settings_headline_theme
import podkop.composeapp.generated.resources.settings_option_theme_auto
import podkop.composeapp.generated.resources.settings_option_theme_dark
import podkop.composeapp.generated.resources.settings_option_theme_light
import podkop.composeapp.generated.resources.topbar_label_settings

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
                    Text(text = stringResource(resource = Res.string.topbar_label_settings))
                },
                navigationIcon = {
                    IconButton(onClick = actions::onTopBarBackClicked) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = vectorResource(resource = Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(
                                resource = Res.string.accessibility_topbar_back,
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
                CircularProgressIndicator()
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
                if (state.supportsPrivateMessagesBackgroundNotifications) {
                    SectionCard(
                        title = stringResource(resource = Res.string.settings_headline_notifications),
                    ) {
                        SwitchSettingRow(
                            label = stringResource(resource = Res.string.settings_body_pm_notifications_toggle),
                            supportingText = stringResource(
                                resource = if (state.areSystemNotificationsEnabled) {
                                    Res.string.settings_body_pm_notifications_enabled
                                } else {
                                    Res.string.settings_body_pm_notifications_disabled
                                },
                            ),
                            checked = state.privateMessagesBackgroundNotificationsEnabled,
                            onCheckedChange = actions::onPrivateMessagesBackgroundNotificationsChanged,
                        )
                    }
                }

                SectionCard(
                    title = stringResource(resource = Res.string.settings_headline_theme),
                ) {
                    ThemeOverrideSelector(
                        selectedTheme = state.themeOverride,
                        onThemeSelected = actions::onThemeOverrideChanged,
                    )

                    if (state.supportsDynamicColorsToggle) {
                        SectionCardDivider()
                        SwitchSettingRow(
                            label = stringResource(resource = Res.string.settings_body_dynamic_colors),
                            supportingText = null,
                            checked = state.dynamicColorsEnabled,
                            onCheckedChange = actions::onDynamicColorsChanged,
                        )
                    }
                }

                SectionCard(
                    title = stringResource(resource = Res.string.settings_headline_media),
                ) {
                    SwitchSettingRow(
                        label = stringResource(resource = Res.string.settings_body_gif_autoplay),
                        supportingText = null,
                        checked = state.autoplayGifs,
                        onCheckedChange = actions::onAutoplayGifsChanged,
                    )
                }

                if (state.supportsTelemetryControls) {
                    SectionCard(
                        title = stringResource(resource = Res.string.settings_headline_privacy),
                    ) {
                        SwitchSettingRow(
                            label = stringResource(resource = Res.string.settings_body_analytics),
                            supportingText = null,
                            checked = state.analyticsEnabled,
                            onCheckedChange = actions::onAnalyticsCollectionChanged,
                        )
                        SectionCardDivider()
                        SwitchSettingRow(
                            label = stringResource(resource = Res.string.settings_body_crash_reporting),
                            supportingText = null,
                            checked = state.crashReportingEnabled,
                            onCheckedChange = actions::onCrashReportingChanged,
                        )
                    }
                }

                if (state.showDebugTools) {
                    SectionCard(
                        title = stringResource(resource = Res.string.settings_headline_debug),
                    ) {
                        ActionSettingRow(
                            label = stringResource(resource = Res.string.settings_body_open_debug),
                            buttonLabel = stringResource(resource = Res.string.settings_button_open_debug),
                            onClick = actions::onDebugToolsClicked,
                        )
                        SectionCardDivider()
                        ActionSettingRow(
                            label = stringResource(resource = Res.string.settings_action_copy_diagnostics),
                            buttonLabel = stringResource(resource = Res.string.settings_button_copy),
                            onClick = actions::onCopyDiagnosticsClicked,
                        )
                    }
                }
                if (state.supportsCacheClearing) {
                    SectionCard(
                        title = stringResource(resource = Res.string.settings_headline_support),
                    ) {
                        ActionSettingRow(
                            label = stringResource(resource = Res.string.settings_action_clear_cache),
                            buttonLabel = stringResource(resource = Res.string.settings_button_clear),
                            onClick = actions::onClearCacheClicked,
                        )
                    }
                }

                if (state.showLogoutButton) {
                    SectionCard(
                        title = stringResource(resource = Res.string.settings_headline_account),
                    ) {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 600.dp)
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally),
                            onClick = actions::onLogoutClicked,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            ),
                        ) {
                            Text(text = stringResource(resource = Res.string.profile_log_out_button))
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(
                        resource = Res.string.settings_body_version,
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
) {
    val options = listOf(
        ThemeOverride.AUTO to stringResource(resource = Res.string.settings_option_theme_auto),
        ThemeOverride.LIGHT to stringResource(resource = Res.string.settings_option_theme_light),
        ThemeOverride.DARK to stringResource(resource = Res.string.settings_option_theme_dark),
    )

    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        options.forEachIndexed { index, (themeOverride, label) ->
            SegmentedButton(
                modifier = Modifier.weight(1f),
                selected = selectedTheme == themeOverride,
                onClick = { onThemeSelected(themeOverride) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size,
                ),
                icon = {},
                label = {
                    Text(text = label)
                },
            )
        }
    }
}

@Composable
private fun SwitchSettingRow(
    label: String,
    supportingText: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
            )
            supportingText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun ActionSettingRow(
    label: String,
    buttonLabel: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )
        TextButton(onClick = onClick) {
            Text(text = buttonLabel)
        }
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

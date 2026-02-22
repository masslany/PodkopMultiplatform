package pl.masslany.podkop.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import pl.masslany.podkop.common.settings.ThemeOverride
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_topbar_back
import podkop.composeapp.generated.resources.ic_arrow_back
import podkop.composeapp.generated.resources.settings_body_dynamic_colors
import podkop.composeapp.generated.resources.settings_body_gif_autoplay
import podkop.composeapp.generated.resources.settings_body_open_debug
import podkop.composeapp.generated.resources.settings_button_open_debug
import podkop.composeapp.generated.resources.settings_headline_debug
import podkop.composeapp.generated.resources.settings_headline_media
import podkop.composeapp.generated.resources.settings_headline_theme
import podkop.composeapp.generated.resources.settings_option_theme_auto
import podkop.composeapp.generated.resources.settings_option_theme_dark
import podkop.composeapp.generated.resources.settings_option_theme_light
import podkop.composeapp.generated.resources.topbar_label_settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenRoot(
    paddingValues: PaddingValues,
) {
    val viewModel = koinViewModel<SettingsViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
            ),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(resource = Res.string.topbar_label_settings))
                },
                navigationIcon = {
                    IconButton(onClick = viewModel::onTopBarBackClicked) {
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
                windowInsets = WindowInsets(top = paddingValues.calculateTopPadding()),
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPaddingValues)
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(resource = Res.string.settings_headline_theme),
                style = MaterialTheme.typography.titleMedium,
            )

            ThemeOverrideOptionRow(
                label = stringResource(resource = Res.string.settings_option_theme_auto),
                selected = state.themeOverride == ThemeOverride.AUTO,
                onClick = { viewModel.onThemeOverrideChanged(ThemeOverride.AUTO) },
            )
            ThemeOverrideOptionRow(
                label = stringResource(resource = Res.string.settings_option_theme_light),
                selected = state.themeOverride == ThemeOverride.LIGHT,
                onClick = { viewModel.onThemeOverrideChanged(ThemeOverride.LIGHT) },
            )
            ThemeOverrideOptionRow(
                label = stringResource(resource = Res.string.settings_option_theme_dark),
                selected = state.themeOverride == ThemeOverride.DARK,
                onClick = { viewModel.onThemeOverrideChanged(ThemeOverride.DARK) },
            )

            if (state.supportsDynamicColorsToggle) {
                SwitchSettingRow(
                    label = stringResource(resource = Res.string.settings_body_dynamic_colors),
                    checked = state.dynamicColorsEnabled,
                    onCheckedChange = viewModel::onDynamicColorsChanged,
                )
            }

            Text(
                text = stringResource(resource = Res.string.settings_headline_media),
                style = MaterialTheme.typography.titleMedium,
            )

            val autoplayGifs = state.autoplayGifs
            if (autoplayGifs == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(resource = Res.string.settings_body_gif_autoplay),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                    )
                }
            } else {
                SwitchSettingRow(
                    label = stringResource(resource = Res.string.settings_body_gif_autoplay),
                    checked = autoplayGifs,
                    onCheckedChange = viewModel::onAutoplayGifsChanged,
                )
            }

            if (state.showDebugTools) {
                Text(
                    text = stringResource(resource = Res.string.settings_headline_debug),
                    style = MaterialTheme.typography.titleMedium,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(resource = Res.string.settings_body_open_debug),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    TextButton(
                        onClick = viewModel::onDebugToolsClicked,
                    ) {
                        Text(text = stringResource(resource = Res.string.settings_button_open_debug))
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeOverrideOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
    }
}

@Composable
private fun SwitchSettingRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

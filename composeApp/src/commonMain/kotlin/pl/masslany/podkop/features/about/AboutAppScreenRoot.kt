package pl.masslany.podkop.features.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.about_app_button_open_license
import podkop.composeapp.generated.resources.about_app_button_open_project
import podkop.composeapp.generated.resources.about_app_libraries_title
import podkop.composeapp.generated.resources.about_app_license_unknown
import podkop.composeapp.generated.resources.about_app_no_libraries
import podkop.composeapp.generated.resources.accessibility_dialog_close
import podkop.composeapp.generated.resources.app_name
import podkop.composeapp.generated.resources.ic_close
import podkop.composeapp.generated.resources.settings_body_version

@Composable
fun AboutAppScreenRoot(
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<AboutAppViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    AboutAppScreenContent(
        appName = stringResource(resource = Res.string.app_name),
        state = state,
        actions = viewModel,
        modifier = modifier,
    )
}

@Composable
private fun AboutAppScreenContent(
    appName: String,
    state: AboutAppScreenState,
    actions: AboutAppActions,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 720.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = appName,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Text(
                            text = stringResource(
                                resource = Res.string.settings_body_version,
                                state.appVersion,
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    IconButton(onClick = actions::onCloseClicked) {
                        Icon(
                            imageVector = vectorResource(resource = Res.drawable.ic_close),
                            contentDescription = stringResource(
                                resource = Res.string.accessibility_dialog_close,
                            ),
                        )
                    }
                }

                Text(
                    text = stringResource(resource = Res.string.about_app_libraries_title),
                    style = MaterialTheme.typography.titleMedium,
                )

                if (state.libraries.isEmpty()) {
                    Text(
                        text = stringResource(resource = Res.string.about_app_no_libraries),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        itemsIndexed(state.libraries) { index, library ->
                            OpenSourceLibraryNoticeItem(
                                notice = library,
                                onOpenLink = actions::onOpenLinkClicked,
                            )
                            if (index != state.libraries.lastIndex) {
                                HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OpenSourceLibraryNoticeItem(
    notice: OpenSourceLibraryNotice,
    onOpenLink: (String) -> Unit,
) {
    val actionUrl = notice.licenseUrl ?: notice.projectUrl
    val actionLabel = when {
        notice.licenseUrl != null -> stringResource(resource = Res.string.about_app_button_open_license)
        notice.projectUrl != null -> stringResource(resource = Res.string.about_app_button_open_project)
        else -> null
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = notice.name,
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            text = notice.artifact,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = notice.licenseName ?: stringResource(resource = Res.string.about_app_license_unknown),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (actionUrl != null && actionLabel != null) {
            TextButton(
                modifier = Modifier.align(Alignment.End),
                onClick = { onOpenLink(actionUrl) },
            ) {
                Text(text = actionLabel)
            }
        }
    }
}

package pl.masslany.podkop.features.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import pl.masslany.podkop.common.extensions.toWindowInsets
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.debug.preview.DebugScreenStateProvider
import pl.masslany.podkop.features.debug.preview.NoOpDebugActions
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_topbar_back
import podkop.composeapp.generated.resources.debug_entry_button_open
import podkop.composeapp.generated.resources.debug_entry_hint
import podkop.composeapp.generated.resources.debug_entry_title
import podkop.composeapp.generated.resources.debug_entry_validation_error
import podkop.composeapp.generated.resources.debug_link_button_open
import podkop.composeapp.generated.resources.debug_link_hint
import podkop.composeapp.generated.resources.debug_link_title
import podkop.composeapp.generated.resources.debug_link_validation_error
import podkop.composeapp.generated.resources.debug_pm_notification_button_send
import podkop.composeapp.generated.resources.debug_pm_notification_title
import podkop.composeapp.generated.resources.debug_snackbar_button_send
import podkop.composeapp.generated.resources.ic_arrow_back
import podkop.composeapp.generated.resources.topbar_label_debug

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DebugScreenRoot(
    paddingValues: PaddingValues,
) {
    val viewModel = koinViewModel<DebugViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    DebugScreenContent(
        paddingValues = paddingValues,
        state = state,
        actions = viewModel,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreenContent(
    paddingValues: PaddingValues,
    state: DebugScreenState,
    actions: DebugActions,
    modifier: Modifier = Modifier,
) {
    val topBarInsets = paddingValues.toWindowInsets(includeBottom = false)
    val contentInsets = paddingValues.toWindowInsets(includeTop = false)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(resource = Res.string.topbar_label_debug))
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
            Text(
                text = stringResource(resource = Res.string.debug_entry_title),
                style = MaterialTheme.typography.titleMedium,
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.entryIdInput,
                onValueChange = actions::onEntryIdChanged,
                singleLine = true,
                label = {
                    Text(text = stringResource(resource = Res.string.debug_entry_hint))
                },
                isError = state.isEntryIdInvalid,
                supportingText = {
                    if (state.isEntryIdInvalid) {
                        Text(text = stringResource(resource = Res.string.debug_entry_validation_error))
                    }
                },
            )

            Button(
                onClick = actions::onOpenEntryClicked,
                enabled = state.entryIdInput.isNotBlank(),
            ) {
                Text(text = stringResource(resource = Res.string.debug_entry_button_open))
            }

            Text(
                text = stringResource(resource = Res.string.debug_link_title),
                style = MaterialTheme.typography.titleMedium,
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.linkIdInput,
                onValueChange = actions::onLinkIdChanged,
                singleLine = true,
                label = {
                    Text(text = stringResource(resource = Res.string.debug_link_hint))
                },
                isError = state.isLinkIdInvalid,
                supportingText = {
                    if (state.isLinkIdInvalid) {
                        Text(text = stringResource(resource = Res.string.debug_link_validation_error))
                    }
                },
            )

            Button(
                onClick = actions::onOpenLinkClicked,
                enabled = state.linkIdInput.isNotBlank(),
            ) {
                Text(text = stringResource(resource = Res.string.debug_link_button_open))
            }

            Text(
                text = stringResource(resource = Res.string.debug_pm_notification_title),
                style = MaterialTheme.typography.titleMedium,
            )

            Button(
                onClick = actions::onSendPrivateMessagesNotificationClicked,
            ) {
                Text(text = stringResource(resource = Res.string.debug_pm_notification_button_send))
            }

            Button(
                onClick = actions::onSendSnackbarClicked,
            ) {
                Text(text = stringResource(resource = Res.string.debug_snackbar_button_send))
            }
        }
    }
}

@Preview
@Composable
private fun DebugScreenContentPreview(
    @PreviewParameter(DebugScreenStateProvider::class) state: DebugScreenState,
) {
    PodkopPreview(darkTheme = false) {
        DebugScreenContent(
            paddingValues = PaddingValues(),
            state = state,
            actions = NoOpDebugActions,
        )
    }
}

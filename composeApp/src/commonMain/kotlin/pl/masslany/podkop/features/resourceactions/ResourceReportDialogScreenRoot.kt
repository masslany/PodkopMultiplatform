package pl.masslany.podkop.features.resourceactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pl.masslany.podkop.common.navigation.SetDialogDestinationToEdgeToEdge
import pl.masslany.podkop.common.platform.rememberPlatformClipboard
import pl.masslany.podkop.common.preview.PodkopPreview
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.dialog_button_dismiss
import podkop.composeapp.generated.resources.resource_actions_copy_as_link
import podkop.composeapp.generated.resources.resource_actions_report
import podkop.composeapp.generated.resources.resource_report_dialog_contact
import podkop.composeapp.generated.resources.resource_report_dialog_fallback
import podkop.composeapp.generated.resources.resource_report_dialog_instructions
import podkop.composeapp.generated.resources.resource_report_dialog_open_content

@Composable
fun ResourceReportDialogScreenRoot(
    screen: ResourceReportDialogScreen,
    modifier: Modifier = Modifier,
) {
    SetDialogDestinationToEdgeToEdge()

    val viewModel = koinViewModel<ResourceReportDialogViewModel>(
        parameters = { parametersOf(screen.contentUrl) },
    )

    ResourceReportDialogContent(
        contentUrl = screen.contentUrl,
        actions = viewModel,
        modifier = modifier,
    )
}

/** Mirrors the iOS report screen: open the content on the website, or copy it for the contact form. */
@Composable
private fun ResourceReportDialogContent(
    contentUrl: String,
    actions: ResourceReportDialogActions,
    modifier: Modifier = Modifier,
) {
    val clipboard = rememberPlatformClipboard()
    val coroutineScope = rememberCoroutineScope()
    val dismissInteractionSource = remember { MutableInteractionSource() }
    val contentInteractionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.56f))
                .clickable(
                    interactionSource = dismissInteractionSource,
                    indication = null,
                    onClick = actions::onDismissClicked,
                ),
        )

        Surface(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .clickable(
                    interactionSource = contentInteractionSource,
                    indication = null,
                    onClick = {},
                ),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(resource = Res.string.resource_actions_report),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(resource = Res.string.resource_report_dialog_instructions),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = actions::onOpenContentClicked,
                ) {
                    Text(text = stringResource(resource = Res.string.resource_report_dialog_open_content))
                }
                HorizontalDivider()
                Text(
                    text = stringResource(resource = Res.string.resource_report_dialog_fallback),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        coroutineScope.launch {
                            clipboard.setText(contentUrl)
                            actions.onLinkCopied()
                        }
                    },
                ) {
                    Text(text = stringResource(resource = Res.string.resource_actions_copy_as_link))
                }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = actions::onContactClicked,
                ) {
                    Text(text = stringResource(resource = Res.string.resource_report_dialog_contact))
                }
                TextButton(
                    modifier = Modifier.align(Alignment.End),
                    onClick = actions::onDismissClicked,
                ) {
                    Text(text = stringResource(resource = Res.string.dialog_button_dismiss))
                }
            }
        }
    }
}

@Preview
@Composable
private fun ResourceReportDialogContentPreview() {
    PodkopPreview(darkTheme = false) {
        ResourceReportDialogContent(
            contentUrl = "https://wykop.pl/wpis/1",
            actions = object : ResourceReportDialogActions {
                override fun onOpenContentClicked() = Unit
                override fun onLinkCopied() = Unit
                override fun onContactClicked() = Unit
                override fun onDismissClicked() = Unit
            },
        )
    }
}

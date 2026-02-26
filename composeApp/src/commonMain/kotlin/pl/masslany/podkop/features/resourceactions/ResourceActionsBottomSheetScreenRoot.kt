package pl.masslany.podkop.features.resourceactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ResourceActionsBottomSheetScreenRoot(
    screen: ResourceActionsBottomSheetScreen,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<ResourceActionsBottomSheetViewModel>(
        parameters = {
            parametersOf(
                ResourceActionsParams(
                    resourceType = screen.resourceType,
                    rootId = screen.rootId,
                    rootSlug = screen.rootSlug,
                    parentId = screen.parentId,
                    childId = screen.childId,
                    screenshotDraftId = screen.screenshotDraftId,
                ),
            )
        },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    ResourceActionsBottomSheetContent(
        state = state,
        actions = viewModel,
        modifier = modifier,
    )
}

@Composable
internal fun ResourceActionsBottomSheetContent(
    state: ResourceActionsBottomSheetState,
    actions: ResourceActionsBottomSheetActions,
    modifier: Modifier = Modifier,
) {
    val clipboardManager = LocalClipboardManager.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp),
        ) {
            state.actions.forEach { item ->
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            when (val localAction = item.localAction) {
                                is ResourceActionLocalAction.CopyToClipboard -> {
                                    clipboardManager.setText(AnnotatedString(localAction.value))
                                }

                                null -> Unit
                            }
                            actions.onActionClicked(item.id)
                        },
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent,
                    ),
                    headlineContent = {
                        Text(text = stringResource(item.title))
                    },
                    leadingContent = {
                        Icon(
                            painter = painterResource(item.icon),
                            contentDescription = null,
                        )
                    },
                )
            }
        }
    }
}

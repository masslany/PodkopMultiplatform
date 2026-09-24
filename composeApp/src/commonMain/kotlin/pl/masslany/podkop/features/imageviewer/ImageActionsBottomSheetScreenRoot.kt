package pl.masslany.podkop.features.imageviewer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_topbar_downloads
import podkop.composeapp.generated.resources.ic_copy
import podkop.composeapp.generated.resources.ic_download
import podkop.composeapp.generated.resources.screenshot_preview_action_copy

@Composable
fun ImageActionsBottomSheetScreenRoot(screen: ImageActionsBottomSheetScreen) {
    val viewModel = koinViewModel<ImageViewerViewModel>(parameters = { parametersOf(screen.imageUrl, true) })
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        ListItem(
            modifier = Modifier.clickable(enabled = !state.isCopying) { viewModel.onCopyClicked(screen.imageUrl) },
            headlineContent = { Text(stringResource(Res.string.screenshot_preview_action_copy)) },
            leadingContent = {
                if (state.isCopying) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Icon(vectorResource(Res.drawable.ic_copy), contentDescription = null)
                }
            },
        )
        ListItem(
            modifier = Modifier.clickable(enabled = !state.isCopying) { viewModel.onDownloadClicked(screen.imageUrl) },
            headlineContent = { Text(stringResource(Res.string.accessibility_topbar_downloads)) },
            leadingContent = { Icon(vectorResource(Res.drawable.ic_download), contentDescription = null) },
        )
    }
}

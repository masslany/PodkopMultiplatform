package pl.masslany.podkop.features.linksubmission.linkdraft

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pl.masslany.podkop.R
import pl.masslany.podkop.features.linksubmission.LinkDraftScreen
import pl.masslany.podkop.features.linksubmission.addlink.components.AddLinkBottomActionBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LinkDraftScreenRoot(
    screen: LinkDraftScreen,
) {
    val viewModel = koinViewModel<LinkDraftViewModel>(
        parameters = { parametersOf(screen) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions: LinkDraftActions = viewModel

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.topbar_label_add_link)) },
                navigationIcon = {
                    IconButton(onClick = actions::onTopBarBackClicked) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_back),
                            contentDescription = stringResource(id = R.string.accessibility_topbar_back),
                        )
                    }
                },
                windowInsets = WindowInsets.safeDrawing,
            )
        },
        bottomBar = {
            AddLinkBottomActionBar(
                primaryLabel = R.string.add_link_action_add,
                isPrimaryEnabled = state.canSubmit,
                isLoading = state.isPublishing,
                onPrimaryClicked = actions::onSubmitClicked,
                onCancelClicked = actions::onCancelClicked,
                isCancelEnabled = !state.isPublishing && !state.isMediaUploading,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        LinkDraftScreenContent(
            state = state,
            actions = actions,
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        )
    }
}

package pl.masslany.podkop.features.linksubmission.addlink

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import pl.masslany.podkop.features.linksubmission.addlink.components.AddLinkBottomActionBar
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_topbar_back
import podkop.composeapp.generated.resources.add_link_action_continue
import podkop.composeapp.generated.resources.ic_arrow_back
import podkop.composeapp.generated.resources.topbar_label_add_link

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddLinkScreenRoot() {
    val viewModel = koinViewModel<AddLinkStartViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions: AddLinkStartActions = viewModel

    LifecycleStartEffect(viewModel) {
        viewModel.onScreenOpened()
        onStopOrDispose {
            // no-op
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(resource = Res.string.topbar_label_add_link)) },
                navigationIcon = {
                    IconButton(onClick = actions::onTopBarBackClicked) {
                        Icon(
                            imageVector = vectorResource(resource = Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(resource = Res.string.accessibility_topbar_back),
                        )
                    }
                },
                windowInsets = WindowInsets.safeDrawing,
            )
        },
        bottomBar = {
            AddLinkBottomActionBar(
                primaryLabel = Res.string.add_link_action_continue,
                isPrimaryEnabled = state.canContinue,
                isLoading = state.isCheckingDraft,
                onPrimaryClicked = actions::onContinueClicked,
                onCancelClicked = actions::onTopBarBackClicked,
                isCancelEnabled = !state.isCheckingDraft,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        AddLinkScreenContent(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            state = state,
            actions = actions,
        )
    }
}

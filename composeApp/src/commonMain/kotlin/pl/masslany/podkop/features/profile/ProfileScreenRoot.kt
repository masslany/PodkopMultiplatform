package pl.masslany.podkop.features.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pl.masslany.podkop.features.profile.components.ProfileHeader
import pl.masslany.podkop.features.profile.components.ProfileSummary
import pl.masslany.podkop.features.resources.components.ResourceItemRenderer
import pl.masslany.podkop.features.resources.models.ResourceItemState
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_topbar_back
import podkop.composeapp.generated.resources.accessibility_topbar_settings
import podkop.composeapp.generated.resources.generic_error_body
import podkop.composeapp.generated.resources.generic_error_title
import podkop.composeapp.generated.resources.ic_arrow_back
import podkop.composeapp.generated.resources.ic_settings
import podkop.composeapp.generated.resources.profile_log_in_button
import podkop.composeapp.generated.resources.profile_not_logged_in_message
import podkop.composeapp.generated.resources.refresh_button
import podkop.composeapp.generated.resources.topbar_label_profile
import podkop.composeapp.generated.resources.user_profile_not_logged_in

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenRoot(
    username: String?,
    paddingValues: PaddingValues,
) {
    val viewModel = koinViewModel<ProfileViewModel>(
        parameters = { parametersOf(username) },
    )
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
                    Text(text = stringResource(resource = Res.string.topbar_label_profile))
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
                actions = {
                    if (state.content.shouldShowSettingsButton()) {
                        IconButton(onClick = viewModel::onTopBarSettingsClicked) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = vectorResource(resource = Res.drawable.ic_settings),
                                contentDescription = stringResource(
                                    resource = Res.string.accessibility_topbar_settings,
                                ),
                            )
                        }
                    }
                },
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPaddingValues ->

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        } else {
            ProfileScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = innerPaddingValues),
                state = state,
                actions = viewModel,
            )
        }
    }
}

@Composable
private fun ProfileScreen(
    modifier: Modifier = Modifier,
    state: ProfileScreenState,
    actions: ProfileActions,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        when (val content = state.content) {
            ProfileContentState.Empty -> Unit

            ProfileContentState.LoggedOut -> {
                ProfileLoggedOutContent(actions = actions)
            }

            is ProfileContentState.Loaded -> {
                ProfileLoadedContent(
                    content = content,
                    resources = state.resources,
                    actions = actions,
                )
            }

            ProfileContentState.Error -> {
                ProfileErrorContent(actions = actions)
            }
        }
    }
}

@Composable
private fun ProfileLoadedContent(
    content: ProfileContentState.Loaded,
    resources: ImmutableList<ResourceItemState>,
    actions: ProfileActions,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item(
            key = "ProfileHeader",
        ) {
            ProfileHeader(
                state = content.header,
            )
            Spacer(modifier = Modifier.size(16.dp))
        }

        item(
            key = "ProfileSummary",
        ) {
            ProfileSummary(
                summary = content.summary,
            )
            Spacer(modifier = Modifier.size(8.dp))
        }

        items(
            items = resources,
            key = { item -> "${item.contentType}_${item.id}" },
            contentType = { item -> item.contentType },
        ) { resource ->
            ResourceItemRenderer(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                state = resource,
                actions = actions,
            )
        }
    }
}

@Composable
private fun ProfileLoggedOutContent(actions: ProfileActions) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Image(
            modifier = Modifier.size(240.dp),
            painter = painterResource(resource = Res.drawable.user_profile_not_logged_in),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = stringResource(resource = Res.string.profile_not_logged_in_message))
        Button(onClick = actions::onLoginClicked) {
            Text(text = stringResource(resource = Res.string.profile_log_in_button))
        }
    }
}

@Composable
private fun ProfileErrorContent(actions: ProfileActions) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(resource = Res.string.generic_error_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(resource = Res.string.generic_error_body),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Button(onClick = actions::onRetryClicked) {
            Text(
                text = stringResource(resource = Res.string.refresh_button),
            )
        }
    }
}

private fun ProfileContentState.shouldShowSettingsButton(): Boolean =
    when (this) {
        ProfileContentState.LoggedOut -> true
        is ProfileContentState.Loaded -> isCurrentUser
        else -> false
    }

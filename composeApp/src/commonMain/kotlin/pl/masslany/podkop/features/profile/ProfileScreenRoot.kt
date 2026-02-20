package pl.masslany.podkop.features.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pl.masslany.podkop.common.components.pagination.PaginationLoadingIndicator
import pl.masslany.podkop.common.extensions.isScrollingUp
import pl.masslany.podkop.common.pagination.rememberLazyListPaginator
import pl.masslany.podkop.common.snackbar.LocalAppSnackbarHostState
import pl.masslany.podkop.features.profile.components.ObservedTagItem
import pl.masslany.podkop.features.profile.components.ObservedUserItem
import pl.masslany.podkop.features.profile.components.ProfileErrorContent
import pl.masslany.podkop.features.profile.components.ProfileHeader
import pl.masslany.podkop.features.profile.components.ProfileLoggedOutContent
import pl.masslany.podkop.features.profile.components.ProfileSubActionDropdown
import pl.masslany.podkop.features.profile.components.ProfileSummary
import pl.masslany.podkop.features.profile.models.ProfileContentState
import pl.masslany.podkop.features.profile.models.ProfileListContentState
import pl.masslany.podkop.features.profile.models.ProfileObservedTagItemState
import pl.masslany.podkop.features.profile.models.ProfileObservedUserItemState
import pl.masslany.podkop.features.resources.components.ResourceItemRenderer
import pl.masslany.podkop.features.resources.models.ResourceItemState
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_fab_scroll_to_top
import podkop.composeapp.generated.resources.accessibility_topbar_back
import podkop.composeapp.generated.resources.accessibility_topbar_settings
import podkop.composeapp.generated.resources.ic_arrow_back
import podkop.composeapp.generated.resources.ic_keyboard_arrow_up
import podkop.composeapp.generated.resources.ic_settings
import podkop.composeapp.generated.resources.profile_no_observed_tags
import podkop.composeapp.generated.resources.profile_no_observed_users
import podkop.composeapp.generated.resources.topbar_label_profile

private const val FabItemsOffset = 10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenRoot(
    username: String?,
    paddingValues: PaddingValues,
) {
    val viewModel = koinViewModel<ProfileViewModel>(
        parameters = { parametersOf(username) },
    )
    val snackbarHostState = LocalAppSnackbarHostState.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val lazyListState = rememberLazyListPaginator(
        shouldPaginate = { lastVisibleIndex, totalItems ->
            viewModel.shouldPaginate(lastVisibleIndex, totalItems)
        },
        paginate = {
            viewModel.paginate()
        },
    )
    val isScrollingUp = lazyListState.isScrollingUp()
    val showFab by remember(isScrollingUp) {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > FabItemsOffset && isScrollingUp
        }
    }
    val showProfileName by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemScrollOffset > 0
        }
    }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
            )
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    when (state.content) {
                        is ProfileContentState.Loaded -> {
                            if (showProfileName) {
                                val profileLabel = stringResource(resource = Res.string.topbar_label_profile)
                                val username = (state.content as? ProfileContentState.Loaded)?.header?.username
                                Text(text = username ?: profileLabel)
                            }
                        }

                        ProfileContentState.Error -> {
                            Text(text = stringResource(resource = Res.string.topbar_label_profile))
                        }

                        ProfileContentState.LoggedOut -> {
                            Text(text = stringResource(resource = Res.string.topbar_label_profile))
                        }

                        ProfileContentState.Empty -> Unit
                    }
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
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            scrollBehavior.state.heightOffset = 0f
                            scrollBehavior.state.contentOffset = 0f
                            lazyListState.animateScrollToItem(0)
                        }
                    },
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = vectorResource(resource = Res.drawable.ic_keyboard_arrow_up),
                        contentDescription = stringResource(
                            resource = Res.string.accessibility_fab_scroll_to_top,
                        ),
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                lazyListState = lazyListState,
            )
        }
    }
}

@Composable
private fun ProfileScreen(
    modifier: Modifier = Modifier,
    state: ProfileScreenState,
    actions: ProfileActions,
    lazyListState: LazyListState,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        when (val content = state.content) {
            ProfileContentState.Empty -> Unit

            ProfileContentState.LoggedOut -> {
                ProfileLoggedOutContent(
                    onLoginClicked = actions::onLoginClicked,
                )
            }

            is ProfileContentState.Loaded -> {
                ProfileLoadedContent(
                    content = content,
                    state = state,
                    actions = actions,
                    lazyListState = lazyListState,
                )
            }

            ProfileContentState.Error -> {
                ProfileErrorContent(
                    onRetryClicked = actions::onRetryClicked,
                )
            }
        }
    }
}

@Composable
private fun ProfileLoadedContent(
    content: ProfileContentState.Loaded,
    state: ProfileScreenState,
    actions: ProfileActions,
    lazyListState: LazyListState,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(
            bottom = WindowInsets
                .systemBars
                .asPaddingValues()
                .calculateBottomPadding() + 16.dp,
        ),
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
                selectedType = content.selectedSummaryType,
                onSelected = actions::onSummarySelected,
            )
            Spacer(modifier = Modifier.size(8.dp))
        }

        if (content.subActionState.items.size > 1) {
            item(
                key = "ProfileSubActionDropdown",
            ) {
                ProfileSubActionDropdown(
                    subActionState = content.subActionState,
                    actions = actions,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
            }
        }

        if (state.isResourcesLoading) {
            item(
                key = "ResourcesLoadingIndicator",
            ) {
                PaginationLoadingIndicator()
            }
        }

        renderListContent(
            listContent = state.listContent,
            actions = actions,
        )

        if (state.isPaginating) {
            item(
                key = "PaginationLoadingIndicator",
            ) {
                PaginationLoadingIndicator()
            }
        }
    }
}

private fun LazyListScope.renderListContent(
    listContent: ProfileListContentState,
    actions: ProfileActions,
) {
    when (listContent) {
        ProfileListContentState.Empty -> Unit

        is ProfileListContentState.Resources -> {
            renderResources(
                resources = listContent.items,
                actions = actions,
            )
        }

        is ProfileListContentState.ObservedUsers -> {
            renderObservedUsers(
                users = listContent.items,
                actions = actions,
            )
        }

        is ProfileListContentState.ObservedTags -> {
            renderObservedTags(
                tags = listContent.items,
                actions = actions,
            )
        }
    }
}

private fun LazyListScope.renderResources(
    resources: ImmutableList<ResourceItemState>,
    actions: ProfileActions,
) {
    if (resources.isEmpty()) return

    resources.forEach { resource ->
        item(
            key = "${resource.contentType}_${resource.id}",
            contentType = resource.contentType,
        ) {
            ResourceItemRenderer(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                state = resource,
                actions = actions,
            )
        }
    }
}

private fun LazyListScope.renderObservedUsers(
    users: ImmutableList<ProfileObservedUserItemState>,
    actions: ProfileActions,
) {
    if (users.isEmpty()) {
        item(
            key = "no_observed_users",
            contentType = "no_observed_users",
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(resource = Res.string.profile_no_observed_users),
                )
            }
        }
    } else {
        users.forEach { user ->
            item(
                key = "observed_user_${user.username}",
                contentType = "observed_user",
            ) {
                ObservedUserItem(
                    user = user,
                    onClick = { actions.onProfileClicked(user.username) },
                )
            }
        }
    }
}

private fun LazyListScope.renderObservedTags(
    tags: ImmutableList<ProfileObservedTagItemState>,
    actions: ProfileActions,
) {
    if (tags.isEmpty()) {
        item(
            key = "no_observed_tags",
            contentType = "no_observed_tags",
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(resource = Res.string.profile_no_observed_tags),
                )
            }
        }
    } else {
        tags.forEach { tag ->
            item(
                key = "observed_tag_${tag.name}",
                contentType = "observed_tag",
            ) {
                ObservedTagItem(
                    tag = tag,
                    onClick = { actions.onTagClicked(tag.name) },
                )
            }
        }
    }
}

private fun ProfileContentState.shouldShowSettingsButton(): Boolean =
    when (this) {
        ProfileContentState.LoggedOut -> true
        is ProfileContentState.Loaded -> isCurrentUser
        else -> false
    }

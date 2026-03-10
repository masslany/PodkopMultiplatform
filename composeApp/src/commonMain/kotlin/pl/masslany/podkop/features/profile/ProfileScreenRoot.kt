package pl.masslany.podkop.features.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pl.masslany.podkop.common.components.GenericErrorScreen
import pl.masslany.podkop.common.components.pagination.PaginationLoadingIndicator
import pl.masslany.podkop.common.extensions.isScrollingUp
import pl.masslany.podkop.common.pagination.rememberLazyListPaginator
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.common.snackbar.LocalAppSnackbarHostState
import pl.masslany.podkop.features.profile.components.ObservedTagItem
import pl.masslany.podkop.features.profile.components.ObservedUserItem
import pl.masslany.podkop.features.profile.components.ProfileAchievementsSection
import pl.masslany.podkop.features.profile.components.ProfileHeader
import pl.masslany.podkop.features.profile.components.ProfileNoteSection
import pl.masslany.podkop.features.profile.components.ProfileSubActionDropdown
import pl.masslany.podkop.features.profile.components.ProfileSummary
import pl.masslany.podkop.features.profile.models.ProfileListContentState
import pl.masslany.podkop.features.profile.models.ProfileObservedTagItemState
import pl.masslany.podkop.features.profile.models.ProfileObservedUserItemState
import pl.masslany.podkop.features.profile.preview.NoOpProfileActions
import pl.masslany.podkop.features.profile.preview.ProfileScreenStateProvider
import pl.masslany.podkop.features.resources.components.ResourceItemRenderer
import pl.masslany.podkop.features.resources.models.ResourceItemConfig
import pl.masslany.podkop.features.resources.models.ResourceItemState
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_fab_scroll_to_top
import podkop.composeapp.generated.resources.accessibility_lock_icon
import podkop.composeapp.generated.resources.accessibility_profile_observe
import podkop.composeapp.generated.resources.accessibility_profile_send_private_message
import podkop.composeapp.generated.resources.accessibility_profile_unobserve
import podkop.composeapp.generated.resources.accessibility_topbar_back
import podkop.composeapp.generated.resources.accessibility_unlock_icon
import podkop.composeapp.generated.resources.ic_arrow_back
import podkop.composeapp.generated.resources.ic_keyboard_arrow_up
import podkop.composeapp.generated.resources.ic_lock
import podkop.composeapp.generated.resources.ic_lock_open_right
import podkop.composeapp.generated.resources.ic_mail
import podkop.composeapp.generated.resources.ic_visibility_off
import podkop.composeapp.generated.resources.ic_visibility_on
import podkop.composeapp.generated.resources.profile_no_observed_tags
import podkop.composeapp.generated.resources.profile_no_observed_users
import podkop.composeapp.generated.resources.topbar_label_profile

private const val FAB_ITEMS_OFFSET = 10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfileScreenRoot(
    username: String,
    paddingValues: PaddingValues,
) {
    val viewModel = koinViewModel<ProfileViewModel>(
        parameters = { parametersOf(username) },
    )
    val snackbarHostState = LocalAppSnackbarHostState.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListPaginator(
        shouldPaginate = { lastVisibleIndex, totalItems ->
            viewModel.shouldPaginate(lastVisibleIndex, totalItems)
        },
        paginate = {
            viewModel.paginate()
        },
    )

    ProfileScreenContent(
        paddingValues = paddingValues,
        state = state,
        actions = viewModel,
        lazyListState = lazyListState,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(
    paddingValues: PaddingValues,
    state: ProfileScreenState,
    actions: ProfileActions,
    lazyListState: LazyListState,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val isScrollingUp = lazyListState.isScrollingUp()
    val showFab by remember(isScrollingUp) {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > FAB_ITEMS_OFFSET && isScrollingUp
        }
    }
    val showProfileName by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemScrollOffset > 0
        }
    }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
            )
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    if (state.isError) {
                        Text(text = stringResource(resource = Res.string.topbar_label_profile))
                    } else if (showProfileName) {
                        state.header?.let { header ->
                            Text(text = header.username)
                        }
                    }
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
                actions = {
                    state.header?.let { header ->
                        if (header.canSendPrivateMessage) {
                            IconButton(onClick = actions::onPrivateMessageClicked) {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    imageVector = vectorResource(resource = Res.drawable.ic_mail),
                                    contentDescription = stringResource(
                                        resource = Res.string.accessibility_profile_send_private_message,
                                    ),
                                )
                            }
                        }

                        if (header.isLoggedIn && !header.isOwnProfile) {
                            IconButton(
                                onClick = actions::onBlacklistClicked,
                                enabled = !state.isBlacklistActionLoading,
                            ) {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    imageVector = vectorResource(
                                        resource = if (header.isBlacklisted) {
                                            Res.drawable.ic_lock_open_right
                                        } else {
                                            Res.drawable.ic_lock
                                        },
                                    ),
                                    contentDescription = stringResource(
                                        resource = if (header.isBlacklisted) {
                                            Res.string.accessibility_unlock_icon
                                        } else {
                                            Res.string.accessibility_lock_icon
                                        },
                                    ),
                                )
                            }
                        }

                        if (header.canManageObservation) {
                            IconButton(
                                onClick = actions::onObserveClicked,
                                enabled = !state.isObserveActionLoading,
                            ) {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    imageVector = vectorResource(
                                        resource = if (header.isObserved) {
                                            Res.drawable.ic_visibility_off
                                        } else {
                                            Res.drawable.ic_visibility_on
                                        },
                                    ),
                                    contentDescription = stringResource(
                                        resource = if (header.isObserved) {
                                            Res.string.accessibility_profile_unobserve
                                        } else {
                                            Res.string.accessibility_profile_observe
                                        },
                                    ),
                                )
                            }
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
        } else if (state.isError) {
            GenericErrorScreen(
                onRefreshClicked = actions::onRetryClicked,
            )
        } else {
            ProfileScreenBody(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = innerPaddingValues),
                state = state,
                actions = actions,
                lazyListState = lazyListState,
            )
        }
    }
}

@Composable
private fun ProfileScreenBody(
    modifier: Modifier = Modifier,
    state: ProfileScreenState,
    actions: ProfileActions,
    lazyListState: LazyListState,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        ProfileLoadedContent(
            state = state,
            actions = actions,
            lazyListState = lazyListState,
        )
    }
}

@Composable
private fun ProfileLoadedContent(
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
            state.header?.let {
                ProfileHeader(
                    state = state.header,
                    isDetailsExpanded = state.isDetailsExpanded,
                    onDetailsToggleClicked = actions::onDetailsToggleClicked,
                )
                Spacer(modifier = Modifier.size(16.dp))
            }
        }

        item(
            key = "ProfileDetailsSection",
        ) {
            if (state.isDetailsExpanded) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ProfileNoteSection(
                        state = state.noteState,
                        onContentChanged = actions::onNoteContentChanged,
                        onSaveClicked = actions::onNoteSaveClicked,
                    )
                    ProfileAchievementsSection(
                        state = state.achievementsState,
                    )
                }
            }
        }

        item(
            key = "ProfileSummary",
        ) {
            ProfileSummary(
                summary = state.summary,
                selectedType = state.selectedSummaryType,
                onSelected = actions::onSummarySelected,
            )
            Spacer(modifier = Modifier.size(8.dp))
        }

        if (state.subActionState.items.size > 1) {
            item(
                key = "ProfileSubActionDropdown",
            ) {
                ProfileSubActionDropdown(
                    subActionState = state.subActionState,
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
                config = ResourceItemConfig(
                    showReplyAction = true,
                    isReplyActionEnabled = true,
                ),
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

@Preview
@Composable
private fun ProfileScreenContentPreview(
    @PreviewParameter(ProfileScreenStateProvider::class) state: ProfileScreenState,
) {
    PodkopPreview(darkTheme = false) {
        ProfileScreenContent(
            paddingValues = PaddingValues(),
            state = state,
            actions = NoOpProfileActions,
            lazyListState = rememberLazyListState(),
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

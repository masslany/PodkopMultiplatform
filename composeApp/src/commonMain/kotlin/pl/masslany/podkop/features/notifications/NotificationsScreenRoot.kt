package pl.masslany.podkop.features.notifications

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import pl.masslany.podkop.common.components.GenericErrorScreen
import pl.masslany.podkop.common.extensions.toWindowInsets
import pl.masslany.podkop.common.pagination.rememberLazyListPaginator
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.notifications.components.NotificationsList
import pl.masslany.podkop.features.notifications.components.NotificationsScreenHeader
import pl.masslany.podkop.features.notifications.preview.NoOpNotificationsActions
import pl.masslany.podkop.features.notifications.preview.NotificationsScreenStateProvider
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_topbar_back
import podkop.composeapp.generated.resources.ic_arrow_back
import podkop.composeapp.generated.resources.topbar_label_notifications

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotificationsScreenRoot(
    paddingValues: PaddingValues,
) {
    val viewModel = koinViewModel<NotificationsViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListPaginator(
        resetStateKey = state.selectedGroup.name,
        shouldPaginate = { lastVisibleIndex, totalItems ->
            viewModel.shouldPaginate(lastVisibleIndex, totalItems)
        },
        paginate = {
            viewModel.paginate()
        },
    )

    NotificationsScreenContent(
        paddingValues = paddingValues,
        state = state,
        actions = viewModel,
        lazyListState = lazyListState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreenContent(
    paddingValues: PaddingValues,
    state: NotificationsScreenState,
    actions: NotificationsActions,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val topBarInsets = paddingValues.toWindowInsets(includeBottom = false)
    val contentInsets = paddingValues.toWindowInsets(includeTop = false)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(resource = Res.string.topbar_label_notifications))
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
                scrollBehavior = scrollBehavior,
                windowInsets = topBarInsets,
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = contentInsets,
    ) { innerPaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPaddingValues),
        ) {
            NotificationsScreenHeader(
                state = state,
                onGroupSelected = actions::onGroupSelected,
                onMarkAllAsReadClicked = actions::onMarkAllAsReadClicked,
            )

            HorizontalDivider()

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = actions::onRefresh,
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                when {
                    state.isLoading -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                            )
                        }
                    }

                    state.isError -> {
                        GenericErrorScreen(
                            modifier = Modifier
                                .padding(top = 32.dp)
                                .fillMaxSize()
                                .align(Alignment.Center),
                            onRefreshClicked = actions::onRefresh,
                        )
                    }

                    else -> {
                        NotificationsList(
                            state = state,
                            actions = actions,
                            lazyListState = lazyListState,
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun NotificationsScreenContentPreview(
    @PreviewParameter(NotificationsScreenStateProvider::class) state: NotificationsScreenState,
) {
    PodkopPreview(darkTheme = false) {
        NotificationsScreenContent(
            paddingValues = PaddingValues(),
            state = state,
            actions = NoOpNotificationsActions,
            lazyListState = LazyListState(),
        )
    }
}

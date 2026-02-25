package pl.masslany.podkop.features.resourceactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pl.masslany.podkop.common.components.GenericErrorScreen
import pl.masslany.podkop.common.components.pagination.PaginationLoadingIndicator
import pl.masslany.podkop.common.pagination.rememberLazyListPaginator
import pl.masslany.podkop.features.profile.components.ObservedUserItem
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.resource_votes_empty

@Composable
fun ResourceVotesBottomSheetScreenRoot(
    screen: ResourceVotesBottomSheetScreen,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<ResourceVotesBottomSheetViewModel>(
        parameters = {
            parametersOf(
                ResourceVotesParams(
                    resourceType = screen.resourceType,
                    entryId = screen.entryId,
                    entryCommentId = screen.entryCommentId,
                ),
            )
        },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListPaginator(
        shouldPaginate = { lastVisibleIndex, totalItems ->
            viewModel.shouldPaginate(lastVisibleIndex, totalItems)
        },
        paginate = {
            viewModel.paginate()
        },
    )

    ResourceVotesBottomSheetContent(
        state = state,
        actions = viewModel,
        lazyListState = lazyListState,
        modifier = modifier,
    )
}

@Composable
internal fun ResourceVotesBottomSheetContent(
    state: ResourceVotesBottomSheetState,
    actions: ResourceVotesBottomSheetActions,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val nestedScrollConnection = remember(lazyListState) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source != NestedScrollSource.UserInput || available.y == 0f) return Offset.Zero

                // Allow dragging the sheet down from the top of the list.
                // Only block leftover upward deltas at the bottom edge to avoid sheet jitter.
                return if (available.y < 0f && lazyListState.isAtBottom()) {
                    Offset(x = 0f, y = available.y)
                } else {
                    Offset.Zero
                }
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity,
            ): Velocity = if (available.y < 0f && lazyListState.isAtBottom()) {
                Velocity(x = 0f, y = available.y)
            } else {
                Velocity.Zero
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
    ) {
        val maxSheetHeight = if (maxHeight == Dp.Infinity) {
            560.dp
        } else {
            maxHeight * 0.9f
        }
        val contentModifier = Modifier
            .fillMaxWidth()
            .heightIn(
                min = ResourceVotesBottomSheetMinHeight,
                max = maxSheetHeight,
            )
            .navigationBarsPadding()

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = contentModifier,
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.isError -> {
                    Box(
                        modifier = contentModifier,
                        contentAlignment = Alignment.Center,
                    ) {
                        GenericErrorScreen(
                            onRefreshClicked = actions::onRetryClicked,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }

                state.items.isEmpty() -> {
                    Box(
                        modifier = contentModifier,
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = stringResource(resource = Res.string.resource_votes_empty),
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = contentModifier.nestedScroll(nestedScrollConnection),
                        state = lazyListState,
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(
                            items = state.items,
                            key = { user -> "voter_${user.username}" },
                            contentType = { "voter" },
                        ) { user ->
                            ObservedUserItem(
                                user = user,
                                onClick = { actions.onUserClicked(user.username) },
                            )
                        }

                        if (state.isPaginating) {
                            item(
                                key = "PaginationLoadingIndicator",
                            ) {
                                PaginationLoadingIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun LazyListState.isAtBottom(): Boolean = canScrollBackward && !canScrollForward

private val ResourceVotesBottomSheetMinHeight = 220.dp

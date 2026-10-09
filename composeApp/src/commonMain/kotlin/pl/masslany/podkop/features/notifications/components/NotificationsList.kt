package pl.masslany.podkop.features.notifications.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.components.pagination.PaginationLoadingIndicator
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.notifications.NotificationsActions
import pl.masslany.podkop.features.notifications.NotificationsScreenState
import pl.masslany.podkop.features.notifications.NotificationsTestTags
import pl.masslany.podkop.features.notifications.models.NotificationGroupExpansionState
import pl.masslany.podkop.features.notifications.preview.NoOpNotificationsActions
import pl.masslany.podkop.features.notifications.preview.NotificationsPreviewFixtures
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.notifications_empty_state
import podkop.composeapp.generated.resources.notifications_grouped_show_more

@Composable
fun NotificationsList(
    state: NotificationsScreenState,
    actions: NotificationsActions,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
) {
    if (state.items.isEmpty()) {
        Box(modifier = modifier.fillMaxSize()) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                text = stringResource(resource = Res.string.notifications_empty_state),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .testTag(NotificationsTestTags.Screen.List),
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 16.dp,
            ),
        ) {
            state.items.forEach { item ->
                item(key = item.id) {
                    NotificationCard(
                        state = item,
                        onClick = {
                            actions.onNotificationClicked(item.id)
                        },
                        onExpandClick = item.groupId?.let {
                            { actions.onGroupedRowExpandToggled(item.id) }
                        },
                    )
                }

                item.expansion?.let { expansion ->
                    groupedRowMembers(
                        rowId = item.id,
                        expansion = expansion,
                        actions = actions,
                    )
                }
            }

            if (state.isPaginating) {
                item(key = "pagination_loading") {
                    PaginationLoadingIndicator()
                }
            }
        }
    }
}

private fun LazyListScope.groupedRowMembers(
    rowId: String,
    expansion: NotificationGroupExpansionState,
    actions: NotificationsActions,
) {
    items(
        items = expansion.items,
        key = { member -> groupedRowMemberKey(rowId, member.id) },
    ) { member ->
        NotificationCard(
            modifier = Modifier.padding(start = 24.dp),
            state = member,
            onClick = {
                actions.onGroupedRowNotificationClicked(rowId = rowId, id = member.id)
            },
        )
    }

    if (expansion.isLoading) {
        item(key = "$rowId/loading") {
            PaginationLoadingIndicator()
        }
    } else if (expansion.canLoadMore) {
        item(key = "$rowId/show-more") {
            Box(modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag(NotificationsTestTags.Grouped.showMore(rowId)),
                    onClick = { actions.onGroupedRowShowMoreClicked(rowId) },
                ) {
                    Text(text = stringResource(resource = Res.string.notifications_grouped_show_more))
                }
            }
        }
    }
}

/** List key of a notification shown inside the expanded grouped row [rowId]. */
fun groupedRowMemberKey(
    rowId: String,
    memberId: String,
): String = "$rowId/$memberId"

@Preview(name = "Notifications List")
@Composable
private fun NotificationsListPreview() {
    PodkopPreview(darkTheme = false) {
        NotificationsList(
            state = NotificationsPreviewFixtures.contentState(),
            actions = NoOpNotificationsActions,
            lazyListState = rememberLazyListState(),
        )
    }
}

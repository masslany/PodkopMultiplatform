package pl.masslany.podkop.features.notifications.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.components.pagination.PaginationLoadingIndicator
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.notifications.NotificationsActions
import pl.masslany.podkop.features.notifications.NotificationsScreenState
import pl.masslany.podkop.features.notifications.preview.NoOpNotificationsActions
import pl.masslany.podkop.features.notifications.preview.NotificationsPreviewFixtures
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.notifications_empty_state

@Composable
fun NotificationsList(
    state: NotificationsScreenState,
    actions: NotificationsActions,
    lazyListState: LazyListState,
    bottomPadding: androidx.compose.ui.unit.Dp,
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
            modifier = modifier.fillMaxSize(),
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + bottomPadding + 16.dp,
            ),
        ) {
            items(
                items = state.items,
                key = { item -> item.id },
            ) { item ->
                NotificationCard(
                    state = item,
                    onClick = {
                        actions.onNotificationClicked(item.id)
                    },
                )
            }

            if (state.isPaginating) {
                item(key = "pagination_loading") {
                    PaginationLoadingIndicator()
                }
            }
        }
    }
}

@Preview(name = "Notifications List")
@Composable
private fun NotificationsListPreview() {
    PodkopPreview(darkTheme = false) {
        NotificationsList(
            state = NotificationsPreviewFixtures.contentState(),
            actions = NoOpNotificationsActions,
            lazyListState = rememberLazyListState(),
            bottomPadding = 0.dp,
        )
    }
}

package pl.masslany.podkop.features.notifications.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.notifications.NotificationsScreenState
import pl.masslany.podkop.features.notifications.models.NotificationGroupChipState
import pl.masslany.podkop.features.notifications.preview.NotificationsPreviewFixtures
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.notifications_button_mark_all_read
import podkop.composeapp.generated.resources.notifications_group_entries
import podkop.composeapp.generated.resources.notifications_group_observed_discussions
import podkop.composeapp.generated.resources.notifications_group_private_messages
import podkop.composeapp.generated.resources.notifications_group_tags

@Composable
fun NotificationsScreenHeader(
    state: NotificationsScreenState,
    onGroupSelected: (NotificationGroup) -> Unit,
    onMarkAllAsReadClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = 8.dp,
                bottom = 12.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
            ),
        ) {
            items(
                items = state.groups,
                key = { chip -> chip.group.name },
            ) { chip ->
                FilterChip(
                    selected = chip.selected,
                    onClick = {
                        onGroupSelected(chip.group)
                    },
                    label = {
                        Text(text = chip.label())
                    },
                )
            }
        }

        if (state.canMarkAllAsRead || state.isMarkingAllAsRead) {
            FilledTonalButton(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                    ),
                onClick = onMarkAllAsReadClicked,
                enabled = !state.isMarkingAllAsRead,

            ) {
                if (state.isMarkingAllAsRead) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = stringResource(resource = Res.string.notifications_button_mark_all_read),
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationGroupChipState.label(): String {
    val label = stringResource(resource = group.labelRes())
    return "$label ($unreadCount)"
}

private fun NotificationGroup.labelRes(): StringResource = when (this) {
    NotificationGroup.Entries -> Res.string.notifications_group_entries
    NotificationGroup.PrivateMessages -> Res.string.notifications_group_private_messages
    NotificationGroup.Tags -> Res.string.notifications_group_tags
    NotificationGroup.ObservedDiscussions -> Res.string.notifications_group_observed_discussions
}

@Preview(name = "Notifications Header")
@Composable
private fun NotificationsScreenHeaderPreview() {
    PodkopPreview(darkTheme = false) {
        NotificationsScreenHeader(
            state = NotificationsPreviewFixtures.contentState(NotificationGroup.Tags),
            onGroupSelected = {},
            onMarkAllAsReadClicked = {},
        )
    }
}

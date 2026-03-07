package pl.masslany.podkop.features.notifications.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.features.notifications.NotificationsScreenState

class NotificationsScreenStateProvider : PreviewParameterProvider<NotificationsScreenState> {
    override val values: Sequence<NotificationsScreenState> = sequenceOf(
        NotificationsScreenState.initial,
        NotificationsScreenState.initial.copy(
            isLoading = false,
            isError = true,
            groups = NotificationsPreviewFixtures.groups(NotificationGroup.Entries),
        ),
        NotificationsPreviewFixtures.contentState(NotificationGroup.Tags),
        NotificationsPreviewFixtures.contentState(NotificationGroup.ObservedDiscussions),
    )
}

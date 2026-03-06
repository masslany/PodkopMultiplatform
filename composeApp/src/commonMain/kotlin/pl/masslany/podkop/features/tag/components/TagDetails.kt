package pl.masslany.podkop.features.tag.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import pl.masslany.podkop.features.tag.TagActions
import pl.masslany.podkop.features.tag.TagScreenState
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_tag_disable_notifications
import podkop.composeapp.generated.resources.accessibility_tag_enable_notifications
import podkop.composeapp.generated.resources.ic_notifications_active
import podkop.composeapp.generated.resources.ic_notifications_off
import podkop.composeapp.generated.resources.ic_visibility_on
import podkop.composeapp.generated.resources.tag_details_screen_observe_button
import podkop.composeapp.generated.resources.tag_details_screen_observed_button

@Composable
fun TagDetails(
    state: TagScreenState,
    actions: TagActions,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = "#${state.tag}",
            style = MaterialTheme.typography.headlineSmall,
        )

        if (state.isLoggedIn) {
            Row(
                modifier = Modifier.padding(start = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.isObserved) {
                    val notificationsEnabled = state.areNotificationsEnabled
                    val notificationIcon = if (notificationsEnabled) {
                        Res.drawable.ic_notifications_active
                    } else {
                        Res.drawable.ic_notifications_off
                    }
                    val notificationContentDescription = if (notificationsEnabled) {
                        stringResource(resource = Res.string.accessibility_tag_disable_notifications)
                    } else {
                        stringResource(resource = Res.string.accessibility_tag_enable_notifications)
                    }

                    if (notificationsEnabled) {
                        FilledTonalIconButton(
                            modifier = Modifier.size(32.dp),
                            onClick = actions::onNotificationsClicked,
                            enabled = !state.isNotificationsActionLoading,
                        ) {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                imageVector = vectorResource(resource = notificationIcon),
                                contentDescription = notificationContentDescription,
                            )
                        }
                    } else {
                        FilledTonalIconButton(
                            modifier = Modifier.size(32.dp),
                            onClick = actions::onNotificationsClicked,
                            enabled = !state.isNotificationsActionLoading,
                        ) {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                imageVector = vectorResource(resource = notificationIcon),
                                contentDescription = notificationContentDescription,
                            )
                        }
                    }
                }

                Button(
                    modifier = Modifier.defaultMinSize(minHeight = 32.dp),
                    onClick = actions::onObserveClicked,
                    enabled = !state.isObserveActionLoading,
                ) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = vectorResource(resource = Res.drawable.ic_visibility_on),
                        contentDescription = null,
                    )
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(
                            resource = if (state.isObserved) {
                                Res.string.tag_details_screen_observed_button
                            } else {
                                Res.string.tag_details_screen_observe_button
                            },
                        ),
                    )
                }
            }
        }
    }
}

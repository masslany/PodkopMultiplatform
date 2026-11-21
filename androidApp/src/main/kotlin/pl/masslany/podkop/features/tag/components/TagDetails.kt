package pl.masslany.podkop.features.tag.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import pl.masslany.podkop.R
import pl.masslany.podkop.features.tag.TagActions
import pl.masslany.podkop.features.tag.TagScreenState

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
                FilledTonalIconButton(
                    modifier = Modifier.size(32.dp),
                    onClick = actions::onBlacklistClicked,
                    enabled = !state.isBlacklistActionLoading,
                    shapes = IconButtonDefaults.shapes(),
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = ImageVector.vectorResource(
                            id = if (state.isBlacklisted) {
                                R.drawable.ic_lock_open_right
                            } else {
                                R.drawable.ic_lock
                            },
                        ),
                        contentDescription = stringResource(
                            id = if (state.isBlacklisted) {
                                R.string.accessibility_unlock_icon
                            } else {
                                R.string.accessibility_lock_icon
                            },
                        ),
                    )
                }

                if (state.isObserved) {
                    val notificationsEnabled = state.areNotificationsEnabled
                    val notificationIcon = if (notificationsEnabled) {
                        R.drawable.ic_notifications_active
                    } else {
                        R.drawable.ic_notifications_off
                    }
                    val notificationContentDescription = if (notificationsEnabled) {
                        stringResource(id = R.string.accessibility_tag_disable_notifications)
                    } else {
                        stringResource(id = R.string.accessibility_tag_enable_notifications)
                    }

                    if (notificationsEnabled) {
                        FilledTonalIconButton(
                            modifier = Modifier.size(32.dp),
                            onClick = actions::onNotificationsClicked,
                            enabled = !state.isNotificationsActionLoading,
                            shapes = IconButtonDefaults.shapes(),
                        ) {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                imageVector = ImageVector.vectorResource(id = notificationIcon),
                                contentDescription = notificationContentDescription,
                            )
                        }
                    } else {
                        FilledTonalIconButton(
                            modifier = Modifier.size(32.dp),
                            onClick = actions::onNotificationsClicked,
                            enabled = !state.isNotificationsActionLoading,
                            shapes = IconButtonDefaults.shapes(),
                        ) {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                imageVector = ImageVector.vectorResource(id = notificationIcon),
                                contentDescription = notificationContentDescription,
                            )
                        }
                    }
                }

                Button(
                    modifier = Modifier.defaultMinSize(minHeight = 32.dp),
                    onClick = actions::onObserveClicked,
                    enabled = !state.isObserveActionLoading,
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_visibility_on),
                        contentDescription = null,
                    )
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(
                            id = if (state.isObserved) {
                                R.string.tag_details_screen_observed_button
                            } else {
                                R.string.tag_details_screen_observe_button
                            },
                        ),
                    )
                }
            }
        }
    }
}

package pl.masslany.podkop.features.topbar

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_topbar_notifications
import podkop.composeapp.generated.resources.accessibility_topbar_search
import podkop.composeapp.generated.resources.ic_notifications
import podkop.composeapp.generated.resources.ic_search

@Composable
fun FeedTopBarActions(
    isLoggedIn: Boolean,
    notificationsUnreadCount: Int,
    onSearchClicked: () -> Unit,
    onNotificationsClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onSearchClicked,
        modifier = modifier,
    ) {
        Icon(
            imageVector = vectorResource(resource = Res.drawable.ic_search),
            contentDescription = stringResource(resource = Res.string.accessibility_topbar_search),
        )
    }

    if (isLoggedIn) {
        IconButton(
            onClick = onNotificationsClicked,
            modifier = modifier,
        ) {
            BadgedBox(
                badge = {
                    if (notificationsUnreadCount > 0) {
                        Badge()
                    }
                },
            ) {
                Icon(
                    imageVector = vectorResource(resource = Res.drawable.ic_notifications),
                    contentDescription = stringResource(resource = Res.string.accessibility_topbar_notifications),
                )
            }
        }
    }
}

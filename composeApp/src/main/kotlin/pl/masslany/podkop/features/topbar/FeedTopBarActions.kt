package pl.masslany.podkop.features.topbar

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import pl.masslany.podkop.composeapp.R

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
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_search),
            contentDescription = stringResource(id = R.string.accessibility_topbar_search),
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
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_notifications),
                    contentDescription = stringResource(id = R.string.accessibility_topbar_notifications),
                )
            }
        }
    }
}

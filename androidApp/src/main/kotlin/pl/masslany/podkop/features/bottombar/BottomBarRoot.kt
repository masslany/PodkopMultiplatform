package pl.masslany.podkop.features.bottombar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.R
import pl.masslany.podkop.common.navigation.NavTarget
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.entries.EntriesScreen
import pl.masslany.podkop.features.links.LinksScreen
import pl.masslany.podkop.features.upcoming.UpcomingScreen

@Composable
fun BottomBarRoot(
    destinations: ImmutableList<BottomBarDestinationState>,
    onScreenChanged: (NavTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    BottomBar(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                clip = false,
            ),
        destinations = destinations,
        onScreenChanged = onScreenChanged,
    )
}

@Composable
private fun BottomBar(
    modifier: Modifier = Modifier,
    destinations: ImmutableList<BottomBarDestinationState>,
    onScreenChanged: (NavTarget) -> Unit,
) {
    NavigationBar(
        modifier = modifier,
    ) {
        destinations.forEach { bottomBarDestinationState ->
            NavigationBarItemContent(
                iconRes = bottomBarDestinationState.iconRes,
                labelRes = bottomBarDestinationState.labelRes,
                badgeCount = bottomBarDestinationState.badgeCount,
                content = { itemModifier, icon, label ->
                    NavigationBarItem(
                        modifier = itemModifier,
                        selected = bottomBarDestinationState.isSelected,
                        enabled = bottomBarDestinationState.isEnabled,
                        onClick = { onScreenChanged(bottomBarDestinationState.screen) },
                        icon = icon,
                        alwaysShowLabel = true,
                        label = label,
                    )
                },
            )
        }
    }
}

@Composable
fun SideBarRoot(
    destinations: ImmutableList<BottomBarDestinationState>,
    onScreenChanged: (NavTarget) -> Unit,
    modifier: Modifier = Modifier,
    onSizeChanged: ((Int) -> Unit)? = null,
) {
    NavigationRail(
        modifier = modifier
            .onSizeChanged { size ->
                onSizeChanged?.invoke(size.width)
            }
            .shadow(
                elevation = 8.dp,
                clip = false,
            ),
    ) {
        Spacer(modifier = Modifier.weight(1f))
        destinations.forEach { destination ->
            NavigationBarItemContent(
                iconRes = destination.iconRes,
                labelRes = destination.labelRes,
                badgeCount = destination.badgeCount,
                content = { itemModifier, icon, label ->
                    NavigationRailItem(
                        modifier = itemModifier,
                        selected = destination.isSelected,
                        enabled = destination.isEnabled,
                        onClick = { onScreenChanged(destination.screen) },
                        icon = icon,
                        label = label,
                        alwaysShowLabel = true,
                    )
                },
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun NavigationBarItemContent(
    @DrawableRes iconRes: Int,
    @StringRes labelRes: Int,
    badgeCount: Int,
    content: @Composable (
        itemModifier: Modifier,
        icon: @Composable () -> Unit,
        label: @Composable () -> Unit,
    ) -> Unit,
) {
    content(
        Modifier
            .semantics {
                role = Role.Tab
            },
        {
            BadgedBox(
                badge = {
                    if (badgeCount > 0) {
                        Badge {
                            Text(text = badgeCount.toBadgeLabel())
                        }
                    }
                },
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                )
            }
        },
        {
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelSmall,
            )
        },
    )
}

@Preview
@Composable
private fun BottomBarRootPreview() {
    PodkopPreview(darkTheme = false) {
        BottomBarRoot(
            destinations = persistentListOf(
                BottomBarDestinationState(
                    screen = LinksScreen,
                    isSelected = true,
                    isEnabled = true,
                    iconRes = R.drawable.ic_home,
                    labelRes = R.string.navigation_label_homepage,
                    badgeCount = 0,
                ),
                BottomBarDestinationState(
                    screen = UpcomingScreen,
                    isSelected = false,
                    isEnabled = true,
                    iconRes = R.drawable.ic_nav_shovel,
                    labelRes = R.string.navigation_label_upcoming,
                    badgeCount = 0,
                ),
                BottomBarDestinationState(
                    screen = EntriesScreen,
                    isSelected = false,
                    isEnabled = true,
                    iconRes = R.drawable.ic_nav_letter_m,
                    labelRes = R.string.navigation_label_entries,
                    badgeCount = 12,
                ),
            ),
            onScreenChanged = {},
            modifier = Modifier,
        )
    }
}

@Preview
@Composable
private fun SideBarRootPreview() {
    PodkopPreview(darkTheme = false) {
        SideBarRoot(
            destinations = persistentListOf(
                BottomBarDestinationState(
                    screen = LinksScreen,
                    isSelected = false,
                    isEnabled = true,
                    iconRes = R.drawable.ic_home,
                    labelRes = R.string.navigation_label_homepage,
                    badgeCount = 0,
                ),
                BottomBarDestinationState(
                    screen = EntriesScreen,
                    isSelected = true,
                    isEnabled = true,
                    iconRes = R.drawable.ic_nav_letter_m,
                    labelRes = R.string.navigation_label_entries,
                    badgeCount = 3,
                ),
            ),
            onScreenChanged = {},
        )
    }
}

private fun Int.toBadgeLabel(): String = if (this > 99) "99+" else toString()

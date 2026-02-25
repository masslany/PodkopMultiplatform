package pl.masslany.podkop.features.bottombar

import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.navigation.NavTarget
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.entries.EntriesScreen
import pl.masslany.podkop.features.links.LinksScreen
import pl.masslany.podkop.features.upcoming.UpcomingScreen
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.ic_home
import podkop.composeapp.generated.resources.ic_nav_letter_m
import podkop.composeapp.generated.resources.ic_nav_shovel
import podkop.composeapp.generated.resources.navigation_label_entries
import podkop.composeapp.generated.resources.navigation_label_homepage
import podkop.composeapp.generated.resources.navigation_label_upcoming

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
    iconRes: org.jetbrains.compose.resources.DrawableResource,
    labelRes: org.jetbrains.compose.resources.StringResource,
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
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
            )
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
                    iconRes = Res.drawable.ic_home,
                    labelRes = Res.string.navigation_label_homepage,
                ),
                BottomBarDestinationState(
                    screen = UpcomingScreen,
                    isSelected = false,
                    isEnabled = true,
                    iconRes = Res.drawable.ic_nav_shovel,
                    labelRes = Res.string.navigation_label_upcoming,
                ),
                BottomBarDestinationState(
                    screen = EntriesScreen,
                    isSelected = false,
                    isEnabled = true,
                    iconRes = Res.drawable.ic_nav_letter_m,
                    labelRes = Res.string.navigation_label_entries,
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
                    iconRes = Res.drawable.ic_home,
                    labelRes = Res.string.navigation_label_homepage,
                ),
                BottomBarDestinationState(
                    screen = EntriesScreen,
                    isSelected = true,
                    isEnabled = true,
                    iconRes = Res.drawable.ic_nav_letter_m,
                    labelRes = Res.string.navigation_label_entries,
                ),
            ),
            onScreenChanged = {},
        )
    }
}

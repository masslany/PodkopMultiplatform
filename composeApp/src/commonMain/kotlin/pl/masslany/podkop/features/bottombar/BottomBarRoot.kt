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
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.navigation.NavTarget

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

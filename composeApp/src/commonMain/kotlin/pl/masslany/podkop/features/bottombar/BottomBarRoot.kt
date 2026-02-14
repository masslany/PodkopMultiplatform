package pl.masslany.podkop.features.bottombar

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
            NavigationBarItem(
                modifier = Modifier
                    .semantics {
                        role = Role.Tab
                    },
                selected = bottomBarDestinationState.isSelected,
                enabled = bottomBarDestinationState.isEnabled,
                onClick = { onScreenChanged(bottomBarDestinationState.screen) },
                icon = {
                    Icon(
                        painter = painterResource(bottomBarDestinationState.iconRes),
                        contentDescription = null,
                    )
                },
                alwaysShowLabel = true,
                label = {
                    Text(
                        text = stringResource(bottomBarDestinationState.labelRes),
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
            )
        }
    }
}

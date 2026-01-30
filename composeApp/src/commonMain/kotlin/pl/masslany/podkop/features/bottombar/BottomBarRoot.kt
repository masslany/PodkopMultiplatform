package pl.masslany.podkop.features.bottombar

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BottomBarRoot(
    modifier: Modifier = Modifier,
) {
    val viewModel: BottomBarViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    BottomBar(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                clip = false,
            ),
        destinations = state.destinations,
        actions = viewModel,
    )
}

@Composable
private fun BottomBar(
    modifier: Modifier = Modifier,
    destinations: ImmutableList<BottomBarDestinationState>,
    actions: BottomBarActions,
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
                onClick = { actions.onScreenChanged(bottomBarDestinationState.screen) },
                icon = {
                    Icon(
                        painter = painterResource(bottomBarDestinationState.iconRes),
                        contentDescription = null
                    )
                },
                alwaysShowLabel = true,
                label = {
                    Text(
                        text = stringResource(bottomBarDestinationState.labelRes),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
            )
        }
    }
}


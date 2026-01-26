package pl.masslany.podkop.features.links

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pl.masslany.podkop.features.resources.components.ResourceItemRenderer

@Composable
fun LinksScreenRoot(
    isUpcoming: Boolean,
    paddingValues: PaddingValues,
) {
    val viewModel = koinViewModel<LinksViewModel>(
        parameters = { parametersOf(isUpcoming) }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    LinksScreen(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        state = state,
        actions = viewModel,
    )
}

@Composable
private fun LinksScreen(
    modifier: Modifier = Modifier,
    state: LinksScreenState,
    actions: LinksActions,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!state.isUpcoming) {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.hits) {
                        Card(
                            modifier = Modifier
                                .width(160.dp)
                                .height(120.dp)
                                .clip(CardDefaults.shape)
                                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
                        ) {
                            ResourceItemRenderer(it, actions)
                        }
                    }
                }
            }
        }

        items(state.links) {
            Card {
                ResourceItemRenderer(it, actions)
            }
        }
    }
}
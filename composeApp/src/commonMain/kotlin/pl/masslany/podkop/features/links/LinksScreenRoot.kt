package pl.masslany.podkop.features.links

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pl.masslany.podkop.features.links.hits.HitsList
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (!state.isUpcoming) {
            item {
                HitsList(
                    modifier = Modifier
                        .fillMaxSize(),
                    state = state.hits,
                    actions = actions,
                )
            }
        }

        items(
            items = state.links,
            key = { item -> item.id }
        ) {
            ResourceItemRenderer(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                state = it,
                actions = actions
            )
        }
    }
}
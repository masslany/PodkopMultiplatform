package pl.masslany.podkop.features.links

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

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

    Text(
        text = "LinksScreen upcoming = $state"
    )
}
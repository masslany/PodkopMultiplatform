package pl.masslany.podkop.features.more

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun MoreScreenRoot(
    paddingValues: PaddingValues,
) {
    val viewModel = koinViewModel<MoreViewModel>()
    val state = viewModel.state.collectAsStateWithLifecycle()
}

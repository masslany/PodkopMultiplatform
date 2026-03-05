package pl.masslany.podkop.features.composer

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pl.masslany.podkop.common.composer.Composer
import pl.masslany.podkop.common.navigation.LocalBottomSheetState
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.entries_composer_hint
import podkop.composeapp.generated.resources.entry_details_reply_composer_hint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposerBottomSheetScreenRoot(
    screen: ComposerBottomSheetScreen,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<ComposerBottomSheetViewModel>(
        parameters = { parametersOf(screen) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    val bottomSheetState = LocalBottomSheetState.current
    val autoFocusEnabled = bottomSheetState?.currentValue != SheetValue.Hidden

    Composer(
        modifier = modifier,
        state = state.composer,
        autoFocus = autoFocusEnabled,
        hintText = stringResource(resource = screen.request.hintTextResource()),
        onContentChanged = viewModel::onComposerTextChanged,
        onAdultChanged = viewModel::onComposerAdultChanged,
        onPhotoAttachClicked = viewModel::onComposerPhotoAttachClicked,
        onPhotoRemoved = viewModel::onComposerPhotoRemoved,
        onDismiss = viewModel::onComposerDismissed,
        onSubmit = viewModel::onComposerSubmit,
    )
}

private fun ComposerRequest.hintTextResource(): StringResource = when (this) {
    is ComposerRequest.CreateEntry,
    is ComposerRequest.EditEntry,
    -> Res.string.entries_composer_hint

    is ComposerRequest.CreateEntryComment,
    is ComposerRequest.CreateLinkComment,
    is ComposerRequest.EditEntryComment,
    is ComposerRequest.EditLinkComment,
    -> Res.string.entry_details_reply_composer_hint
}

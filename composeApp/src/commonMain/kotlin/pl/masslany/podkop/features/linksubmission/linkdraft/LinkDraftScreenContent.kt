package pl.masslany.podkop.features.linksubmission.linkdraft

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.linksubmission.linkdraft.components.LinkDraftStepContent
import pl.masslany.podkop.features.linksubmission.linkdraft.preview.LinkDraftScreenStateProvider
import pl.masslany.podkop.features.linksubmission.linkdraft.preview.NoOpLinkDraftActions

@Composable
internal fun LinkDraftScreenContent(
    state: LinkDraftScreenState,
    actions: LinkDraftActions,
    modifier: Modifier = Modifier,
) {
    if (state.isLoadingDraft) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            LinkDraftStepContent(
                state = state,
                onTitleChanged = actions::onTitleChanged,
                onDescriptionChanged = actions::onDescriptionChanged,
                onTagInputChanged = actions::onTagInputChanged,
                onPendingTagSubmitted = actions::onPendingTagSubmitted,
                onTagSuggestionClicked = actions::onTagSuggestionClicked,
                onTagRemoved = actions::onTagRemoved,
                onAdultChanged = actions::onAdultChanged,
                onSuggestedImageChanged = actions::onSuggestedImageChanged,
                onPhotoAttachClicked = actions::onPhotoAttachClicked,
                onPhotoRemoved = actions::onPhotoRemoved,
            )
        }
    }
}

@Preview
@Composable
private fun LinkDraftScreenContentPreview(
    @PreviewParameter(LinkDraftScreenStateProvider::class) state: LinkDraftScreenState,
) {
    PodkopPreview(darkTheme = false) {
        LinkDraftScreenContent(
            state = state,
            actions = NoOpLinkDraftActions,
            modifier = Modifier.padding(16.dp),
        )
    }
}

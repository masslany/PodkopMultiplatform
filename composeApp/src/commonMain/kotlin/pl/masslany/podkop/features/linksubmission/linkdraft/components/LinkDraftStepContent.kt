package pl.masslany.podkop.features.linksubmission.linkdraft.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.features.linksubmission.addlink.components.CurrentUrlField
import pl.masslany.podkop.features.linksubmission.linkdraft.LinkDraftScreenState
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.add_link_description_hint
import podkop.composeapp.generated.resources.add_link_description_label
import podkop.composeapp.generated.resources.add_link_tag_add
import podkop.composeapp.generated.resources.add_link_tag_label
import podkop.composeapp.generated.resources.add_link_title_hint
import podkop.composeapp.generated.resources.add_link_title_label
import podkop.composeapp.generated.resources.links_screen_label_adult_rating

@Composable
internal fun LinkDraftStepContent(
    state: LinkDraftScreenState,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onTagInputChanged: (String) -> Unit,
    onPendingTagSubmitted: () -> Unit,
    onTagSuggestionClicked: (String) -> Unit,
    onTagRemoved: (String) -> Unit,
    onAdultChanged: (Boolean) -> Unit,
    onSuggestedImageChanged: (Int) -> Unit,
    onPhotoAttachClicked: () -> Unit,
    onPhotoRemoved: () -> Unit,
) {
    if (state.currentUrl.isNotBlank()) {
        CurrentUrlField(url = state.currentUrl)
    }

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = state.title,
        onValueChange = onTitleChanged,
        label = {
            Text(text = stringResource(resource = Res.string.add_link_title_label))
        },
        placeholder = {
            Text(text = stringResource(resource = Res.string.add_link_title_hint))
        },
        singleLine = true,
    )

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = state.description,
        onValueChange = onDescriptionChanged,
        label = {
            Text(text = stringResource(resource = Res.string.add_link_description_label))
        },
        placeholder = {
            Text(text = stringResource(resource = Res.string.add_link_description_hint))
        },
        minLines = 6,
    )

    LinkDraftImageSection(
        suggestedImages = state.suggestedImages,
        selectedSuggestedImageIndex = state.selectedSuggestedImageIndex,
        photoUrl = state.photoUrl,
        isMediaUploading = state.isMediaUploading,
        onSuggestedImageChanged = onSuggestedImageChanged,
        onPhotoAttachClicked = onPhotoAttachClicked,
        onPhotoRemoved = onPhotoRemoved,
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(resource = Res.string.add_link_tag_label),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        LinkDraftTagInputField(
            tagInput = state.tagInput,
            tagSuggestions = state.tagSuggestions,
            isLoadingTagSuggestions = state.isLoadingTagSuggestions,
            onTagInputChanged = onTagInputChanged,
            onPendingTagSubmitted = onPendingTagSubmitted,
            onSuggestionClicked = onTagSuggestionClicked,
        )
        if (state.tagInput.isNotBlank()) {
            TextButton(
                modifier = Modifier.align(Alignment.End),
                onClick = onPendingTagSubmitted,
            ) {
                Text(text = stringResource(resource = Res.string.add_link_tag_add))
            }
        }
        if (state.tags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.tags.forEach { tag ->
                    AddLinkTagChip(
                        tag = tag,
                        onRemoveClicked = { onTagRemoved(tag) },
                    )
                }
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(resource = Res.string.links_screen_label_adult_rating),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Switch(
            checked = state.adult,
            onCheckedChange = onAdultChanged,
            enabled = !state.isMediaUploading && !state.isPublishing,
        )
    }
}

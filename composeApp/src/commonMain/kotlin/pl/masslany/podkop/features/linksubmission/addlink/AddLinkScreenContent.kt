package pl.masslany.podkop.features.linksubmission.addlink

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.linksubmission.addlink.components.SavedDraftsSection
import pl.masslany.podkop.features.linksubmission.addlink.components.SimilarLinks
import pl.masslany.podkop.features.linksubmission.models.AddLinkSavedDraftState
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.add_link_intro
import podkop.composeapp.generated.resources.add_link_url_hint
import podkop.composeapp.generated.resources.add_link_url_invalid
import podkop.composeapp.generated.resources.add_link_url_label

@Composable
internal fun AddLinkScreenContent(
    state: AddLinkStartState,
    actions: AddLinkStartActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        if (state.hasSavedDrafts) {
            SavedDraftsSection(
                drafts = state.currentDrafts,
                deletingDraftKey = state.deletingDraftKey,
                isActionEnabled = state.deletingDraftKey == null && !state.isCheckingDraft,
                onContinueClicked = actions::onSavedDraftContinueClicked,
                onDeleteClicked = actions::onSavedDraftDeleteClicked,
            )
        }

        Text(
            text = stringResource(resource = Res.string.add_link_intro),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.url,
            onValueChange = actions::onUrlChanged,
            label = {
                Text(text = stringResource(resource = Res.string.add_link_url_label))
            },
            placeholder = {
                Text(text = stringResource(resource = Res.string.add_link_url_hint))
            },
            singleLine = true,
            isError = state.showUrlFormatError,
            supportingText = if (state.showUrlFormatError) {
                {
                    Text(text = stringResource(resource = Res.string.add_link_url_invalid))
                }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { actions.onContinueClicked() },
            ),
        )

        if (state.hasSimilarLinks) {
            SimilarLinks(
                similarLinks = state.similarLinks,
            )
        }
    }
}

@Preview
@Composable
private fun AddLinkScreenContentPreview() {
    PodkopPreview(darkTheme = false) {
        AddLinkScreenContent(
            state = AddLinkStartState.initial.copy(
                currentDrafts = kotlinx.collections.immutable.persistentListOf(
                    AddLinkSavedDraftState(
                        key = "draft-1",
                        title = "Świnoujście zawarło ugodę z wykonawcą tunelu pod Świną",
                        url = "https://inzynieria.com/tunele/wiadomosci/99940,swinoujscie-zawarlo-ugode-z-wykonawca-tunelu-pod-swina",
                    ),
                ),
                url = "https://example.com",
            ),
            actions = object : AddLinkStartActions {
                override fun onTopBarBackClicked() = Unit
                override fun onTopBarSearchClicked() = Unit
                override fun onTopBarNotificationsClicked() = Unit
                override fun onUrlChanged(value: String) = Unit
                override fun onContinueClicked() = Unit
                override fun onSavedDraftContinueClicked(key: String) = Unit
                override fun onSavedDraftDeleteClicked(key: String) = Unit
                override fun onTopBarAddEntryClicked() = Unit
                override fun onTopBarAddLinkClicked() = Unit
            },
            modifier = Modifier.padding(16.dp),
        )
    }
}

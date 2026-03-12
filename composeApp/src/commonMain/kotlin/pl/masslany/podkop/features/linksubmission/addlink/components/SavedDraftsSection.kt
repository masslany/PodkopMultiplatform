package pl.masslany.podkop.features.linksubmission.addlink.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import pl.masslany.podkop.features.linksubmission.models.AddLinkSavedDraftState
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.add_link_saved_draft_title_fallback
import podkop.composeapp.generated.resources.add_link_saved_drafts_body
import podkop.composeapp.generated.resources.add_link_saved_drafts_title
import podkop.composeapp.generated.resources.ic_delete
import podkop.composeapp.generated.resources.ic_edit
import podkop.composeapp.generated.resources.resource_actions_delete_entry
import podkop.composeapp.generated.resources.resource_actions_edit_entry

@Composable
internal fun SavedDraftsSection(
    drafts: ImmutableList<AddLinkSavedDraftState>,
    deletingDraftKey: String?,
    isActionEnabled: Boolean,
    onContinueClicked: (String) -> Unit,
    onDeleteClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(resource = Res.string.add_link_saved_drafts_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(resource = Res.string.add_link_saved_drafts_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            drafts.forEachIndexed { index, draft ->
                AddLinkSavedDraftItem(
                    draft = draft,
                    isDeleting = deletingDraftKey == draft.key,
                    isActionEnabled = isActionEnabled,
                    onContinueClicked = { onContinueClicked(draft.key) },
                    onDeleteClicked = { onDeleteClicked(draft.key) },
                )
                if (index != drafts.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun AddLinkSavedDraftItem(
    draft: AddLinkSavedDraftState,
    isDeleting: Boolean,
    isActionEnabled: Boolean,
    onContinueClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = draft.url.toDisplayUrl(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = draft.title.ifBlank {
                    stringResource(resource = Res.string.add_link_saved_draft_title_fallback)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Row {
            IconButton(
                onClick = onContinueClicked,
                enabled = isActionEnabled && !isDeleting,
            ) {
                Icon(
                    imageVector = vectorResource(resource = Res.drawable.ic_edit),
                    contentDescription = stringResource(resource = Res.string.resource_actions_edit_entry),
                )
            }
            IconButton(
                onClick = onDeleteClicked,
                enabled = isActionEnabled && !isDeleting,
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector = vectorResource(resource = Res.drawable.ic_delete),
                        contentDescription = stringResource(resource = Res.string.resource_actions_delete_entry),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

private fun String.toDisplayUrl(): String = removePrefix("https://")
    .removePrefix("http://")
    .trimEnd('/')

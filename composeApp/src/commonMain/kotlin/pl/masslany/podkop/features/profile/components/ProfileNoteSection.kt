package pl.masslany.podkop.features.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.features.profile.models.ProfileNoteState
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.profile_note_error
import podkop.composeapp.generated.resources.profile_note_placeholder
import podkop.composeapp.generated.resources.profile_note_save
import podkop.composeapp.generated.resources.profile_note_title

@Composable
internal fun ProfileNoteSection(
    state: ProfileNoteState,
    onContentChanged: (String) -> Unit,
    onSaveClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(resource = Res.string.profile_note_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                    )
                }
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.content,
                onValueChange = onContentChanged,
                minLines = 3,
                maxLines = 6,
                enabled = !state.isLoading && !state.isSaving,
                placeholder = {
                    Text(text = stringResource(resource = Res.string.profile_note_placeholder))
                },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(
                    onClick = onSaveClicked,
                    enabled = state.canSave,
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(text = stringResource(resource = Res.string.profile_note_save))
                    }
                }
            }

            if (state.isError) {
                Text(
                    text = stringResource(resource = Res.string.profile_note_error),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

package pl.masslany.podkop.common.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.models.EntryContentState
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.comment_label_removed_by_author
import podkop.composeapp.generated.resources.comment_label_removed_by_moderator

@Composable
fun EntryContentRouter(state: EntryContentState) {
    when (state) {
        is EntryContentState.Content -> {
            if (state.content.isNotEmpty()) {
//                MarkdownText(
//                    markdown = state.content,
//                    style = MaterialTheme.typography.bodySmall
//                        .copy(
//                            color = MaterialTheme.colorScheme.onSurface,
//                        ),
//                    linkColor = MaterialTheme.colorsPalette.tagBlue,
//                    onLinkClicked = { },
//                    linkifyMask = Linkify.WEB_URLS,
//                )
                Text(
                    text = state.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        EntryContentState.DeletedByAuthor -> {
            Text(
                text = stringResource(resource = Res.string.comment_label_removed_by_author),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            )
        }
        EntryContentState.DeletedByModerator -> {
            Text(
                text = stringResource(resource = Res.string.comment_label_removed_by_moderator),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            )
        }
    }
}
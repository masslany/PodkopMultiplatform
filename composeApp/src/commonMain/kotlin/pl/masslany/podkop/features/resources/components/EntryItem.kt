package pl.masslany.podkop.features.resources.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.masslany.podkop.common.components.Author
import pl.masslany.podkop.common.components.Avatar
import pl.masslany.podkop.common.components.EntryContentRouter
import pl.masslany.podkop.common.components.Published
import pl.masslany.podkop.common.components.vote.Vote
import pl.masslany.podkop.features.resources.models.entry.EntryItemState

@Composable
fun EntryItem(
    state: EntryItemState,
    modifier: Modifier = Modifier,
    onProfileClick: (String) -> Unit,
    onVoteUpClick: () -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        Row {
            Row(
                modifier = Modifier.weight(1f),
            ) {
                Avatar(
                    state = state.avatarState,
                    onClick = { onProfileClick(state.authorState?.name.orEmpty()) },
                )
                Spacer(Modifier.size(8.dp))
                Column {
                    state.authorState?.let {
                        Author(
                            state = state.authorState,
                            onClick = { onProfileClick(state.authorState.name) },
                        )
                    }
                    state.publishedTimeType?.let {
                        Published(
                            type = state.publishedTimeType,
                        )
                    }
                }
            }
            Vote(
                state = state.voteState,
                onVoteUpClick = onVoteUpClick,
                onVoteDownClick = { /* no-op */ },
            )
        }
        Spacer(Modifier.size(8.dp))
        EntryContentRouter(state = state.entryContentState)
    }
}

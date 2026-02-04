package pl.masslany.podkop.common.components.vote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.masslany.podkop.common.models.vote.VoteState
import pl.masslany.podkop.common.models.vote.VoteValueType
import pl.masslany.podkop.common.theme.colorsPalette

@Composable
fun Vote(
    state: VoteState,
    modifier: Modifier = Modifier,
    onVoteUpClick: () -> Unit,
    onVoteDownClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        VoteValue(state.voteValueType)
        state.positiveVoteButtonState?.let {
            VoteButton(
                state = state.positiveVoteButtonState,
                onVoteUpClick = onVoteUpClick,
                onVoteDownClick = onVoteDownClick,
            )
        }
        state.negativeVoteButtonState?.let {
            VoteButton(
                state = state.negativeVoteButtonState,
                onVoteUpClick = onVoteUpClick,
                onVoteDownClick = onVoteDownClick,
            )
        }
    }
}

@Composable
fun VoteValue(voteValueType: VoteValueType) {
    when (voteValueType) {
        is VoteValueType.Negative -> {
            Text(
                text = voteValueType.value,
                color = MaterialTheme.colorsPalette.voteNegative,
                style = MaterialTheme.typography.labelLarge,
            )
        }

        is VoteValueType.Positive -> {
            Text(
                text = "+${voteValueType.value}",
                color = MaterialTheme.colorsPalette.votePositive,
                style = MaterialTheme.typography.labelLarge,
            )
        }

        is VoteValueType.Zero -> {
            Text(
                text = "0",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

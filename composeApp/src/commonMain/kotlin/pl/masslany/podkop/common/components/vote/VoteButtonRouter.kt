package pl.masslany.podkop.common.components.vote

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.vectorResource
import pl.masslany.podkop.common.models.vote.VoteButtonState
import pl.masslany.podkop.common.models.vote.VoteButtonType
import pl.masslany.podkop.common.theme.colorsPalette
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.ic_add
import podkop.composeapp.generated.resources.ic_remove

@Composable
fun VoteButton(
    state: VoteButtonState,
    modifier: Modifier = Modifier,
    onVoteUpClick: () -> Unit,
    onVoteDownClick: () -> Unit,
) {
    when (state.voteButtonType) {
        VoteButtonType.Positive -> PositiveButton(
            modifier = modifier,
            state = state,
            onClick = onVoteUpClick,
        )
        VoteButtonType.Negative -> NegativeButton(
            modifier = modifier,
            state = state,
            onClick = onVoteDownClick,
        )
    }
}

@Composable
private fun PositiveButton(
    modifier: Modifier,
    state: VoteButtonState,
    onClick: () -> Unit ,
) {
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorsPalette.votePositive,
                shape = RoundedCornerShape(4.dp)
            )
            .background(
                color = getPositiveBackgroundColor(state.isVoted),
                shape = RoundedCornerShape(4.dp)
            )
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(4.dp)
            .height(12.dp)
            .aspectRatio(1f)
    ) {
        Icon(
            imageVector = vectorResource(resource = Res.drawable.ic_add),
            contentDescription = null,
            tint = getPositiveIconColor(state.isVoted),
        )
    }
}

@Composable
private fun NegativeButton(
    modifier: Modifier,
    state: VoteButtonState,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorsPalette.voteNegative,
                shape = RoundedCornerShape(4.dp)
            )
            .background(
                color = getNegativeBackgroundColor(state.isVoted),
                shape = RoundedCornerShape(4.dp)
            )
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(4.dp)
            .height(12.dp)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {

        Icon(
            imageVector = vectorResource(resource = Res.drawable.ic_remove),
            contentDescription = null,
            tint = getNegativeIconColor(state.isVoted)
        )
    }
}

@Composable
private fun getPositiveIconColor(isVoted: Boolean): Color {
    return if (isVoted) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorsPalette.votePositive
    }
}

@Composable
private fun getPositiveBackgroundColor(isVoted: Boolean): Color {
    return if (isVoted) {
        MaterialTheme.colorsPalette.votePositive
    } else {
        Color.Transparent
    }
}

@Composable
private fun getNegativeIconColor(isVoted: Boolean): Color {
    return if (isVoted) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorsPalette.voteNegative
    }
}

@Composable
private fun getNegativeBackgroundColor(isVoted: Boolean): Color {
    return if (isVoted) {
        MaterialTheme.colorsPalette.voteNegative
    } else {
        Color.Transparent
    }
}


package pl.masslany.podkop.common.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.models.survey.AnswerState
import pl.masslany.podkop.common.models.survey.SurveyState
import pl.masslany.podkop.common.preview.PodkopPreview
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.survey_answers_count
import podkop.composeapp.generated.resources.survey_hide_results
import podkop.composeapp.generated.resources.survey_show_results

@Composable
fun Survey(
    modifier: Modifier = Modifier,
    state: SurveyState,
    onVoteClick: (Int) -> Unit = {},
) {
    val canVote = state.isVoteActionEnabled
    var showResults by remember(state.areResultsVisibleByDefault) {
        mutableStateOf(state.areResultsVisibleByDefault)
    }
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                shape = RoundedCornerShape(8.dp),
            )
            .padding(8.dp),
    ) {
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = state.question,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.size(8.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            state.answers.forEachIndexed { index, answer ->
                AnswerItem(
                    state = answer,
                    showResults = showResults,
                    canVote = canVote,
                    onClick = { onVoteClick(index + 1) },
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Text(
                modifier = Modifier
                    .padding(top = if (!state.isResultsToggleVisible) 8.dp else 0.dp),
                text = stringResource(
                    resource = Res.string.survey_answers_count,
                    state.count,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            if (state.isResultsToggleVisible) {
                TextButton(
                    onClick = { showResults = !showResults },
                    contentPadding = PaddingValues(8.dp),
                ) {
                    Text(
                        text = resultsLabel(showResults),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun AnswerItem(
    state: AnswerState,
    showResults: Boolean,
    canVote: Boolean,
    onClick: () -> Unit,
) {
    val widthFraction = backgroundWidthFraction(state, showResults)
    val backgroundColor = if (state.isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (state.isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Box {
        Box(
            modifier = Modifier
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(8.dp),
                )
                .animateContentSize()
                .fillMaxWidth(widthFraction)
                .height(48.dp),
        )
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .then(
                    if (canVote) {
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier
                    },
                )
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                text = state.text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Normal,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (canVote && !showResults) {
                RadioButton(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    selected = false,
                    onClick = null,
                )
            }
            if (showResults) {
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = "${state.percentage}% (${state.count})",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Normal,
                    color = contentColor,
                    maxLines = 1,
                )
            }
        }
    }
}

private fun backgroundWidthFraction(
    state: AnswerState,
    showResults: Boolean,
): Float = if (showResults) state.percentageFraction else 1f

@Composable
private fun resultsLabel(showResults: Boolean): String =
    if (showResults) {
        stringResource(resource = Res.string.survey_hide_results)
    } else {
        stringResource(resource = Res.string.survey_show_results)
    }

@Preview
@Composable
private fun SurveyPreview() {
    PodkopPreview(darkTheme = false) {
        Survey(
            modifier = Modifier.padding(16.dp),
            state = SurveyState(
                question = "Which state should previews cover?",
                answers = persistentListOf(
                    AnswerState(
                        isSelected = true,
                        text = "Loading / error / empty",
                        count = 128,
                        percentageFraction = 0.64f,
                        percentage = "64",
                    ),
                    AnswerState(
                        isSelected = false,
                        text = "Content variations",
                        count = 52,
                        percentageFraction = 0.26f,
                        percentage = "26",
                    ),
                    AnswerState(
                        isSelected = false,
                        text = "Dark mode",
                        count = 20,
                        percentageFraction = 0.10f,
                        percentage = "10",
                    ),
                ),
                count = 200,
                votedOptionNumber = 1,
            ),
        )
    }
}

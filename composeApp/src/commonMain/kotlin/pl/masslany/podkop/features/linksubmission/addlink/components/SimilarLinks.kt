package pl.masslany.podkop.features.linksubmission.addlink.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.components.Count
import pl.masslany.podkop.common.models.CountState
import pl.masslany.podkop.features.linksubmission.models.AddLinkSimilarItemState
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.add_link_duplicate_body
import podkop.composeapp.generated.resources.add_link_duplicate_meta
import podkop.composeapp.generated.resources.add_link_duplicate_title

@Composable
internal fun SimilarLinks(
    similarLinks: ImmutableList<AddLinkSimilarItemState>,
) {
    Text(
        text = stringResource(resource = Res.string.add_link_duplicate_title),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
    )
    Text(
        text = stringResource(resource = Res.string.add_link_duplicate_body),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Surface(
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column {
            similarLinks.forEachIndexed { index, item ->
                SimilarLinkCard(
                    item = item,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (index != similarLinks.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun SimilarLinkCard(
    item: AddLinkSimilarItemState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Count(
            state = CountState(count = item.digCount.toString(), isHot = false, isVoted = false, canVote = false),
            modifier = Modifier.padding(end = 4.dp),
            onClick = { },
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = stringResource(
                    resource = Res.string.add_link_duplicate_meta,
                    item.createdAt,
                    item.sourceLabel,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

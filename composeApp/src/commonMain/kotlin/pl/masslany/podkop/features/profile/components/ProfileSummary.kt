package pl.masslany.podkop.features.profile.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.profile.models.ProfileSummaryItem
import pl.masslany.podkop.features.profile.models.ProfileSummaryType
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.profile_summary_actions
import podkop.composeapp.generated.resources.profile_summary_entries
import podkop.composeapp.generated.resources.profile_summary_followers
import podkop.composeapp.generated.resources.profile_summary_following
import podkop.composeapp.generated.resources.profile_summary_links

@Composable
fun ProfileSummary(
    summary: ImmutableList<ProfileSummaryItem>,
    selectedType: ProfileSummaryType,
    onSelected: (ProfileSummaryType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        summary.forEach { item ->
            val isSelected = item.type == selectedType
            Card(
                onClick = {
                    onSelected(item.type)
                },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    },
                ),
            ) {
                Column(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 92.dp)
                        .padding(
                            horizontal = 12.dp,
                            vertical = 10.dp,
                        ),
                ) {
                    Text(
                        text = summaryItemLabel(item),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.value.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun summaryItemLabel(item: ProfileSummaryItem): String =
    when (item) {
        is ProfileSummaryItem.Actions -> stringResource(resource = Res.string.profile_summary_actions)
        is ProfileSummaryItem.Entries -> stringResource(resource = Res.string.profile_summary_entries)
        is ProfileSummaryItem.Followers -> stringResource(resource = Res.string.profile_summary_followers)
        is ProfileSummaryItem.Following -> stringResource(resource = Res.string.profile_summary_following)
        is ProfileSummaryItem.Links -> stringResource(resource = Res.string.profile_summary_links)
    }

@Preview
@Composable
private fun ProfileSummaryPreview() {
    PodkopPreview(darkTheme = false) {
        ProfileSummary(
            modifier = Modifier.padding(horizontal = 16.dp),
            summary = persistentListOf(
                ProfileSummaryItem.Links(128),
                ProfileSummaryItem.Entries(42),
                ProfileSummaryItem.Followers(900),
                ProfileSummaryItem.Following(123),
            ),
            selectedType = ProfileSummaryType.Links,
            onSelected = {},
        )
    }
}

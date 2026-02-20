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
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.features.profile.ProfileSummaryItem
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.profile_summary_actions
import podkop.composeapp.generated.resources.profile_summary_entries
import podkop.composeapp.generated.resources.profile_summary_followers
import podkop.composeapp.generated.resources.profile_summary_following_tags
import podkop.composeapp.generated.resources.profile_summary_following_users
import podkop.composeapp.generated.resources.profile_summary_links

@Composable
fun ProfileSummary(
    summary: ImmutableList<ProfileSummaryItem>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        summary.forEach { item ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
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
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.value.toString(),
                        style = MaterialTheme.typography.bodySmall,
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
        is ProfileSummaryItem.FollowingTags -> stringResource(resource = Res.string.profile_summary_following_tags)
        is ProfileSummaryItem.FollowingUsers -> stringResource(resource = Res.string.profile_summary_following_users)
        is ProfileSummaryItem.Links -> stringResource(resource = Res.string.profile_summary_links)
    }

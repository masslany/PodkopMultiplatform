package pl.masslany.podkop.features.profile.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.DefaultRotation
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import pl.masslany.podkop.features.profile.ProfileActions
import pl.masslany.podkop.features.profile.models.ProfileSubActionState
import pl.masslany.podkop.features.profile.models.ProfileSubActionType
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.ic_arrow_dropdown
import podkop.composeapp.generated.resources.profile_sub_action_added
import podkop.composeapp.generated.resources.profile_sub_action_all
import podkop.composeapp.generated.resources.profile_sub_action_commented
import podkop.composeapp.generated.resources.profile_sub_action_down
import podkop.composeapp.generated.resources.profile_sub_action_followers
import podkop.composeapp.generated.resources.profile_sub_action_following_tags
import podkop.composeapp.generated.resources.profile_sub_action_following_users
import podkop.composeapp.generated.resources.profile_sub_action_published
import podkop.composeapp.generated.resources.profile_sub_action_related
import podkop.composeapp.generated.resources.profile_sub_action_up
import podkop.composeapp.generated.resources.profile_sub_action_voted

private const val ExpandedRotation = 180f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSubActionDropdown(
    subActionState: ProfileSubActionState,
    actions: ProfileActions,
    modifier: Modifier = Modifier,
) {
    val subActionLabels = subActionState.items.map { profileSubActionLabel(it) }
    val textMeasurer = rememberTextMeasurer()
    val labelTextStyle = MaterialTheme.typography.bodySmall
    val density = LocalDensity.current
    val minMenuWidth = remember(subActionLabels, labelTextStyle, density) {
        val widestLabelWidth = subActionLabels
            .maxOfOrNull { label ->
                textMeasurer.measure(
                    text = label,
                    style = labelTextStyle,
                    maxLines = 1,
                ).size.width
            } ?: 0

        // TextButton internal horizontal paddings plus breathing room.
        with(density) { widestLabelWidth.toDp() + 40.dp }
    }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = subActionState.expanded,
        onExpandedChange = actions::onSubActionExpandedChanged,
    ) {
        FilterChip(
            selected = true,
            onClick = {
                if (subActionState.expanded) {
                    actions.onSubActionDismissed()
                }
            },
            label = {
                Text(
                    text = profileSubActionLabel(subActionState.selected),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            },
            trailingIcon = {
                Icon(
                    modifier = Modifier.rotate(
                        if (subActionState.expanded) {
                            ExpandedRotation
                        } else {
                            DefaultRotation
                        },
                    ),
                    imageVector = vectorResource(resource = Res.drawable.ic_arrow_dropdown),
                    contentDescription = null,
                )
            },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )

        ExposedDropdownMenu(
            modifier = Modifier.widthIn(min = minMenuWidth),
            expanded = subActionState.expanded,
            onDismissRequest = actions::onSubActionDismissed,
        ) {
            subActionState.items.forEach { subActionType ->
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        actions.onSubActionSelected(subActionType)
                    },
                ) {
                    Text(
                        text = profileSubActionLabel(subActionType),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                    )
                }
            }
        }
    }
}

@Composable
private fun profileSubActionLabel(type: ProfileSubActionType): String =
    when (type) {
        ProfileSubActionType.Actions -> stringResource(resource = Res.string.profile_sub_action_all)
        ProfileSubActionType.EntriesAdded -> stringResource(resource = Res.string.profile_sub_action_added)
        ProfileSubActionType.EntriesVoted -> stringResource(resource = Res.string.profile_sub_action_voted)
        ProfileSubActionType.EntriesCommented -> stringResource(resource = Res.string.profile_sub_action_commented)
        ProfileSubActionType.LinksAdded -> stringResource(resource = Res.string.profile_sub_action_added)
        ProfileSubActionType.LinksPublished -> stringResource(resource = Res.string.profile_sub_action_published)
        ProfileSubActionType.LinksUp -> stringResource(resource = Res.string.profile_sub_action_up)
        ProfileSubActionType.LinksDown -> stringResource(resource = Res.string.profile_sub_action_down)
        ProfileSubActionType.LinksCommented -> stringResource(resource = Res.string.profile_sub_action_commented)
        ProfileSubActionType.LinksRelated -> stringResource(resource = Res.string.profile_sub_action_related)
        ProfileSubActionType.Followers -> stringResource(resource = Res.string.profile_sub_action_followers)
        ProfileSubActionType.FollowingTags -> stringResource(resource = Res.string.profile_sub_action_following_tags)
        ProfileSubActionType.FollowingUsers -> stringResource(resource = Res.string.profile_sub_action_following_users)
    }

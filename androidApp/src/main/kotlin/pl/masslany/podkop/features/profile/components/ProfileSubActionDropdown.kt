package pl.masslany.podkop.features.profile.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.R
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.profile.ProfileActions
import pl.masslany.podkop.features.profile.models.ProfileSubActionState
import pl.masslany.podkop.features.profile.models.ProfileSubActionType
import pl.masslany.podkop.features.profile.preview.NoOpProfileActions

private const val EXPANDED_ROTATION = 180f

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
                            EXPANDED_ROTATION
                        } else {
                            DefaultRotation
                        },
                    ),
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_dropdown),
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
        ProfileSubActionType.Actions -> stringResource(id = R.string.profile_sub_action_all)
        ProfileSubActionType.EntriesAdded -> stringResource(id = R.string.profile_sub_action_added)
        ProfileSubActionType.EntriesVoted -> stringResource(id = R.string.profile_sub_action_voted)
        ProfileSubActionType.EntriesCommented -> stringResource(id = R.string.profile_sub_action_commented)
        ProfileSubActionType.LinksAdded -> stringResource(id = R.string.profile_sub_action_added)
        ProfileSubActionType.LinksPublished -> stringResource(id = R.string.profile_sub_action_published)
        ProfileSubActionType.LinksUp -> stringResource(id = R.string.profile_sub_action_up)
        ProfileSubActionType.LinksDown -> stringResource(id = R.string.profile_sub_action_down)
        ProfileSubActionType.LinksCommented -> stringResource(id = R.string.profile_sub_action_commented)
        ProfileSubActionType.LinksRelated -> stringResource(id = R.string.profile_sub_action_related)
        ProfileSubActionType.Followers -> stringResource(id = R.string.profile_sub_action_followers)
        ProfileSubActionType.FollowingTags -> stringResource(id = R.string.profile_sub_action_following_tags)
        ProfileSubActionType.FollowingUsers -> stringResource(id = R.string.profile_sub_action_following_users)
    }

@Preview
@Composable
private fun ProfileSubActionDropdownPreview() {
    PodkopPreview(darkTheme = false) {
        ProfileSubActionDropdown(
            modifier = Modifier.padding(16.dp),
            subActionState = ProfileSubActionState(
                items = persistentListOf(
                    ProfileSubActionType.LinksAdded,
                    ProfileSubActionType.LinksPublished,
                    ProfileSubActionType.LinksCommented,
                ),
                selected = ProfileSubActionType.LinksPublished,
                expanded = false,
            ),
            actions = NoOpProfileActions,
        )
    }
}

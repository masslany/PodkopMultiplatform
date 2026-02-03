package pl.masslany.podkop.features.resources.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import pl.masslany.podkop.features.links.hits.HitItem
import pl.masslany.podkop.features.links.hits.models.HitItemState
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.resources.models.ResourceItemConfig
import pl.masslany.podkop.features.resources.models.ResourceItemState
import pl.masslany.podkop.features.resources.models.comment.EntryCommentItemState
import pl.masslany.podkop.features.resources.models.entry.EntryItemState
import pl.masslany.podkop.features.resources.models.link.LinkItemState

@Composable
fun ResourceItemRenderer(
    modifier: Modifier = Modifier,
    state: ResourceItemState,
    actions: ResourceItemActions,
    config: ResourceItemConfig = ResourceItemConfig(),
) {
    when (state) {
        is EntryItemState -> EntryItemRenderer(modifier, state, actions, config)
        is LinkItemState -> LinkItemRenderer(modifier, state, actions)
        is EntryCommentItemState -> EntryCommentItemRenderer(modifier, state, actions)
        is HitItemState -> HitItemRenderer(modifier, state, actions)
    }
}

@Composable
fun EntryItemRenderer(
    modifier: Modifier,
    state: EntryItemState,
    actions: ResourceItemActions,
    config: ResourceItemConfig,
) {
    if (config.renderEntryAsCard) {
        Card(
            modifier = modifier
                .clip(CardDefaults.shape)
                .clickable {  },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
            )
        ) {
            EntryItem(
                modifier = Modifier
                    .padding(16.dp),
                state = state,
                onProfileClick = { actions.onProfileClicked(it) },
                onVoteUpClick = {
                    actions.onEntryVoteUpClicked(
                        entryId = state.id,
                        voted = state.voteState.positiveVoteButtonState?.isVoted ?: false,
                    )
                },

            )
        }
    } else {
        EntryItem(
            modifier = modifier,
            state = state,
            onProfileClick = { actions.onProfileClicked(it) },
            onVoteUpClick = {
                actions.onEntryVoteUpClicked(
                    entryId = state.id,
                    voted = state.voteState.positiveVoteButtonState?.isVoted ?: false,
                )
            }
        )
    }
}

@Composable
fun LinkItemRenderer(
    modifier: Modifier,
    state: LinkItemState,
    actions: ResourceItemActions,
) {
    LinkItem(
        modifier = modifier,
        state = state,
        onLinkClick = { actions.onLinkClicked(state.id) },
        onVoteClick = { actions.onLinkVoteClicked(state.id, state.countState.isVoted) },
        onAuthorClick = { actions.onProfileClicked(it) },
        onTagClick = { actions.onTagClicked(it) },
    )
}

@Composable
fun EntryCommentItemRenderer(
    modifier: Modifier,
    state: EntryCommentItemState,
    actions: ResourceItemActions,
) {
}

@Composable
fun HitItemRenderer(
    modifier: Modifier,
    state: HitItemState,
    actions: ResourceItemActions,
) {
    HitItem(
        modifier = modifier,
        state = state,
        onItemClick = { actions.onLinkClicked(state.id) },
        onVoteClick = { actions.onLinkVoteClicked(state.id, state.countState.isVoted) }
    )
}

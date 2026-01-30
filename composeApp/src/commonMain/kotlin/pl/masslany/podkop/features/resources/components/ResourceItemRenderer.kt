package pl.masslany.podkop.features.resources.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pl.masslany.podkop.features.links.hits.HitItem
import pl.masslany.podkop.features.links.hits.models.HitItemState
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.resources.models.ResourceItemState
import pl.masslany.podkop.features.resources.models.comment.CommentItemState
import pl.masslany.podkop.features.resources.models.entry.EntryItemState
import pl.masslany.podkop.features.resources.models.link.LinkItemState

@Composable
fun ResourceItemRenderer(
    modifier: Modifier = Modifier,
    state: ResourceItemState,
    actions: ResourceItemActions,
) {
    when (state) {
        is EntryItemState -> EntryItemRenderer(modifier, state, actions)
        is LinkItemState -> LinkItemRenderer(modifier, state, actions)
        is CommentItemState -> CommentItemRenderer(modifier, state, actions)
        is HitItemState -> HitItemRenderer(modifier, state, actions)
    }
}

@Composable
fun EntryItemRenderer(
    modifier: Modifier,
    state: EntryItemState,
    actions: ResourceItemActions,
) {
    Text(text = state.text)
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
fun CommentItemRenderer(
    modifier: Modifier,
    state: CommentItemState,
    actions: ResourceItemActions,
) {
    Text(text = state.text)
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

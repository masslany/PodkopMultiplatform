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
    item: ResourceItemState,
    actions: ResourceItemActions,
) {
    when (item) {
        is EntryItemState -> EntryItemRenderer(modifier, item, actions)
        is LinkItemState -> LinkItemRenderer(modifier, item, actions)
        is CommentItemState -> CommentItemRenderer(modifier, item, actions)
        is HitItemState -> HitItemRenderer(modifier, item, actions)
    }
}

@Composable
fun EntryItemRenderer(
    modifier: Modifier,
    item: EntryItemState,
    actions: ResourceItemActions,
) {
    Text(text = item.text)
}

@Composable
fun LinkItemRenderer(
    modifier: Modifier,
    item: LinkItemState,
    actions: ResourceItemActions,
) {
    Text(text = item.text)
}

@Composable
fun CommentItemRenderer(
    modifier: Modifier,
    item: CommentItemState,
    actions: ResourceItemActions,
) {
    Text(text = item.text)
}

@Composable
fun HitItemRenderer(
    modifier: Modifier,
    item: HitItemState,
    actions: ResourceItemActions,
) {
    HitItem(
        modifier = modifier,
        state = item,
        onItemClick = { actions.onLinkClicked(item.id) },
        onVoteClick = { actions.onLinkVoteClicked(item.id, item.countState.isVoted) }
    )
}

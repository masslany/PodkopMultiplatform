package pl.masslany.podkop.features.resources.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.resources.models.ResourceItemState
import pl.masslany.podkop.features.resources.models.comment.CommentItemState
import pl.masslany.podkop.features.resources.models.entry.EntryItemState
import pl.masslany.podkop.features.resources.models.link.LinkItemState

@Composable
fun ResourceItemRenderer(
    item: ResourceItemState,
    actions: ResourceItemActions,
) {
    when (item) {
        is EntryItemState -> EntryItemRenderer(item, actions)
        is LinkItemState -> LinkItemRenderer(item, actions)
        is CommentItemState -> CommentItemRenderer(item, actions)
    }
}

@Composable
fun EntryItemRenderer(
    item: EntryItemState,
    actions: ResourceItemActions,
) {
    Text(text = item.text)
}

@Composable
fun LinkItemRenderer(
    item: LinkItemState,
    actions: ResourceItemActions,
) {
    Text(text = item.text)
}

@Composable
fun CommentItemRenderer(
    item: CommentItemState,
    actions: ResourceItemActions,
) {
    Text(text = item.text)
}
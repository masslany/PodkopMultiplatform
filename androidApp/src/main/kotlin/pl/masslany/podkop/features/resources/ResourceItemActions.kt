package pl.masslany.podkop.features.resources

import androidx.compose.runtime.Stable
import pl.masslany.podkop.features.resources.models.CommonActions
import pl.masslany.podkop.features.resources.models.entry.EntryActions
import pl.masslany.podkop.features.resources.models.entrycomment.EntryCommentActions
import pl.masslany.podkop.features.resources.models.link.LinkActions
import pl.masslany.podkop.features.resources.models.linkcomment.LinkCommentActions

@Stable
interface ResourceItemActions :
    EntryActions,
    LinkActions,
    LinkCommentActions,
    EntryCommentActions,
    CommonActions

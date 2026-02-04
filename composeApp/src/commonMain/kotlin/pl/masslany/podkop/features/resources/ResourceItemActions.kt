package pl.masslany.podkop.features.resources

import androidx.compose.runtime.Stable
import pl.masslany.podkop.features.resources.models.CommonActions
import pl.masslany.podkop.features.resources.models.entry.EntryActions
import pl.masslany.podkop.features.resources.models.entrycomment.EntryCommentActions
import pl.masslany.podkop.features.resources.models.link.LinkActions

@Stable
interface ResourceItemActions :
    EntryActions,
    LinkActions,
    EntryCommentActions,
    CommonActions

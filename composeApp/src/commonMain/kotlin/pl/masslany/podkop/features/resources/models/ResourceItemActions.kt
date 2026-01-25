package pl.masslany.podkop.features.resources.models

import androidx.compose.runtime.Stable
import pl.masslany.podkop.features.resources.models.comment.CommentActions
import pl.masslany.podkop.features.resources.models.entry.EntryActions
import pl.masslany.podkop.features.resources.models.link.LinkActions

@Stable
interface ResourceItemActions : EntryActions, LinkActions, CommentActions
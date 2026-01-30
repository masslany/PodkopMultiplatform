package pl.masslany.podkop.features.resources.models.link

import kotlinx.collections.immutable.ImmutableList
import pl.masslany.podkop.common.models.AuthorState
import pl.masslany.podkop.common.models.CountState
import pl.masslany.podkop.common.models.DescriptionState
import pl.masslany.podkop.common.models.PublishedTimeType
import pl.masslany.podkop.common.models.TagItem
import pl.masslany.podkop.common.models.TitleState
import pl.masslany.podkop.features.resources.models.ResourceItemState

data class LinkItemState(
    override val id: Int,
    val titleState: TitleState?,
    val descriptionState: DescriptionState?,
    val countState: CountState,
    val authorState: AuthorState?,
    val source: String?,
    val publishedTimeType: PublishedTimeType?,
    val commentCount: Int,
    val imageUrl: String,
    val tags: ImmutableList<TagItem>,
) : ResourceItemState

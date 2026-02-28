package pl.masslany.podkop.features.tag

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import pl.masslany.podkop.features.resources.models.ResourceItemState
import pl.masslany.podkop.features.resources.models.entry.EntryItemState

internal fun List<ResourceItemState>.toTagGalleryItems(): ImmutableList<TagGalleryItemState> = this
    .mapIndexedNotNull { index, resource ->
        val entry = resource as? EntryItemState ?: return@mapIndexedNotNull null
        val imageState = entry.embedImageState
            ?.takeIf { it.url.isNotBlank() }
            ?: return@mapIndexedNotNull null

        TagGalleryItemState(
            resourceIndex = index,
            entryId = entry.id,
            imageUrl = imageState.url,
            aspectRatio = imageState.aspectRatio,
            isAdult = imageState.isAdult,
            isGif = imageState.isGif,
        )
    }
    .toImmutableList()

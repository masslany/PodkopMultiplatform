package pl.masslany.podkop.business.tags.data.main.mapper

import pl.masslany.podkop.business.tags.data.network.models.TagsAutoCompleteResponseDto
import pl.masslany.podkop.business.tags.domain.models.TagsAutoComplete
import pl.masslany.podkop.business.tags.domain.models.TagsAutoCompleteItem

fun TagsAutoCompleteResponseDto.toTagsAutoComplete(): TagsAutoComplete {
    return TagsAutoComplete(
        tags =
            data.map {
                TagsAutoCompleteItem(
                    name = it.name,
                    observedQuantity = it.observedQuantity,
                )
            },
    )
}

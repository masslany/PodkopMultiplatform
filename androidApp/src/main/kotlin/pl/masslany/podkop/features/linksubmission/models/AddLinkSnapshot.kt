package pl.masslany.podkop.features.linksubmission.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class AddLinkSnapshot(
    val currentUrl: String,
    val title: String,
    val description: String,
    val tags: ImmutableList<String>,
    val tagInput: String,
    val selectedSuggestedImageIndex: Int?,
    val adult: Boolean,
    val photoKey: String?,
    val photoUrl: String?,
) {
    companion object {
        val empty = AddLinkSnapshot(
            currentUrl = "",
            title = "",
            description = "",
            tags = persistentListOf(),
            tagInput = "",
            selectedSuggestedImageIndex = null,
            adult = false,
            photoKey = null,
            photoUrl = null,
        )
    }
}

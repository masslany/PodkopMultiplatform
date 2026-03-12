package pl.masslany.podkop.business.links.domain.models

data class LinkDraftDetails(
    val key: String,
    val url: String,
    val title: String?,
    val description: String?,
    val tags: List<String>,
    val adult: Boolean,
    val photoKey: String?,
    val photoUrl: String?,
    val suggestedImages: List<String>,
    val selectedImageIndex: Int?,
)

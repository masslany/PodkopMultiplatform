package pl.masslany.podkop.business.links.domain.models.request

data class UpdateLinkDraft(
    val title: String,
    val description: String?,
    val tags: List<String>,
    val photoKey: String?,
    val adult: Boolean,
    val selectedImageIndex: Int?,
)

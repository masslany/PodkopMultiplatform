package pl.masslany.podkop.business.common.domain.models.common

data class Photo(
    val height: Int,
    val key: String,
    val label: String,
    val mimeType: String,
    val size: Int,
    val url: String,
    val width: Int,
)

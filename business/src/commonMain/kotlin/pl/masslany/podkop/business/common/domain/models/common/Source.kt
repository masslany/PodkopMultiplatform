package pl.masslany.podkop.business.common.domain.models.common

data class Source(
    val label: String,
    val type: String,
    val typeId: Int,
    val url: String,
)

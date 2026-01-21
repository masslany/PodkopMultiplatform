package pl.masslany.podkop.business.common.domain.models.common

data class Comments(
    val count: Int,
    val hot: Boolean,
    val items: List<Comment>,
)

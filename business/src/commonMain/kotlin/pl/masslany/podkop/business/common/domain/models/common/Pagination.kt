package pl.masslany.podkop.business.common.domain.models.common

data class Pagination(
    val perPage: Int,
    val total: Int,
    val next: String,
    val prev: String,
)

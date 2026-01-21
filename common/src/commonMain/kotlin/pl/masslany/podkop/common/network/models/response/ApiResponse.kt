package pl.masslany.podkop.common.network.models.response

data class ApiResponse<T>(
    val content: T,
)

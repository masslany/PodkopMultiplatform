package pl.masslany.podkop.features.links

data class LinksScreenState(
    val isLoading: Boolean,
    val isUpcoming: Boolean,
) {
    companion object {
        val initial = LinksScreenState(
            isLoading = false,
            isUpcoming = false,
        )
    }

    fun showLoading() = this.copy(
        isLoading = true,
    )

    fun hideLoading() = this.copy(
        isLoading = false,
    )
}

package pl.masslany.podkop.features.linkdetails

data class LinkDetailsScreenState(
    val id: Int,
) {
    companion object Companion {
        val initial = LinkDetailsScreenState(
            id = -1,
        )
    }
}

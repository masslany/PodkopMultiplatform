package pl.masslany.podkop.features.more

data class MoreScreenState(val isLoading: Boolean) {
    companion object {
        val initial = MoreScreenState(
            isLoading = true,
        )
    }
}

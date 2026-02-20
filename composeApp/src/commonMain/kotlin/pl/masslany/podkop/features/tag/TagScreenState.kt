package pl.masslany.podkop.features.tag

data class TagScreenState(val tag: String) {
    companion object Companion {
        val initial = TagScreenState(tag = "")
    }
}

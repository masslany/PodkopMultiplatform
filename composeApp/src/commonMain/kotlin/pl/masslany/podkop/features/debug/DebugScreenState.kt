package pl.masslany.podkop.features.debug

data class DebugScreenState(
    val entryIdInput: String,
    val isEntryIdInvalid: Boolean,
    val linkIdInput: String,
    val isLinkIdInvalid: Boolean,
) {
    companion object {
        val initial = DebugScreenState(
            entryIdInput = "",
            isEntryIdInvalid = false,
            linkIdInput = "",
            isLinkIdInvalid = false,
        )
    }
}

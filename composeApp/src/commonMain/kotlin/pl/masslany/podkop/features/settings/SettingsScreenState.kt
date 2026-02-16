package pl.masslany.podkop.features.settings

data class SettingsScreenState(val autoplayGifs: Boolean?, val showDebugTools: Boolean) {
    companion object {
        val initial = SettingsScreenState(
            autoplayGifs = null,
            showDebugTools = false,
        )
    }
}

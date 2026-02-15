package pl.masslany.podkop.features.settings

data class SettingsScreenState(val autoplayGifs: Boolean?) {
    companion object {
        val initial = SettingsScreenState(
            autoplayGifs = null,
        )
    }
}

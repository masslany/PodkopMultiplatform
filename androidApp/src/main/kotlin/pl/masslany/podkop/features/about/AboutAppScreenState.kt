package pl.masslany.podkop.features.about

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class AboutAppScreenState(val appVersion: String, val libraries: ImmutableList<OpenSourceLibraryNotice>) {
    companion object {
        val initial = AboutAppScreenState(
            appVersion = "",
            libraries = persistentListOf(),
        )
    }
}

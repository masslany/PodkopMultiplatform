package pl.masslany.podkop.features.more

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.features.more.models.MoreSectionState
import pl.masslany.podkop.features.profile.models.ProfileHeaderState

data class MoreScreenState(
    val isLoading: Boolean,
    val isLoggedIn: Boolean,
    val profileHeader: ProfileHeaderState?,
    val sections: ImmutableList<MoreSectionState>,
) {
    companion object {
        val initial = MoreScreenState(
            isLoading = true,
            isLoggedIn = false,
            profileHeader = null,
            sections = persistentListOf(),
        )
    }
}

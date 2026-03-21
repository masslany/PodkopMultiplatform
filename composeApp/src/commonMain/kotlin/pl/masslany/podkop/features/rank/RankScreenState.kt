package pl.masslany.podkop.features.rank

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.avatar.GenderIndicatorType
import pl.masslany.podkop.features.profile.models.MemberSinceState

data class RankScreenState(
    val isLoading: Boolean,
    val isError: Boolean,
    val isRefreshing: Boolean,
    val items: ImmutableList<RankUserItemState>,
    val isPaginating: Boolean,
) {
    companion object {
        val initial = RankScreenState(
            isLoading = true,
            isError = false,
            isRefreshing = false,
            items = persistentListOf(),
            isPaginating = false,
        )
    }

    fun updateLoading(isLoading: Boolean) = copy(isLoading = isLoading)

    fun updateError(isError: Boolean) = copy(isError = isError)

    fun updateRefreshing(isRefreshing: Boolean) = copy(isRefreshing = isRefreshing)
}

data class RankUserItemState(
    val username: String,
    val avatarUrl: String,
    val genderIndicatorType: GenderIndicatorType,
    val nameColorType: NameColorType,
    val memberSinceState: MemberSinceState,
    val position: Int,
    val trend: Int,
    val actionsCount: Int,
    val linksCount: Int,
    val entriesCount: Int,
    val followersCount: Int,
)

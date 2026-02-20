package pl.masslany.podkop.features.profile

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.avatar.GenderIndicatorType
import pl.masslany.podkop.features.resources.models.ResourceItemState

data class ProfileScreenState(
    val isLoading: Boolean,
    val content: ProfileContentState,
    val resources: ImmutableList<ResourceItemState>,
) {
    companion object Companion {
        val initial = ProfileScreenState(
            isLoading = true,
            content = ProfileContentState.Empty,
            resources = persistentListOf(),
        )
    }
}

sealed interface ProfileContentState {
    data object Empty : ProfileContentState

    data object LoggedOut : ProfileContentState

    data class Loaded(
        val isCurrentUser: Boolean,
        val header: ProfileHeaderState,
        val summary: ImmutableList<ProfileSummaryItem>,
    ) : ProfileContentState

    data object Error : ProfileContentState
}

data class ProfileHeaderState(
    val username: String,
    val avatarUrl: String,
    val backgroundUrl: String,
    val genderIndicatorType: GenderIndicatorType,
    val nameColorType: NameColorType,
    val memberSinceState: MemberSinceState,
)

sealed class MemberSinceState {
    data class Days(val days: Int) : MemberSinceState()

    data class Months(val months: Int) : MemberSinceState()

    data class Years(val years: Int) : MemberSinceState()

    data class YearsAndMonths(val years: Int, val months: Int) : MemberSinceState()

    data object Unknown : MemberSinceState()
}

sealed class ProfileSummaryItem(open val value: Int) {
    data class Actions(override val value: Int) : ProfileSummaryItem(value)

    data class Entries(override val value: Int) : ProfileSummaryItem(value)

    data class Links(override val value: Int) : ProfileSummaryItem(value)

    data class Followers(override val value: Int) : ProfileSummaryItem(value)

    data class FollowingTags(override val value: Int) : ProfileSummaryItem(value)

    data class FollowingUsers(override val value: Int) : ProfileSummaryItem(value)
}

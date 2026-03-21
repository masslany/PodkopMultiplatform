package pl.masslany.podkop.features.rank.preview

import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.avatar.GenderIndicatorType
import pl.masslany.podkop.features.profile.models.MemberSinceState
import pl.masslany.podkop.features.rank.RankScreenState
import pl.masslany.podkop.features.rank.RankUserItemState

object RankPreviewFixtures {
    val leadingUser = RankUserItemState(
        username = "user1",
        avatarUrl = "https://picsum.photos/seed/rank-leading/96/96",
        genderIndicatorType = GenderIndicatorType.Male,
        nameColorType = NameColorType.Orange,
        memberSinceState = MemberSinceState.YearsAndMonths(years = 11, months = 8),
        position = 1,
        trend = 0,
        actionsCount = 175794,
        linksCount = 12668,
        entriesCount = 163126,
        followersCount = 429,
    )

    val fallingUser = RankUserItemState(
        username = "user2",
        avatarUrl = "",
        genderIndicatorType = GenderIndicatorType.Male,
        nameColorType = NameColorType.Burgundy,
        memberSinceState = MemberSinceState.Years(years = 6),
        position = 5,
        trend = -1,
        actionsCount = 1601,
        linksCount = 1079,
        entriesCount = 522,
        followersCount = 6,
    )

    val risingUser = RankUserItemState(
        username = "user3",
        avatarUrl = "",
        genderIndicatorType = GenderIndicatorType.Male,
        nameColorType = NameColorType.Burgundy,
        memberSinceState = MemberSinceState.YearsAndMonths(years = 7, months = 6),
        position = 5,
        trend = 1,
        actionsCount = 13513,
        linksCount = 12272,
        entriesCount = 1241,
        followersCount = 26,
    )

    fun contentState(isPaginating: Boolean = false): RankScreenState = RankScreenState(
        isLoading = false,
        isError = false,
        isRefreshing = false,
        items = persistentListOf(
            leadingUser,
            fallingUser,
            risingUser,
        ),
        isPaginating = isPaginating,
    )

    fun refreshingState(): RankScreenState = contentState().copy(isRefreshing = true)

    fun errorState(): RankScreenState = RankScreenState.initial.copy(
        isLoading = false,
        isError = true,
    )
}

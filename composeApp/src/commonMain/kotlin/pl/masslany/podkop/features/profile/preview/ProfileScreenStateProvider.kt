package pl.masslany.podkop.features.profile.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.avatar.GenderIndicatorType
import pl.masslany.podkop.features.profile.ProfileScreenState
import pl.masslany.podkop.features.profile.models.MemberSinceState
import pl.masslany.podkop.features.profile.models.ProfileContentState
import pl.masslany.podkop.features.profile.models.ProfileHeaderState
import pl.masslany.podkop.features.profile.models.ProfileListContentState
import pl.masslany.podkop.features.profile.models.ProfileObservedTagItemState
import pl.masslany.podkop.features.profile.models.ProfileObservedUserItemState
import pl.masslany.podkop.features.profile.models.ProfileSubActionState
import pl.masslany.podkop.features.profile.models.ProfileSubActionType
import pl.masslany.podkop.features.profile.models.ProfileSummaryItem
import pl.masslany.podkop.features.profile.models.ProfileSummaryType
import pl.masslany.podkop.features.resources.preview.ResourceItemStateProvider

class ProfileScreenStateProvider : PreviewParameterProvider<ProfileScreenState> {
    private val resources = ResourceItemStateProvider().values.toList()

    override val values: Sequence<ProfileScreenState> = sequenceOf(
        ProfileScreenState.initial,
        ProfileScreenState(
            isLoading = false,
            isResourcesLoading = false,
            isPaginating = false,
            content = ProfileContentState.LoggedOut,
            listContent = ProfileListContentState.Empty,
        ),
        ProfileScreenState(
            isLoading = false,
            isResourcesLoading = false,
            isPaginating = true,
            content = ProfileContentState.Loaded(
                isCurrentUser = false,
                header = ProfileHeaderState(
                    username = "patryk",
                    avatarUrl = "https://picsum.photos/seed/profile-avatar/160/160",
                    backgroundUrl = "https://picsum.photos/seed/profile-bg/1200/600",
                    genderIndicatorType = GenderIndicatorType.Male,
                    nameColorType = NameColorType.Orange,
                    memberSinceState = MemberSinceState.YearsAndMonths(years = 6, months = 2),
                ),
                summary = persistentListOf(
                    ProfileSummaryItem.Links(128),
                    ProfileSummaryItem.Entries(42),
                    ProfileSummaryItem.Followers(900),
                    ProfileSummaryItem.Following(123),
                ),
                selectedSummaryType = ProfileSummaryType.Links,
                subActionState = ProfileSubActionState(
                    items = persistentListOf(
                        ProfileSubActionType.LinksAdded,
                        ProfileSubActionType.LinksPublished,
                        ProfileSubActionType.LinksCommented,
                    ),
                    selected = ProfileSubActionType.LinksPublished,
                    expanded = false,
                ),
            ),
            listContent = ProfileListContentState.Resources(
                items = persistentListOf(resources[0], resources[0], resources[1]),
            ),
        ),
        ProfileScreenState(
            isLoading = false,
            isResourcesLoading = false,
            isPaginating = false,
            content = ProfileContentState.Loaded(
                isCurrentUser = true,
                header = ProfileHeaderState(
                    username = "maria_dev",
                    avatarUrl = "https://picsum.photos/seed/profile-avatar2/160/160",
                    backgroundUrl = "https://picsum.photos/seed/profile-bg2/1200/600",
                    genderIndicatorType = GenderIndicatorType.Female,
                    nameColorType = NameColorType.Green,
                    memberSinceState = MemberSinceState.Months(9),
                ),
                summary = persistentListOf(
                    ProfileSummaryItem.Following(12),
                    ProfileSummaryItem.Followers(4),
                ),
                selectedSummaryType = ProfileSummaryType.Following,
                subActionState = ProfileSubActionState(
                    items = persistentListOf(ProfileSubActionType.FollowingUsers, ProfileSubActionType.FollowingTags),
                    selected = ProfileSubActionType.FollowingUsers,
                    expanded = false,
                ),
            ),
            listContent = ProfileListContentState.ObservedUsers(
                items = persistentListOf(
                    ProfileObservedUserItemState(
                        username = "alice",
                        avatarUrl = "https://picsum.photos/seed/alice/96/96",
                        genderIndicatorType = GenderIndicatorType.Female,
                        nameColorType = NameColorType.Orange,
                        online = true,
                        company = false,
                        verified = true,
                        status = "",
                    ),
                    ProfileObservedUserItemState(
                        username = "bob",
                        avatarUrl = "https://picsum.photos/seed/bob/96/96",
                        genderIndicatorType = GenderIndicatorType.Male,
                        nameColorType = NameColorType.Burgundy,
                        online = false,
                        company = true,
                        verified = false,
                        status = "",
                    ),
                ),
            ),
        ),
        ProfileScreenState(
            isLoading = false,
            isResourcesLoading = false,
            isPaginating = false,
            content = ProfileContentState.Loaded(
                isCurrentUser = true,
                header = ProfileHeaderState(
                    username = "maria_dev",
                    avatarUrl = "https://picsum.photos/seed/profile-avatar2/160/160",
                    backgroundUrl = "https://picsum.photos/seed/profile-bg2/1200/600",
                    genderIndicatorType = GenderIndicatorType.Female,
                    nameColorType = NameColorType.Green,
                    memberSinceState = MemberSinceState.Unknown,
                ),
                summary = persistentListOf(ProfileSummaryItem.Following(12)),
                selectedSummaryType = ProfileSummaryType.Following,
                subActionState = ProfileSubActionState(
                    items = persistentListOf(ProfileSubActionType.FollowingTags),
                    selected = ProfileSubActionType.FollowingTags,
                    expanded = false,
                ),
            ),
            listContent = ProfileListContentState.ObservedTags(
                items = persistentListOf(
                    ProfileObservedTagItemState(name = "compose", pinned = true),
                    ProfileObservedTagItemState(name = "kotlin", pinned = false),
                    ProfileObservedTagItemState(name = "multiplatform", pinned = false),
                ),
            ),
        ),
    )
}

package pl.masslany.podkop.features.profile.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.UserItemState
import pl.masslany.podkop.common.models.avatar.GenderIndicatorType
import pl.masslany.podkop.features.profile.ProfileScreenState
import pl.masslany.podkop.features.profile.models.MemberSinceState
import pl.masslany.podkop.features.profile.models.ProfileAchievementItemState
import pl.masslany.podkop.features.profile.models.ProfileAchievementsSectionState
import pl.masslany.podkop.features.profile.models.ProfileHeaderState
import pl.masslany.podkop.features.profile.models.ProfileListContentState
import pl.masslany.podkop.features.profile.models.ProfileNoteState
import pl.masslany.podkop.features.profile.models.ProfileObservedTagItemState
import pl.masslany.podkop.features.profile.models.ProfileSubActionState
import pl.masslany.podkop.features.profile.models.ProfileSubActionType
import pl.masslany.podkop.features.profile.models.ProfileSummaryItem
import pl.masslany.podkop.features.profile.models.ProfileSummaryType
import pl.masslany.podkop.features.resources.preview.ResourceItemStateProvider

class ProfileScreenStateProvider : PreviewParameterProvider<ProfileScreenState> {
    private val resources = ResourceItemStateProvider().values.toList()
    private val achievementsState = ProfileAchievementsSectionState(
        isLoading = false,
        isError = false,
        hasLoaded = true,
        items = persistentListOf(
            ProfileAchievementItemState(
                label = "Wykopowicz",
                slug = "wykopowicz-1",
                description = "Za zdobytą ilość linków na stronie głównej",
                iconUrl = "https://picsum.photos/seed/badge-1/120/80",
                iconMimeType = "image/png",
                colorHex = "111111",
                colorHexDark = "f5f5f5",
                level = 1,
                progress = 50,
                achievedAt = "2021-01-01 20:00:00",
            ),
            ProfileAchievementItemState(
                label = "Komentator",
                slug = "komentator-2",
                description = "Za aktywność w komentarzach",
                iconUrl = "https://picsum.photos/seed/badge-2/120/80",
                iconMimeType = "image/png",
                colorHex = "8b0000",
                colorHexDark = "ffd7d7",
                level = 2,
                progress = 90,
                achievedAt = "2022-06-12 09:30:00",
            ),
        ),
    )
    private val noteState = ProfileNoteState(
        content = "Dobry człowiek",
        savedContent = "Dobry człowiek",
        isLoading = false,
        isError = false,
        isSaving = false,
        hasLoaded = true,
    )

    override val values: Sequence<ProfileScreenState> = sequenceOf(
        ProfileScreenState.initial.copy(username = "patryk"),
        ProfileScreenState(
            username = "patryk",
            isLoading = false,
            isError = false,
            isResourcesLoading = false,
            isPaginating = true,
            isObserveActionLoading = false,
            isBlacklistActionLoading = false,
            isDetailsExpanded = true,
            header = ProfileHeaderState(
                username = "patryk",
                avatarUrl = "https://picsum.photos/seed/profile-avatar/160/160",
                rankPosition = 176,
                backgroundUrl = "https://picsum.photos/seed/profile-bg/1200/600",
                genderIndicatorType = GenderIndicatorType.Male,
                nameColorType = NameColorType.Orange,
                memberSinceState = MemberSinceState.YearsAndMonths(years = 6, months = 2),
                isLoggedIn = true,
                isOwnProfile = false,
                isObserved = true,
                isBlacklisted = false,
                canManageObservation = true,
                canSendPrivateMessage = true,
            ),
            noteState = noteState,
            achievementsState = achievementsState,
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
            listContent = ProfileListContentState.Resources(
                items = persistentListOf(resources[0], resources[0], resources[1]),
            ),
        ),
        ProfileScreenState(
            username = "maria_dev",
            isLoading = false,
            isError = false,
            isResourcesLoading = false,
            isPaginating = false,
            isObserveActionLoading = false,
            isBlacklistActionLoading = false,
            isDetailsExpanded = false,
            header = ProfileHeaderState(
                username = "maria_dev",
                avatarUrl = "https://picsum.photos/seed/profile-avatar2/160/160",
                rankPosition = 24,
                backgroundUrl = "https://picsum.photos/seed/profile-bg2/1200/600",
                genderIndicatorType = GenderIndicatorType.Female,
                nameColorType = NameColorType.Green,
                memberSinceState = MemberSinceState.Months(9),
                isLoggedIn = true,
                isOwnProfile = false,
                isObserved = false,
                isBlacklisted = true,
                canManageObservation = true,
                canSendPrivateMessage = true,
            ),
            noteState = ProfileNoteState.initial,
            achievementsState = ProfileAchievementsSectionState.initial,
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
            listContent = ProfileListContentState.ObservedUsers(
                items = persistentListOf(
                    UserItemState(
                        username = "alice",
                        avatarUrl = "https://picsum.photos/seed/alice/96/96",
                        genderIndicatorType = GenderIndicatorType.Female,
                        nameColorType = NameColorType.Orange,
                        online = true,
                        company = false,
                        verified = true,
                        status = "",
                    ),
                    UserItemState(
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
            username = "maria_dev",
            isLoading = false,
            isError = false,
            isResourcesLoading = false,
            isPaginating = false,
            isObserveActionLoading = false,
            isBlacklistActionLoading = false,
            isDetailsExpanded = false,
            header = ProfileHeaderState(
                username = "maria_dev",
                avatarUrl = "https://picsum.photos/seed/profile-avatar2/160/160",
                rankPosition = null,
                backgroundUrl = "https://picsum.photos/seed/profile-bg2/1200/600",
                genderIndicatorType = GenderIndicatorType.Female,
                nameColorType = NameColorType.Green,
                memberSinceState = MemberSinceState.Unknown,
                isLoggedIn = false,
                isOwnProfile = false,
                isObserved = false,
                isBlacklisted = false,
                canManageObservation = false,
                canSendPrivateMessage = false,
            ),
            noteState = ProfileNoteState.initial,
            achievementsState = ProfileAchievementsSectionState.initial,
            summary = persistentListOf(ProfileSummaryItem.Following(12)),
            selectedSummaryType = ProfileSummaryType.Following,
            subActionState = ProfileSubActionState(
                items = persistentListOf(ProfileSubActionType.FollowingTags),
                selected = ProfileSubActionType.FollowingTags,
                expanded = false,
            ),
            listContent = ProfileListContentState.ObservedTags(
                items = persistentListOf(
                    ProfileObservedTagItemState(name = "compose", pinned = true),
                    ProfileObservedTagItemState(name = "kotlin", pinned = false),
                    ProfileObservedTagItemState(name = "multiplatform", pinned = false),
                ),
            ),
        ),
        ProfileScreenState(
            username = "patryk",
            isLoading = false,
            isError = true,
            isResourcesLoading = false,
            isPaginating = false,
            isObserveActionLoading = false,
            isBlacklistActionLoading = false,
            isDetailsExpanded = false,
            header = null,
            noteState = ProfileNoteState.initial,
            achievementsState = ProfileAchievementsSectionState.initial,
            summary = persistentListOf(),
            selectedSummaryType = ProfileSummaryType.Actions,
            subActionState = ProfileSubActionState(
                items = persistentListOf(ProfileSubActionType.Actions),
                selected = ProfileSubActionType.Actions,
                expanded = false,
            ),
            listContent = ProfileListContentState.Empty,
        ),
    )
}

package pl.masslany.podkop.features.blacklists.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.avatar.AvatarState
import pl.masslany.podkop.common.models.avatar.AvatarType
import pl.masslany.podkop.common.models.avatar.GenderIndicatorType
import pl.masslany.podkop.features.blacklists.BlacklistsScreenState
import pl.masslany.podkop.features.blacklists.models.BlacklistCategoryState
import pl.masslany.podkop.features.blacklists.models.BlacklistCategoryType
import pl.masslany.podkop.features.blacklists.models.BlacklistEntryState
import pl.masslany.podkop.features.blacklists.models.BlacklistEntryState.BlacklistedTagItemState

class BlacklistsScreenStateProvider : PreviewParameterProvider<BlacklistsScreenState> {
    override val values: Sequence<BlacklistsScreenState>
        get() = sequenceOf(
            BlacklistsScreenState.initial.copy(
                categories = persistentListOf(
                    BlacklistCategoryState.initial(BlacklistCategoryType.Users).copy(
                        totalCount = 1,
                        isLoading = false,
                        items = persistentListOf(
                            BlacklistEntryState.BlacklistedUserItemState(
                                username = "user1",
                                avatarState = AvatarState(
                                    type = AvatarType.NetworkImage("https://picsum.photos/seed/blacklist-user/96/96"),
                                    genderIndicatorType = GenderIndicatorType.Male,
                                ),
                                nameColorType = NameColorType.Orange,
                            ),
                        ),
                    ),
                    BlacklistCategoryState.initial(BlacklistCategoryType.Tags).copy(
                        totalCount = 48,
                        isLoading = false,
                        items = persistentListOf(
                            BlacklistedTagItemState(name = "user1"),
                            BlacklistedTagItemState(name = "user2"),
                            BlacklistedTagItemState(name = "user3"),
                        ),
                    ),
                    BlacklistCategoryState.initial(BlacklistCategoryType.Domains).copy(
                        totalCount = 1,
                        isLoading = false,
                        items = persistentListOf(
                            BlacklistEntryState.BlacklistedDomainItemState(domain = "masslany.pl"),
                        ),
                    ),
                ),
            ),
            BlacklistsScreenState.initial.copy(
                selectedCategory = BlacklistCategoryType.Domains,
                categories = persistentListOf(
                    BlacklistCategoryState.initial(BlacklistCategoryType.Users).copy(
                        totalCount = 1,
                        isLoading = false,
                    ),
                    BlacklistCategoryState.initial(BlacklistCategoryType.Tags).copy(
                        totalCount = 48,
                        isLoading = false,
                    ),
                    BlacklistCategoryState.initial(BlacklistCategoryType.Domains).copy(
                        totalCount = 0,
                        isLoading = false,
                        items = persistentListOf(),
                    ),
                ),
            ),
        )
}

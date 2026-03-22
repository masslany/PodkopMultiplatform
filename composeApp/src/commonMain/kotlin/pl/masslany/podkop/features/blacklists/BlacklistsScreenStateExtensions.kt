package pl.masslany.podkop.features.blacklists

import pl.masslany.podkop.business.blacklists.domain.models.BlacklistedDomain
import pl.masslany.podkop.business.blacklists.domain.models.BlacklistedTag
import pl.masslany.podkop.business.blacklists.domain.models.BlacklistedUser
import pl.masslany.podkop.business.profile.domain.models.UserAutoCompleteItem
import pl.masslany.podkop.business.tags.domain.models.TagsAutoCompleteItem
import pl.masslany.podkop.common.models.avatar.AvatarState
import pl.masslany.podkop.common.models.avatar.AvatarType
import pl.masslany.podkop.common.models.avatar.toGenderIndicatorType
import pl.masslany.podkop.common.models.toNameColorType
import pl.masslany.podkop.features.blacklists.models.BlacklistEntryState
import pl.masslany.podkop.features.blacklists.models.BlacklistedTagSuggestionItemState
import pl.masslany.podkop.features.blacklists.models.BlacklistedUserSuggestionItemState

internal fun BlacklistedUser.toItemState(): BlacklistEntryState.BlacklistedUserItemState =
    BlacklistEntryState.BlacklistedUserItemState(
        username = username,
        avatarState = AvatarState(
            type = if (avatarUrl.isNotBlank()) {
                AvatarType.NetworkImage(avatarUrl)
            } else {
                AvatarType.NoAvatar
            },
            genderIndicatorType = gender.toGenderIndicatorType(),
        ),
        nameColorType = color.toNameColorType(),
    )

internal fun BlacklistedTag.toItemState(): BlacklistEntryState.BlacklistedTagItemState =
    BlacklistEntryState.BlacklistedTagItemState(name = name)

internal fun BlacklistedDomain.toItemState(): BlacklistEntryState.BlacklistedDomainItemState =
    BlacklistEntryState.BlacklistedDomainItemState(domain = domain)

internal fun UserAutoCompleteItem.toSuggestionItemState(): BlacklistedUserSuggestionItemState =
    BlacklistedUserSuggestionItemState(
        username = username,
        avatarState = AvatarState(
            type = if (avatarUrl.isNotBlank()) {
                AvatarType.NetworkImage(avatarUrl)
            } else {
                AvatarType.NoAvatar
            },
            genderIndicatorType = gender.toGenderIndicatorType(),
        ),
        nameColorType = color.toNameColorType(),
    )

internal fun TagsAutoCompleteItem.toSuggestionItemState(): BlacklistedTagSuggestionItemState =
    BlacklistedTagSuggestionItemState(
        name = name,
        followers = observedQuantity,
    )

package pl.masslany.podkop.features.blacklists.models

import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.avatar.AvatarState

sealed interface BlacklistEntryState {
    val key: String
    val category: BlacklistCategoryType
    val displayLabel: String

    data class BlacklistedUserItemState(
        val username: String,
        val avatarState: AvatarState,
        val nameColorType: NameColorType,
    ) : BlacklistEntryState {
        override val key: String = "user:$username"
        override val category: BlacklistCategoryType = BlacklistCategoryType.Users
        override val displayLabel: String = username
    }

    data class BlacklistedTagItemState(val name: String) : BlacklistEntryState {
        override val key: String = "tag:$name"
        override val category: BlacklistCategoryType = BlacklistCategoryType.Tags
        override val displayLabel: String = "#$name"
    }

    data class BlacklistedDomainItemState(val domain: String) : BlacklistEntryState {
        override val key: String = "domain:$domain"
        override val category: BlacklistCategoryType = BlacklistCategoryType.Domains
        override val displayLabel: String = domain
    }
}

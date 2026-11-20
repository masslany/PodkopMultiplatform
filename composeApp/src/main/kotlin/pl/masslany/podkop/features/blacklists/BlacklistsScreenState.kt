package pl.masslany.podkop.features.blacklists

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.features.blacklists.models.BlacklistCategoryState
import pl.masslany.podkop.features.blacklists.models.BlacklistCategoryType

data class BlacklistsScreenState(
    val isRefreshing: Boolean,
    val selectedCategory: BlacklistCategoryType,
    val categories: ImmutableList<BlacklistCategoryState>,
) {
    val selectedCategoryState: BlacklistCategoryState
        get() = categories.first { it.type == selectedCategory }

    companion object {
        val initial = BlacklistsScreenState(
            isRefreshing = false,
            selectedCategory = BlacklistCategoryType.Users,
            categories = persistentListOf(
                BlacklistCategoryState.initial(BlacklistCategoryType.Users),
                BlacklistCategoryState.initial(BlacklistCategoryType.Tags),
                BlacklistCategoryState.initial(BlacklistCategoryType.Domains),
            ),
        )
    }
}

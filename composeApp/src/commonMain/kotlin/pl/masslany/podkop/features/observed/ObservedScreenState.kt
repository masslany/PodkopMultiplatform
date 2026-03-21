package pl.masslany.podkop.features.observed

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.observed.domain.models.request.ObservedType
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.DropdownMenuState

data class ObservedScreenState(
    val screenInstanceId: String,
    val isLoading: Boolean,
    val isError: Boolean,
    val isRefreshing: Boolean,
    val items: ImmutableList<ObservedListItemState>,
    val typeMenuState: DropdownMenuState,
    val selectedType: ObservedType,
    val isPaginating: Boolean,
) {
    companion object {
        val initial = ObservedScreenState(
            screenInstanceId = "",
            isLoading = true,
            isError = false,
            isRefreshing = false,
            items = persistentListOf(),
            typeMenuState = DropdownMenuState.initial,
            selectedType = ObservedType.All,
            isPaginating = false,
        )
    }

    fun updateTypeMenuExpanded(expanded: Boolean) = this.copy(
        typeMenuState = typeMenuState.copy(
            expanded = expanded,
        ),
    )

    fun updateTypeMenuSelected(
        type: DropdownMenuItemType,
        observedType: ObservedType,
    ) = this.copy(
        selectedType = observedType,
        typeMenuState = typeMenuState.copy(
            expanded = false,
            selected = type,
        ),
    )

    fun updateLoading(isLoading: Boolean) = this.copy(
        isLoading = isLoading,
    )

    fun updateError(isError: Boolean) = this.copy(
        isError = isError,
    )

    fun updateRefreshing(isRefreshing: Boolean) = this.copy(
        isRefreshing = isRefreshing,
    )
}

internal fun Resource.toObservedItemKey(id: Int): String = when (this) {
    Resource.Entry -> "entry:$id"
    Resource.Link -> "link:$id"
    Resource.EntryComment -> "entry_comment:$id"
    Resource.LinkComment -> "link_comment:$id"
    Resource.Unknown -> "unknown:$id"
}

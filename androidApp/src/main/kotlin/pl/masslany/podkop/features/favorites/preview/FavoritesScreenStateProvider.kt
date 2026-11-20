package pl.masslany.podkop.features.favorites.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.features.favorites.FavoritesScreenState
import pl.masslany.podkop.features.resources.preview.ResourceItemStateProvider

class FavoritesScreenStateProvider : PreviewParameterProvider<FavoritesScreenState> {
    private val items = ResourceItemStateProvider().values.toList()

    override val values: Sequence<FavoritesScreenState> = sequenceOf(
        FavoritesScreenState.initial.copy(
            screenInstanceId = "preview-content-everything",
            isLoading = false,
            isError = false,
            resources = persistentListOf(items[0], items[1], items.last()),
            sortMenuState = favoritesSortMenu(selected = DropdownMenuItemType.Newest),
            typeMenuState = favoritesTypeMenu(selected = DropdownMenuItemType.Everything),
            isPaginating = false,
        ),
        FavoritesScreenState.initial.copy(
            screenInstanceId = "preview-empty-links",
            isLoading = false,
            isError = false,
            resources = persistentListOf(),
            sortMenuState = favoritesSortMenu(selected = DropdownMenuItemType.Oldest),
            typeMenuState = favoritesTypeMenu(selected = DropdownMenuItemType.Links),
            isPaginating = false,
        ),
        FavoritesScreenState.initial.copy(
            screenInstanceId = "preview-error",
            isLoading = false,
            isError = true,
            resources = persistentListOf(),
            sortMenuState = favoritesSortMenu(selected = DropdownMenuItemType.Newest),
            typeMenuState = favoritesTypeMenu(selected = DropdownMenuItemType.Everything),
            isPaginating = false,
        ),
    )
}

private fun favoritesSortMenu(selected: DropdownMenuItemType) = DropdownMenuState(
    items = persistentListOf(
        DropdownMenuItemType.Newest,
        DropdownMenuItemType.Oldest,
    ),
    selected = selected,
    expanded = false,
)

private fun favoritesTypeMenu(selected: DropdownMenuItemType) = DropdownMenuState(
    items = persistentListOf(
        DropdownMenuItemType.Everything,
        DropdownMenuItemType.Links,
        DropdownMenuItemType.Entries,
        DropdownMenuItemType.LinkComments,
        DropdownMenuItemType.EntryComments,
    ),
    selected = selected,
    expanded = false,
)

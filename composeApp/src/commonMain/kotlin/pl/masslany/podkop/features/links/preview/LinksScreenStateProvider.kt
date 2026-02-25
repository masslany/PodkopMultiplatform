package pl.masslany.podkop.features.links.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.features.links.LinksScreenState
import pl.masslany.podkop.features.resources.preview.ResourceItemStateProvider

class LinksScreenStateProvider : PreviewParameterProvider<LinksScreenState> {
    private val items = ResourceItemStateProvider().values.toList()

    override val values: Sequence<LinksScreenState> = sequenceOf(
        LinksScreenState.initial.copy(
            screenInstanceId = "preview-loading",
            isLoading = true,
            isUpcoming = false,
        ),
        LinksScreenState.initial.copy(
            screenInstanceId = "preview-error",
            isLoading = false,
            isError = true,
            isUpcoming = false,
        ),
        LinksScreenState.initial.copy(
            screenInstanceId = "preview-content",
            isLoading = false,
            isError = false,
            isRefreshing = false,
            isUpcoming = false,
            hits = persistentListOf(items.last()), // hit item
            links = persistentListOf(items[0], items[1], items[0]),
            sortMenuState = DropdownMenuState(
                items = persistentListOf(
                    DropdownMenuItemType.Active,
                    DropdownMenuItemType.Newest,
                    DropdownMenuItemType.Digged,
                ),
                selected = DropdownMenuItemType.Active,
                expanded = false,
            ),
            isPaginating = false,
        ),
        LinksScreenState.initial.copy(
            screenInstanceId = "preview-upcoming",
            isLoading = false,
            isError = false,
            isUpcoming = true,
            hits = persistentListOf(),
            links = persistentListOf(items[0], items[0], items[1]),
            sortMenuState = DropdownMenuState(
                items = persistentListOf(DropdownMenuItemType.Active, DropdownMenuItemType.Newest),
                selected = DropdownMenuItemType.Newest,
                expanded = false,
            ),
            isPaginating = true,
        ),
    )
}

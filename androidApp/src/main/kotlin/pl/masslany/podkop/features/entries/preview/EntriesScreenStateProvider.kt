package pl.masslany.podkop.features.entries.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.features.entries.EntriesScreenState
import pl.masslany.podkop.features.resources.preview.ResourceItemStateProvider

class EntriesScreenStateProvider : PreviewParameterProvider<EntriesScreenState> {
    private val items = ResourceItemStateProvider().values.toList()

    override val values: Sequence<EntriesScreenState> = sequenceOf(
        EntriesScreenState.initial.copy(
            screenInstanceId = "preview-loading",
            isLoading = true,
        ),
        EntriesScreenState.initial.copy(
            screenInstanceId = "preview-error",
            isLoading = false,
            isError = true,
        ),
        EntriesScreenState.initial.copy(
            screenInstanceId = "preview-content",
            isLoading = false,
            entries = persistentListOf(items[1], items[1], items[1]),
            sortMenuState = DropdownMenuState(
                items = persistentListOf(
                    DropdownMenuItemType.Active,
                    DropdownMenuItemType.Newest,
                    DropdownMenuItemType.Hot,
                ),
                selected = DropdownMenuItemType.Hot,
                expanded = false,
            ),
            hotSortMenuState = DropdownMenuState(
                items = persistentListOf(
                    DropdownMenuItemType.TwoHours,
                    DropdownMenuItemType.SixHours,
                    DropdownMenuItemType.TwelveHours,
                ),
                selected = DropdownMenuItemType.SixHours,
                expanded = false,
            ),
            isPaginating = false,
        ),
        EntriesScreenState.initial.copy(
            screenInstanceId = "preview-paginating",
            isLoading = false,
            entries = persistentListOf(items[1], items[1], items[1]),
            sortMenuState = DropdownMenuState.initial,
            hotSortMenuState = null,
            isPaginating = true,
        ),
        EntriesScreenState.initial.copy(
            screenInstanceId = "preview-stale-refresh",
            isLoading = false,
            isRefreshPromptVisible = true,
            entries = persistentListOf(items[1], items[1], items[1]),
            sortMenuState = DropdownMenuState(
                items = persistentListOf(
                    DropdownMenuItemType.Active,
                    DropdownMenuItemType.Newest,
                    DropdownMenuItemType.Hot,
                ),
                selected = DropdownMenuItemType.Hot,
                expanded = false,
            ),
            hotSortMenuState = DropdownMenuState(
                items = persistentListOf(
                    DropdownMenuItemType.TwoHours,
                    DropdownMenuItemType.SixHours,
                    DropdownMenuItemType.TwelveHours,
                ),
                selected = DropdownMenuItemType.SixHours,
                expanded = false,
            ),
            isPaginating = false,
        ),
    )
}

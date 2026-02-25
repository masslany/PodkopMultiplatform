package pl.masslany.podkop.features.entrydetails.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.features.entrydetails.EntryDetailsScreenState
import pl.masslany.podkop.features.resources.preview.ResourceItemStateProvider

class EntryDetailsScreenStateProvider : PreviewParameterProvider<EntryDetailsScreenState> {
    private val items = ResourceItemStateProvider().values.toList()

    override val values: Sequence<EntryDetailsScreenState> = sequenceOf(
        EntryDetailsScreenState.initial.copy(isLoading = true),
        EntryDetailsScreenState.initial.copy(isLoading = false, isError = true),
        EntryDetailsScreenState.initial.copy(
            isLoading = false,
            isError = false,
            entry = items[1],
            comments = persistentListOf(items[3], items[3], items[3]),
            isPaginating = false,
        ),
        EntryDetailsScreenState.initial.copy(
            isLoading = false,
            isError = false,
            isCommentsError = true,
            entry = items[1],
            comments = persistentListOf(),
            isPaginating = false,
        ),
    )
}

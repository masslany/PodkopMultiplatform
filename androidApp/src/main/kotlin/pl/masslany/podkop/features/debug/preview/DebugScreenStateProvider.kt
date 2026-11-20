package pl.masslany.podkop.features.debug.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import pl.masslany.podkop.features.debug.DebugScreenState

class DebugScreenStateProvider : PreviewParameterProvider<DebugScreenState> {
    override val values: Sequence<DebugScreenState> = sequenceOf(
        DebugScreenState.initial,
        DebugScreenState(
            entryIdInput = "12345",
            isEntryIdInvalid = false,
            linkIdInput = "54321",
            isLinkIdInvalid = false,
        ),
        DebugScreenState(
            entryIdInput = "abc",
            isEntryIdInvalid = true,
            linkIdInput = "xyz",
            isLinkIdInvalid = true,
        ),
    )
}

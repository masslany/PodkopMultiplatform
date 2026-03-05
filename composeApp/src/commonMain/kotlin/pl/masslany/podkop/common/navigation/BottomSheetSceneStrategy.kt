package pl.masslany.podkop.common.navigation

import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import pl.masslany.podkop.common.navigation.BottomSheetSceneStrategy.Companion.bottomSheet

/** An [OverlayScene] that renders an [entry] within a [ModalBottomSheet]. */
@OptIn(ExperimentalMaterial3Api::class)
internal class BottomSheetScene<T : Any>(
    override val key: T,
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
    private val entry: NavEntry<T>,
    private val modalBottomSheetProperties: ModalBottomSheetProperties,
    private val skipPartiallyExpanded: Boolean,
    private val sheetGesturesEnabled: Boolean,
    private val hideDragHandle: Boolean,
    private val onBack: () -> Unit,
) : OverlayScene<T> {

    override val entries: List<NavEntry<T>> = listOf(entry)

    override val content: @Composable (() -> Unit) = {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = skipPartiallyExpanded,
        )
        ModalBottomSheet(
            onDismissRequest = onBack,
            sheetState = sheetState,
            sheetGesturesEnabled = sheetGesturesEnabled,
            dragHandle = if (hideDragHandle) {
                null
            } else {
                @Composable { BottomSheetDefaults.DragHandle() }
            },
            properties = modalBottomSheetProperties,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            CompositionLocalProvider(LocalBottomSheetState provides sheetState) {
                entry.Content()
            }
        }
    }
}

/**
 * A [SceneStrategy] that displays entries that have added [bottomSheet] to their [NavEntry.metadata]
 * within a [ModalBottomSheet] instance.
 *
 * This strategy should always be added before any non-overlay scene strategies.
 */
@OptIn(ExperimentalMaterial3Api::class)
class BottomSheetSceneStrategy<T : Any> : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val lastEntry = entries.lastOrNull()
        val bottomSheetProperties = lastEntry?.metadata?.get(BOTTOM_SHEET_KEY) as? ModalBottomSheetProperties
        val skipPartiallyExpanded =
            lastEntry?.metadata?.get(SKIP_PARTIALLY_EXPANDED_KEY) as? Boolean ?: false
        val sheetGesturesEnabled = lastEntry?.metadata?.get(SHEET_GESTURES_ENABLED_KEY) as? Boolean ?: true
        val hideDragHandle = lastEntry?.metadata?.get(HIDE_DRAG_HANDLE_KEY) as? Boolean ?: false

        return bottomSheetProperties?.let { properties ->
            @Suppress("UNCHECKED_CAST")
            BottomSheetScene(
                key = lastEntry.contentKey as T,
                previousEntries = entries.dropLast(1),
                overlaidEntries = entries.dropLast(1),
                entry = lastEntry,
                modalBottomSheetProperties = properties,
                skipPartiallyExpanded = skipPartiallyExpanded,
                sheetGesturesEnabled = sheetGesturesEnabled,
                hideDragHandle = hideDragHandle,
                onBack = onBack,
            )
        }
    }

    companion object {
        /**
         * Function to be called on the [NavEntry.metadata] to mark this entry as something that
         * should be displayed within a [ModalBottomSheet].
         *
         * @param modalBottomSheetProperties properties that should be passed to the containing
         * [ModalBottomSheet].
         */
        @OptIn(ExperimentalMaterial3Api::class)
        fun bottomSheet(
            modalBottomSheetProperties: ModalBottomSheetProperties = ModalBottomSheetProperties(),
            skipPartiallyExpanded: Boolean = false,
            sheetGesturesEnabled: Boolean = true,
            hideDragHandle: Boolean = false,
        ): Map<String, Any> = mapOf(
            BOTTOM_SHEET_KEY to modalBottomSheetProperties,
            SKIP_PARTIALLY_EXPANDED_KEY to skipPartiallyExpanded,
            SHEET_GESTURES_ENABLED_KEY to sheetGesturesEnabled,
            HIDE_DRAG_HANDLE_KEY to hideDragHandle,
        )

        internal const val BOTTOM_SHEET_KEY = "bottomsheet"
        internal const val SKIP_PARTIALLY_EXPANDED_KEY = "bottomsheet_skip_partially_expanded"
        internal const val SHEET_GESTURES_ENABLED_KEY = "bottomsheet_gestures_enabled"
        internal const val HIDE_DRAG_HANDLE_KEY = "bottomsheet_hide_drag_handle"
    }
}

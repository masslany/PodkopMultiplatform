package pl.masslany.podkop.common.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.rememberLifecycleOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.launch
import pl.masslany.podkop.common.navigation.AnimatedDialogSceneStrategy.Companion.dialog

/** Material 3 dialog enter motion: medium4 with the emphasized decelerate easing. */
private const val DialogEnterMillis = 400
private val DialogEnterEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

/** Material 3 dialog exit motion: short3 with the emphasized accelerate easing. */
private const val DialogExitMillis = 150
private val DialogExitEasing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

/** The scale a dialog grows from as it enters, as Material 3 dialogs do. */
private const val DialogEnterScale = 0.8f

/** How dark the scrim behind a dialog gets once it has entered. */
private const val DialogScrimAlpha = 0.56f

/**
 * Lets a dialog's content decide what dismissing it does. Tapping outside the dialog and Back
 * pop it by default; content that has to answer instead, such as with a result, sets
 * [onDismissRequest] through [AnimatedDialogDismissHandler].
 */
internal class AnimatedDialogDismissal {
    var onDismissRequest: (() -> Unit)? = null
}

internal val LocalAnimatedDialogDismissal = staticCompositionLocalOf<AnimatedDialogDismissal?> { null }

/** Runs [onDismissRequest] instead of popping when the surrounding animated dialog is dismissed. */
@Composable
internal fun AnimatedDialogDismissHandler(onDismissRequest: () -> Unit) {
    val dismissal = LocalAnimatedDialogDismissal.current ?: return
    val currentOnDismissRequest by rememberUpdatedState(onDismissRequest)
    DisposableEffect(dismissal) {
        dismissal.onDismissRequest = { currentOnDismissRequest() }
        onDispose { dismissal.onDismissRequest = null }
    }
}

/**
 * An [OverlayScene] that shows an [entry] as a dialog with Material 3 motion: it fades in while
 * growing from [DialogEnterScale], fades out when removed, and shrinks with a predictive Back
 * gesture like the screens below it.
 *
 * The dialog window covers the whole screen, so the scene draws the scrim itself and fades it
 * with the dialog; a window's own dim cannot animate. Of [dialogProperties], only
 * [DialogProperties.dismissOnBackPress] and [DialogProperties.dismissOnClickOutside] apply.
 */
internal class AnimatedDialogScene<T : Any>(
    override val key: Any,
    private val entry: NavEntry<T>,
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
    private val dialogProperties: DialogProperties,
    private val onBack: () -> Unit,
) : OverlayScene<T> {

    override val entries: List<NavEntry<T>> = listOf(entry)

    // NavDisplay keeps composing the instance it first saw for this key, and calls onRemove on
    // it, so the motion lives here rather than in the composition.
    private val visibility = Animatable(0f)
    private val enterScale = Animatable(DialogEnterScale)
    private val backProgress = Animatable(0f)
    private val dismissal = AnimatedDialogDismissal()
    private var isRemoving = false

    private fun dismiss() {
        if (isRemoving) return
        (dismissal.onDismissRequest ?: onBack)()
    }

    override val content: @Composable (() -> Unit) = {
        val lifecycleOwner = rememberLifecycleOwner()
        LaunchedEffect(Unit) {
            val enter = tween<Float>(durationMillis = DialogEnterMillis, easing = DialogEnterEasing)
            launch { visibility.animateTo(1f, enter) }
            launch { enterScale.animateTo(1f, enter) }
        }

        Dialog(
            onDismissRequest = ::dismiss,
            // The scene handles Back and outside taps itself, over a window that fills the screen.
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
            ),
        ) {
            SetDialogDestinationToEdgeToEdge()
            val dispatcherOwner = dialogNavigationEventDispatcherOwner()
                ?: LocalNavigationEventDispatcherOwner.current
            CompositionLocalProvider(
                LocalLifecycleOwner provides lifecycleOwner,
                LocalAnimatedDialogDismissal provides dismissal,
            ) {
                if (dispatcherOwner != null) {
                    CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides dispatcherOwner) {
                        PredictiveBackDismissal()
                    }
                }
                DialogLayout()
            }
        }
    }

    /** Shrinks the dialog as a Back gesture progresses, and dismisses it when the gesture ends. */
    @Composable
    private fun PredictiveBackDismissal() {
        val scope = rememberCoroutineScope()
        val backState = rememberNavigationEventState(currentInfo = NavigationEventInfo.None)
        LaunchedEffect(backState) {
            snapshotFlow { backState.transitionState as? NavigationEventTransitionState.InProgress }
                .collect { inProgress -> inProgress?.let { backProgress.snapTo(it.latestEvent.progress) } }
        }
        NavigationBackHandler(
            state = backState,
            isBackEnabled = dialogProperties.dismissOnBackPress,
            onBackCancelled = { scope.launch { backProgress.animateTo(0f) } },
            onBackCompleted = ::dismiss,
        )
    }

    @Composable
    private fun DialogLayout() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = visibility.value }
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = DialogScrimAlpha))
                    .clickable(
                        interactionSource = null,
                        indication = null,
                        enabled = dialogProperties.dismissOnClickOutside,
                        onClick = ::dismiss,
                    ),
            )
            Box(
                modifier = Modifier.graphicsLayer {
                    alpha = visibility.value
                    val backScale = lerp(1f, PredictivePopScale, PredictivePopEasing.transform(backProgress.value))
                    val scale = enterScale.value * backScale
                    scaleX = scale
                    scaleY = scale
                },
            ) {
                entry.Content()
            }
        }
    }

    /** Fades the dialog out, from wherever a Back gesture left it, before NavDisplay drops it. */
    override suspend fun onRemove() {
        isRemoving = true
        visibility.animateTo(0f, tween(durationMillis = DialogExitMillis, easing = DialogExitEasing))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AnimatedDialogScene<*>) return false
        return key == other.key &&
            previousEntries == other.previousEntries &&
            overlaidEntries == other.overlaidEntries &&
            entry == other.entry &&
            dialogProperties == other.dialogProperties
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + previousEntries.hashCode()
        result = 31 * result + overlaidEntries.hashCode()
        result = 31 * result + entry.hashCode()
        result = 31 * result + dialogProperties.hashCode()
        return result
    }
}

/**
 * A [SceneStrategy] that shows entries that have added [dialog] to their [NavEntry.metadata] in an
 * [AnimatedDialogScene].
 *
 * This strategy should always be added before any non-overlay scene strategies.
 */
class AnimatedDialogSceneStrategy<T : Any> : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val lastEntry = entries.lastOrNull() ?: return null
        val dialogProperties = lastEntry.metadata[DIALOG_KEY] as? DialogProperties ?: return null
        return AnimatedDialogScene(
            key = lastEntry.contentKey,
            entry = lastEntry,
            previousEntries = entries.dropLast(1),
            overlaidEntries = entries.dropLast(1),
            dialogProperties = dialogProperties,
            onBack = onBack,
        )
    }

    companion object {
        /**
         * Marks an entry, in its [NavEntry.metadata], to be shown in an [AnimatedDialogScene].
         *
         * @param dialogProperties whether Back and taps outside the dialog dismiss it.
         */
        fun dialog(dialogProperties: DialogProperties = DialogProperties()): Map<String, Any> =
            mapOf(DIALOG_KEY to dialogProperties)

        internal const val DIALOG_KEY = "animated_dialog"
    }
}

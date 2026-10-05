package pl.masslany.podkop.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.navigation3.scene.SceneInfo
import androidx.navigation3.scene.SceneState
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventHandler
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.NavigationEventState
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * The furthest a gesture moves the predictive Back preview. The rest is left for a completed
 * Back, so only that reaches the end, where [rememberPredictivePopCornersDecorator] hides the
 * leaving screen.
 */
private const val PredictivePopMaxGestureProgress = 0.99f

/**
 * Handles Back for [sceneState] and returns the gesture state `NavDisplay` previews.
 *
 * `NavDisplay` previews any Back that starts as a gesture, and once it completes, plays what's
 * left of the preview. Android starts the Back key as a gesture too, so a key press would play
 * the whole preview, and a quick swipe most of it, after the user is done. Instead, a Back only
 * reaches `NavDisplay` as a gesture once it moves, so the key pops at once, like any other pop.
 * When a previewed Back completes, the preview skips to its end before the pop, which leaves
 * nothing to play.
 */
@Composable
internal fun <T : Any> rememberPredictivePopState(
    sceneState: SceneState<T>,
    entryCount: Int,
    onBack: () -> Unit,
): NavigationEventState<SceneInfo<T>> {
    val scene = sceneState.currentScene
    val currentInfo = SceneInfo(scene)
    val backInfo = sceneState.previousScenes.map { SceneInfo(it) }
    val isBackEnabled = scene.previousEntries.isNotEmpty()

    // A dispatcher of its own, not linked to the app's, so it only carries the preview to NavDisplay.
    val previewOwner = rememberNavigationEventDispatcherOwner(parent = null)
    val previewInput = remember(previewOwner) {
        DirectNavigationEventInput().also { previewOwner.navigationEventDispatcher.addInput(it) }
    }
    val previewState = rememberNavigationEventState(currentInfo = currentInfo, backInfo = backInfo)
    CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides previewOwner) {
        NavigationBackHandler(state = previewState, isBackEnabled = isBackEnabled, onBackCompleted = {})
    }

    val dispatcher = checkNotNull(LocalNavigationEventDispatcherOwner.current) {
        "No NavigationEventDispatcherOwner provided in LocalNavigationEventDispatcherOwner"
    }.navigationEventDispatcher
    val scope = rememberCoroutineScope()
    val handler = remember(previewInput) { PredictivePopGestureHandler(currentInfo, previewInput, scope) }
    SideEffect {
        handler.isBackEnabled = isBackEnabled
        handler.setInfo(currentInfo, backInfo, emptyList())
        // Pops every entry the scene below doesn't show, as NavDisplay's own Back handling does.
        handler.pop = { repeat(entryCount - scene.previousEntries.size) { onBack() } }
    }
    DisposableEffect(dispatcher, handler) {
        dispatcher.addHandler(handler)
        onDispose { handler.remove() }
    }
    return previewState
}

/** Forwards a Back gesture to [previewInput] once it moves, and ends its preview when it completes. */
private class PredictivePopGestureHandler<T : NavigationEventInfo>(
    initialInfo: T,
    private val previewInput: DirectNavigationEventInput,
    private val scope: CoroutineScope,
) : NavigationEventHandler<T>(initialInfo = initialInfo, isBackEnabled = false, isForwardEnabled = false) {

    var pop: () -> Unit = {}

    /** The last event forwarded to [previewInput], or null while the gesture isn't previewed. */
    private var previewEvent: NavigationEvent? = null

    override fun onBackProgressed(event: NavigationEvent) {
        val lastEvent = previewEvent
        if (lastEvent == null && event.progress <= 0f) return
        val nextEvent = event.withProgress(event.progress.coerceAtMost(PredictivePopMaxGestureProgress))
        previewEvent = nextEvent
        if (lastEvent == null) previewInput.backStarted(nextEvent) else previewInput.backProgressed(nextEvent)
    }

    override fun onBackCancelled() {
        if (previewEvent != null) previewInput.backCancelled()
        previewEvent = null
    }

    override fun onBackCompleted() {
        val lastEvent = previewEvent ?: return pop()
        previewEvent = null
        previewInput.backProgressed(lastEvent.withProgress(1f))
        scope.launch {
            // NavDisplay seeks to the end in an effect, so the pop waits until that has drawn.
            repeat(2) { withFrameNanos {} }
            previewInput.backCompleted()
            pop()
        }
    }
}

private fun NavigationEvent.withProgress(progress: Float): NavigationEvent = NavigationEvent(
    swipeEdge = swipeEdge,
    progress = progress,
    touchX = touchX,
    touchY = touchY,
    frameTimeMillis = frameTimeMillis,
)

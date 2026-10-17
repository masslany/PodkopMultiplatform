package pl.masslany.podkop.common.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneInfo
import androidx.navigation3.scene.SceneState
import androidx.navigation3.ui.LocalNavAnimatedContentScope
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Metadata keys of entries that `NavDisplay` shows as overlays, which animate their own Back. */
private val OverlayMetadataKeys = setOf(
    AnimatedDialogSceneStrategy.DIALOG_KEY,
    BottomSheetSceneStrategy.BOTTOM_SHEET_KEY,
    TranslucentSceneStrategy.TRANSLUCENT_KEY,
)

private fun NavEntry<*>.isOverlay(): Boolean = metadata.keys.any { it in OverlayMetadataKeys }

/**
 * Plays [PredictiveBackAnimatable] for Back on a `NavDisplay`, as Decompose's predictive Back
 * animation does: a swipe is previewed once it moves, and a completed one finishes its animation
 * before the pop. A Back that never moves, or has no edge like the Back key, pops at once.
 *
 * `NavDisplay` pops as soon as a gesture completes, so the gesture reaches it through
 * [previewInput] instead, only to keep the screen below drawn while the animation runs.
 */
@Stable
internal class PredictiveBackState(private val scope: CoroutineScope) {

    val previewInput = DirectNavigationEventInput()

    private var animatable by mutableStateOf<PredictiveBackAnimatable?>(null)
    private var leavingKeys by mutableStateOf(emptySet<Any>())
    private var revealedKeys by mutableStateOf(emptySet<Any>())

    /** Content keys of the entries `NavDisplay` is taking off screen. */
    private val exitingKeys = mutableStateSetOf<Any>()

    /** The content keys of the current scene's own entries, and of the scene Back reveals. */
    var sceneKeys by mutableStateOf(emptySet<Any>() to emptySet<Any>())

    var pop: () -> Unit = {}

    private var startEvent: NavigationEvent? = null
    private var settleJob: Job? = null

    /** Backs completed while [settleJob] played, which pop once it has. */
    private var pendingPops = 0

    private val isSettling: Boolean get() = settleJob?.isActive == true

    fun modifier(key: Any, cornerRadius: Dp): Modifier {
        val animatable = animatable ?: return Modifier
        return when (key) {
            in leavingKeys -> animatable.exitModifier(cornerRadius)
            in revealedKeys -> animatable.enterModifier()
            else -> Modifier
        }
    }

    fun onExiting(key: Any) {
        exitingKeys += key
    }

    fun onNotExiting(key: Any) {
        exitingKeys -= key
    }

    fun onStarted(event: NavigationEvent) {
        startEvent = event.takeIf { it.swipeEdge != NavigationEvent.EDGE_NONE && !isSettling }
    }

    fun onProgressed(event: NavigationEvent) {
        if (isSettling) return
        startEvent?.let { start ->
            startEvent = null
            val (leaving, revealed) = sceneKeys
            leavingKeys = leaving
            revealedKeys = revealed
            animatable = PredictiveBackAnimatable(start)
            previewInput.backStarted(start)
        }
        val animatable = animatable ?: return
        scope.launch { animatable.animate(event) }
    }

    fun onCancelled() {
        startEvent = null
        if (isSettling) return
        val animatable = animatable ?: return
        settleJob = scope.launch {
            animatable.cancel()
            previewInput.backCancelled()
            clear()
            popPending()
        }
    }

    fun onCompleted() {
        startEvent = null
        if (isSettling) {
            pendingPops++
            return
        }
        val animatable = animatable ?: return pop()
        settleJob = scope.launch {
            animatable.finish()
            previewInput.backCompleted()
            pop()
            // The leaving screen stays faded out until `NavDisplay` has dropped it, or kept it
            // because something else took the Back, such as a screen asking to discard a draft.
            val leaving = leavingKeys
            snapshotFlow { leaving.none { it in exitingKeys } }.first { it }
            clear()
            popPending()
        }
    }

    private fun clear() {
        animatable = null
        leavingKeys = emptySet()
        revealedKeys = emptySet()
    }

    private suspend fun popPending() {
        while (pendingPops > 0) {
            pendingPops--
            val keys = sceneKeys
            pop()
            // `pop` belongs to the popped scene until the scene below recomposes.
            if (pendingPops > 0) snapshotFlow { sceneKeys }.first { it != keys }
        }
    }
}

@Composable
internal fun rememberPredictiveBackState(): PredictiveBackState {
    val scope = rememberCoroutineScope()
    return remember(scope) { PredictiveBackState(scope) }
}

/** Handles Back for `NavDisplay` and passes it to [state]. */
private class PredictiveBackHandler<T : NavigationEventInfo>(initialInfo: T, private val state: PredictiveBackState) :
    NavigationEventHandler<T>(initialInfo = initialInfo, isBackEnabled = false, isForwardEnabled = false) {

    override fun onBackStarted(event: NavigationEvent) = state.onStarted(event)

    override fun onBackProgressed(event: NavigationEvent) = state.onProgressed(event)

    override fun onBackCancelled() = state.onCancelled()

    override fun onBackCompleted() = state.onCompleted()
}

/**
 * Handles Back for [sceneState] with [state], and returns the gesture state `NavDisplay` takes:
 * one that only [state] drives.
 */
@Composable
internal fun <T : Any> rememberPredictiveBackNavigationEventState(
    state: PredictiveBackState,
    sceneState: SceneState<T>,
    entryCount: Int,
    onBack: () -> Unit,
): NavigationEventState<SceneInfo<T>> {
    val scene = sceneState.currentScene
    val previousScene = sceneState.previousScenes.lastOrNull()
    val currentInfo = SceneInfo(scene)
    val backInfo = sceneState.previousScenes.map { SceneInfo(it) }
    val isBackEnabled = scene.previousEntries.isNotEmpty()

    // A dispatcher of its own, not linked to the app's, so only the state drives what NavDisplay previews.
    val previewOwner = rememberNavigationEventDispatcherOwner(parent = null)
    remember(previewOwner) { previewOwner.navigationEventDispatcher.addInput(state.previewInput) }
    val navigationEventState = rememberNavigationEventState(currentInfo = currentInfo, backInfo = backInfo)
    CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides previewOwner) {
        NavigationBackHandler(state = navigationEventState, isBackEnabled = isBackEnabled, onBackCompleted = {})
    }

    val dispatcher = checkNotNull(LocalNavigationEventDispatcherOwner.current) {
        "No NavigationEventDispatcherOwner provided in LocalNavigationEventDispatcherOwner"
    }.navigationEventDispatcher
    val handler = remember(state) { PredictiveBackHandler(currentInfo, state) }
    SideEffect {
        handler.isBackEnabled = isBackEnabled
        handler.setInfo(currentInfo, backInfo, emptyList())
        val revealed = previousScene?.entries.orEmpty().map { it.contentKey }.toSet()
        state.sceneKeys = scene.entries.map { it.contentKey }.toSet() - revealed to revealed
        // Pops every entry the scene below doesn't show, as NavDisplay's own Back handling does.
        state.pop = { repeat(entryCount - scene.previousEntries.size) { onBack() } }
    }
    DisposableEffect(dispatcher, handler) {
        dispatcher.addHandler(handler)
        onDispose { handler.remove() }
    }
    return navigationEventState
}

/** Moves full-screen entries by [state] while it plays a predictive Back animation. */
@Composable
internal fun <T : Any> rememberPredictiveBackDecorator(state: PredictiveBackState): NavEntryDecorator<T> =
    remember(state) {
        NavEntryDecorator { entry ->
            val key = entry.contentKey
            val isExiting = LocalNavAnimatedContentScope.current.transition.targetState == EnterExitState.PostExit
            DisposableEffect(key, isExiting) {
                if (isExiting) state.onExiting(key)
                onDispose { state.onNotExiting(key) }
            }
            if (entry.isOverlay()) {
                entry.Content()
            } else {
                val cornerRadius = rememberDisplayCornerRadius()
                Box(modifier = Modifier.fillMaxSize().then(state.modifier(key, cornerRadius))) {
                    entry.Content()
                }
            }
        }
    }

/**
 * The scene transition of a predictive Back gesture, which only keeps the screen below drawn:
 * [PredictiveBackState] moves both screens itself.
 */
internal fun <T : Any> AnimatedContentTransitionScope<Scene<T>>.predictiveBackTransform(): ContentTransform =
    ContentTransform(
        targetContentEnter = EnterTransition.None,
        initialContentExit = ExitTransition.KeepUntilTransitionsFinished,
    )

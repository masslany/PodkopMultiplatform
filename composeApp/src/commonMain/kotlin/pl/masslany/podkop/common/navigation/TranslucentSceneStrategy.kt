package pl.masslany.podkop.common.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.util.lerp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventHandler
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import pl.masslany.podkop.common.navigation.TranslucentSceneStrategy.Companion.translucent

private const val TranslucentEnterMillis = 200
private const val TranslucentExitMillis = 150

/**
 * Shows an [entry] full screen in the app's own window, unlike dialogs and bottom sheets, so the
 * scene it was opened from stays drawn below it and shows wherever the entry is see-through.
 */
internal class TranslucentScene<T : Any>(
    override val key: Any,
    private val entry: NavEntry<T>,
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
    private val onBack: () -> Unit,
) : OverlayScene<T> {

    override val entries: List<NavEntry<T>> = listOf(entry)

    // NavDisplay keeps composing the instance it first saw for this key, and calls onRemove on
    // it, so the motion lives here rather than in the composition.
    private val visibility = Animatable(0f)
    private val backProgress = Animatable(0f)
    private var backSwipeEdge by mutableIntStateOf(NavigationEvent.EDGE_NONE)
    private var isRemoving by mutableStateOf(false)
    private var backHandler: SceneBackHandler? = null

    override val content: @Composable (() -> Unit) = {
        LaunchedEffect(Unit) {
            visibility.animateTo(1f, tween(durationMillis = TranslucentEnterMillis, easing = LinearOutSlowInEasing))
        }
        BackHandling()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val progress = backProgress.value
                    if (progress > 0f) {
                        val alpha = PredictivePopScrimAlpha * (1f - progress) * visibility.value
                        drawRect(Color.Black.copy(alpha = alpha))
                    }
                }
                .graphicsLayer {
                    alpha = visibility.value
                    val progress = backProgress.value
                    if (progress > 0f) {
                        val scale = lerp(1f, PredictivePopScale, PredictivePopEasing.transform(progress))
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = predictivePopOrigin(backSwipeEdge)
                        val rounding = (progress / PredictivePopCornerProgress).coerceAtMost(1f)
                        shape = RoundedCornerShape(PredictivePopCornerRadius * rounding)
                        clip = true
                    }
                }
                .then(if (isRemoving) Modifier.consumeAllPointerInput() else Modifier),
        ) {
            entry.Content()
        }
    }

    /**
     * Takes Back ahead of the screens below: in the same window, Back would otherwise reach
     * NavDisplay's own handling, which pops the scene below along with this one.
     */
    @Composable
    private fun BackHandling() {
        val dispatcher = LocalNavigationEventDispatcherOwner.current?.navigationEventDispatcher ?: return
        val scope = rememberCoroutineScope()
        val handler = remember(scope) { SceneBackHandler(scope) }
        DisposableEffect(dispatcher, handler) {
            handler.isBackEnabled = !isRemoving
            dispatcher.addHandler(handler, NavigationEventDispatcher.PRIORITY_OVERLAY)
            backHandler = handler
            onDispose {
                handler.remove()
                if (backHandler === handler) backHandler = null
            }
        }
    }

    override suspend fun onRemove() {
        // Back and taps now belong to the screen below, so a second Back pops that one as expected,
        // and a second tap on the fading content can't.
        isRemoving = true
        backHandler?.isBackEnabled = false
        visibility.animateTo(0f, tween(durationMillis = TranslucentExitMillis, easing = FastOutLinearInEasing))
    }

    private inner class SceneBackHandler(private val scope: CoroutineScope) :
        NavigationEventHandler<NavigationEventInfo>(
            initialInfo = NavigationEventInfo.None,
            isBackEnabled = true,
            isForwardEnabled = false,
        ) {
        override fun onBackStarted(event: NavigationEvent) {
            backSwipeEdge = event.swipeEdge
            scope.launch { backProgress.snapTo(event.progress) }
        }

        override fun onBackProgressed(event: NavigationEvent) {
            scope.launch { backProgress.snapTo(event.progress) }
        }

        override fun onBackCancelled() {
            scope.launch { backProgress.animateTo(0f) }
        }

        override fun onBackCompleted() {
            isBackEnabled = false
            onBack()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TranslucentScene<*>) return false
        return key == other.key &&
            previousEntries == other.previousEntries &&
            overlaidEntries == other.overlaidEntries &&
            entry == other.entry
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + previousEntries.hashCode()
        result = 31 * result + overlaidEntries.hashCode()
        result = 31 * result + entry.hashCode()
        return result
    }
}

private fun Modifier.consumeAllPointerInput(): Modifier = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
        }
    }
}

/** Shows entries marked [translucent] in a [TranslucentScene]. Add it before any non-overlay strategy. */
class TranslucentSceneStrategy<T : Any> : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val lastEntry = entries.lastOrNull() ?: return null
        if (lastEntry.metadata[TRANSLUCENT_KEY] != true) return null
        // NavDisplay rejects an overlay with nothing below it.
        if (entries.size < 2) return null
        return TranslucentScene(
            key = lastEntry.contentKey,
            entry = lastEntry,
            previousEntries = entries.dropLast(1),
            overlaidEntries = entries.dropLast(1),
            onBack = onBack,
        )
    }

    companion object {
        fun translucent(): Map<String, Any> = mapOf(TRANSLUCENT_KEY to true)

        internal const val TRANSLUCENT_KEY = "translucent"
    }
}

package pl.masslany.podkop.common.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner

private const val LIST_PANE_WEIGHT = 0.42f
private const val DETAIL_PANE_WEIGHT = 0.58f
private const val DETAIL_PANE_ANIMATION_DURATION_MS = 220

private data class HomeListDetailScene<T : Any>(
    override val key: Any,
    private val listEntry: NavEntry<T>,
    private val detailEntry: NavEntry<T>?,
    override val previousEntries: List<NavEntry<T>>,
    private val detailsPlaceholder: @Composable () -> Unit,
) : Scene<T> {

    override val entries: List<NavEntry<T>> = listOf(listEntry) + listOfNotNull(detailEntry)

    override val content: @Composable () -> Unit = {
        val predictiveBackProgress = rememberPredictiveBackProgress()

        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(LIST_PANE_WEIGHT)
                    .fillMaxSize(),
            ) {
                listEntry.Content()
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            )

            Box(
                modifier = Modifier
                    .weight(DETAIL_PANE_WEIGHT)
                    .fillMaxSize(),
            ) {
                val detailPaneModifier = if (detailEntry != null && predictiveBackProgress > 0f) {
                    Modifier.graphicsLayer {
                        translationX = size.width * predictiveBackProgress
                        alpha = 1f - (predictiveBackProgress * 0.08f)
                    }
                } else {
                    Modifier
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(detailPaneModifier),
                ) {
                    AnimatedContent(
                        targetState = detailEntry,
                        label = "HomeDetailsPane",
                        transitionSpec = {
                            if (initialState == null && targetState != null) {
                                (
                                    slideInHorizontally(
                                        animationSpec = androidx.compose.animation.core.tween(
                                            durationMillis = DETAIL_PANE_ANIMATION_DURATION_MS,
                                        ),
                                        initialOffsetX = { fullWidth -> fullWidth },
                                    ) + fadeIn()
                                    )
                                    .togetherWith(fadeOut())
                            } else if (initialState != null && targetState == null) {
                                EnterTransition.None.togetherWith(ExitTransition.None)
                            } else {
                                (
                                    slideInHorizontally(
                                        animationSpec = androidx.compose.animation.core.tween(
                                            durationMillis = DETAIL_PANE_ANIMATION_DURATION_MS,
                                        ),
                                        initialOffsetX = { fullWidth -> fullWidth / 4 },
                                    ) + fadeIn()
                                    )
                                    .togetherWith(
                                        slideOutHorizontally(
                                            animationSpec = androidx.compose.animation.core.tween(
                                                durationMillis = DETAIL_PANE_ANIMATION_DURATION_MS,
                                            ),
                                            targetOffsetX = { fullWidth -> -fullWidth / 4 },
                                        ) + fadeOut(),
                                    )
                            }
                        },
                    ) { targetDetailEntry ->
                        if (targetDetailEntry != null) {
                            targetDetailEntry.Content()
                        } else {
                            detailsPlaceholder()
                        }
                    }
                }
            }
        }
    }
}

class HomeListDetailSceneStrategy<T : Any>(
    private val enabled: Boolean,
    private val isListTarget: (Any) -> Boolean,
    private val isDetailTarget: (Any) -> Boolean,
    private val detailsPlaceholder: @Composable () -> Unit,
) : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        if (!enabled || entries.isEmpty()) {
            return null
        }

        val listEntry = entries.firstOrNull() ?: return null
        if (!isListTarget(listEntry.contentKey)) {
            return null
        }

        if (entries.size > 2) {
            return null
        }

        val detailEntry = entries
            .lastOrNull()
            ?.takeIf { it != listEntry && isDetailTarget(it.contentKey) }

        if (entries.size == 2 && detailEntry == null) {
            return null
        }

        return HomeListDetailScene(
            key = listEntry.contentKey,
            listEntry = listEntry,
            detailEntry = detailEntry,
            previousEntries = entries.dropLast(1),
            detailsPlaceholder = detailsPlaceholder,
        )
    }
}

@Composable
private fun rememberPredictiveBackProgress(): Float {
    val dispatcher = LocalNavigationEventDispatcherOwner.current?.navigationEventDispatcher
    val transitionState = dispatcher?.transitionState?.collectAsState()?.value
    val inProgress = transitionState as? NavigationEventTransitionState.InProgress ?: return 0f

    if (inProgress.direction != NavigationEventTransitionState.TRANSITIONING_BACK) {
        return 0f
    }

    return inProgress.latestEvent.progress.coerceIn(0f, 1f)
}

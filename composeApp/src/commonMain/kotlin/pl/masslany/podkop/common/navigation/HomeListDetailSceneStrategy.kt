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
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import kotlin.math.roundToInt

private const val DefaultListPaneFraction = 0.42f
private const val MinListPaneFraction = 0.3f
private const val MaxListPaneFraction = 0.7f
private const val DetailPaneAnimationDurationMs = 220
private val DividerWidth = 1.dp
private val DividerDragHandleWidth = 24.dp
private val DividerGripWidth = 16.dp
private val DividerGripHeight = 44.dp
private val DividerGripCornerRadius = 999.dp
private val DividerGripDotSize = 4.dp
private val DividerGripDotSpacing = 4.dp

private data class HomeListDetailScene<T : Any>(
    override val key: Any,
    private val listEntry: NavEntry<T>,
    private val detailEntry: NavEntry<T>?,
    override val previousEntries: List<NavEntry<T>>,
    private val detailsPlaceholder: @Composable () -> Unit,
) : Scene<T> {

    override val entries: List<NavEntry<T>> = listOf(listEntry) + listOfNotNull(detailEntry)

    override val content: @Composable () -> Unit = {
        var listPaneFraction by rememberSaveable {
            mutableFloatStateOf(DefaultListPaneFraction)
        }
        var containerWidthPx by remember {
            mutableIntStateOf(0)
        }
        val predictiveBackProgress = rememberPredictiveBackProgress()
        val layoutDirection = LocalLayoutDirection.current
        val density = LocalDensity.current
        val dividerWidthPx = remember(density) { with(density) { DividerWidth.roundToPx() } }
        val dividerDragHandleWidthPx = remember(density) {
            with(density) { DividerDragHandleWidth.roundToPx() }
        }
        val availablePaneWidthPx = (containerWidthPx - dividerWidthPx).coerceAtLeast(1)
        val dividerCenterOffsetPx = (availablePaneWidthPx * listPaneFraction) + (dividerWidthPx / 2f)
        val dividerOffsetPx = when (layoutDirection) {
            LayoutDirection.Ltr -> dividerCenterOffsetPx - (dividerDragHandleWidthPx / 2f)

            LayoutDirection.Rtl -> {
                (containerWidthPx - dividerCenterOffsetPx) - (dividerDragHandleWidthPx / 2f)
            }
        }
        val maxDividerOffsetPx = (containerWidthPx - dividerDragHandleWidthPx).coerceAtLeast(0)
        val draggableState = rememberDraggableState { dragAmount ->
            val horizontalDragAmount = when (layoutDirection) {
                LayoutDirection.Ltr -> dragAmount
                LayoutDirection.Rtl -> -dragAmount
            }
            val updatedFraction = listPaneFraction + (horizontalDragAmount / availablePaneWidthPx.toFloat())
            listPaneFraction = updatedFraction.coerceIn(MinListPaneFraction, MaxListPaneFraction)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { containerWidthPx = it.width },
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(listPaneFraction)
                        .fillMaxSize(),
                ) {
                    listEntry.Content()
                }

                Box(
                    modifier = Modifier
                        .width(DividerWidth)
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                )

                Box(
                    modifier = Modifier
                        .weight(1f - listPaneFraction)
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
                                                durationMillis = DetailPaneAnimationDurationMs,
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
                                                durationMillis = DetailPaneAnimationDurationMs,
                                            ),
                                            initialOffsetX = { fullWidth -> fullWidth / 4 },
                                        ) + fadeIn()
                                        )
                                        .togetherWith(
                                            slideOutHorizontally(
                                                animationSpec = androidx.compose.animation.core.tween(
                                                    durationMillis = DetailPaneAnimationDurationMs,
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

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = dividerOffsetPx.roundToInt().coerceIn(0, maxDividerOffsetPx),
                            y = 0,
                        )
                    }
                    .width(DividerDragHandleWidth)
                    .fillMaxHeight()
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = draggableState,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                DividerDragIndicator()
            }
        }
    }
}

@Composable
private fun DividerDragIndicator(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(DividerGripWidth)
            .height(DividerGripHeight)
            .clip(RoundedCornerShape(DividerGripCornerRadius))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.96f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.9f),
                shape = RoundedCornerShape(DividerGripCornerRadius),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DividerGripDotSpacing),
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .size(DividerGripDotSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)),
                )
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

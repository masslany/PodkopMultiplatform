package pl.masslany.podkop.features.imageviewer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kotlin.math.abs
import kotlin.math.sign
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pl.masslany.podkop.common.components.LocalImageActions
import pl.masslany.podkop.common.extensions.toWindowInsets
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.imageviewer.preview.ImageViewerScreenStateProvider
import pl.masslany.podkop.features.imageviewer.preview.NoOpImageViewerActions
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_topbar_back
import podkop.composeapp.generated.resources.accessibility_topbar_downloads
import podkop.composeapp.generated.resources.ic_arrow_back
import podkop.composeapp.generated.resources.ic_copy
import podkop.composeapp.generated.resources.ic_download
import podkop.composeapp.generated.resources.screenshot_preview_action_copy

private const val MaxScale = 5f
private const val MinScale = 1f
private const val ScaleResetThreshold = 1.01f

private val DismissDragRange = 240.dp
private val DismissDistance = 120.dp
private val DismissVelocity = 900.dp
private const val DismissShrink = 0.25f
private const val DismissBackdropFade = 0.7f
private const val DismissDragSteepness = 1.2f
private const val DismissExitMillis = 200
private const val DismissReturnDamping = 0.85f
private const val TopBarFadeMillis = 200

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ImageViewerScreenRoot(
    imageUrl: String,
    paddingValues: PaddingValues,
) {
    val viewModel = koinViewModel<ImageViewerViewModel>(
        parameters = { parametersOf(imageUrl) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    ImageViewerScreenContent(
        paddingValues = paddingValues,
        state = state,
        actions = viewModel,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageViewerScreenContent(
    paddingValues: PaddingValues,
    state: ImageViewerScreenState,
    actions: ImageViewerActions,
    modifier: Modifier = Modifier,
) {
    val topBarInsets = paddingValues.toWindowInsets(includeBottom = false)
    val contentInsets = paddingValues.toWindowInsets(includeTop = false)
    val dismissDrag = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val dragRange = with(LocalDensity.current) { DismissDragRange.toPx() }
    val isDragged by remember { derivedStateOf { dismissDrag.value != Offset.Zero } }
    val topBarAlpha by animateFloatAsState(
        targetValue = if (isDragged) 0f else 1f,
        animationSpec = tween(durationMillis = TopBarFadeMillis),
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                val progress = dismissProgress(drag = dismissDrag.value, range = dragRange)
                drawRect(Color.Black.copy(alpha = 1f - DismissBackdropFade * progress))
            },
        topBar = {
            TopAppBar(
                modifier = Modifier.graphicsLayer { alpha = topBarAlpha },
                title = { },
                navigationIcon = {
                    IconButton(onClick = actions::onBackClicked) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = vectorResource(resource = Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(resource = Res.string.accessibility_topbar_back),
                        )
                    }
                },
                actions = {
                    IconButton(
                        enabled = !state.isCopying,
                        onClick = { actions.onCopyClicked(state.imageUrl) },
                    ) {
                        if (state.isCopying) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = vectorResource(Res.drawable.ic_copy),
                                contentDescription = stringResource(Res.string.screenshot_preview_action_copy),
                            )
                        }
                    }
                    IconButton(
                        enabled = !state.isCopying,
                        onClick = { actions.onDownloadClicked(state.imageUrl) },
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = vectorResource(resource = Res.drawable.ic_download),
                            contentDescription = stringResource(resource = Res.string.accessibility_topbar_downloads),
                        )
                    }
                },
                windowInsets = topBarInsets,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                ),
            )
        },
        contentWindowInsets = contentInsets,
        containerColor = Color.Transparent,
    ) { innerPaddingValues ->
        ZoomableImage(
            modifier = Modifier
                .padding(innerPaddingValues)
                .fillMaxSize(),
            imageUrl = state.imageUrl,
            dismissDrag = dismissDrag,
            dragRange = dragRange,
            onDismiss = actions::onBackClicked,
        )
    }
}

@Composable
private fun ZoomableImage(
    imageUrl: String,
    dismissDrag: Animatable<Offset, AnimationVector2D>,
    dragRange: Float,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val showImageActions = LocalImageActions.current
    val scope = rememberCoroutineScope()
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .testTag(ImageViewerTestTags.Screen.Image)
            .onSizeChanged {
                containerSize = it
            }
            .pointerInput(containerSize) {
                val dismissDistance = DismissDistance.toPx()
                val dismissVelocity = DismissVelocity.toPx()
                detectZoomOrDismissGestures(
                    isZoomed = { scale > MinScale },
                    dismissDragStart = { dismissDrag.value },
                    onTransform = { pan, zoom ->
                        val updatedScale = (scale * zoom).coerceIn(MinScale, MaxScale)

                        scale = updatedScale
                        offset = clampOffsetForScale(
                            containerSize = containerSize,
                            scale = updatedScale,
                            offset = offset + pan,
                        )
                    },
                    onDismissDrag = { drag ->
                        scope.launch { dismissDrag.snapTo(drag) }
                    },
                    onDismissDragEnd = { drag, velocity ->
                        if (closesOnRelease(drag.y, velocity.y, dismissDistance, dismissVelocity)) {
                            currentOnDismiss()
                            scope.launch {
                                dismissDrag.animateTo(
                                    targetValue = Offset(drag.x, sign(drag.y) * containerSize.height),
                                    animationSpec = tween(
                                        durationMillis = DismissExitMillis,
                                        easing = FastOutLinearInEasing,
                                    ),
                                )
                            }
                        } else {
                            scope.launch {
                                dismissDrag.animateTo(
                                    targetValue = Offset.Zero,
                                    animationSpec = spring(
                                        dampingRatio = DismissReturnDamping,
                                        stiffness = Spring.StiffnessMediumLow,
                                    ),
                                    initialVelocity = Offset(velocity.x, velocity.y),
                                )
                            }
                        }
                    },
                )
            }
            .pointerInput(containerSize, scale, imageUrl, showImageActions) {
                detectTapGestures(
                    onLongPress = { showImageActions(imageUrl) },
                    onDoubleTap = { tapOffset ->
                        val currentScale = scale
                        val shouldResetZoom = currentScale > ScaleResetThreshold
                        val updatedScale = if (shouldResetZoom) {
                            MinScale
                        } else {
                            (currentScale * 2f).coerceIn(MinScale, MaxScale)
                        }

                        scale = updatedScale
                        offset = if (shouldResetZoom) {
                            Offset.Zero
                        } else {
                            clampOffsetForScale(
                                containerSize = containerSize,
                                scale = updatedScale,
                                offset = calculateOffsetForDoubleTap(
                                    containerSize = containerSize,
                                    tapOffset = tapOffset,
                                    currentOffset = offset,
                                    currentScale = currentScale,
                                    updatedScale = updatedScale,
                                ),
                            )
                        }
                    },
                )
            },
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
                .graphicsLayer {
                    val drag = dismissDrag.value
                    val dragScale = 1f - DismissShrink * dismissProgress(drag = drag, range = dragRange)
                    scaleX = scale * dragScale
                    scaleY = scale * dragScale
                    translationX = offset.x + drag.x
                    translationY = offset.y + drag.y
                },
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Fit,
        )
    }
}

private enum class ImageGesture { Undecided, Transform, Dismiss }

/**
 * Like `detectTransformGestures`, except that a mostly vertical one-finger drag of an unzoomed image
 * drags it to close the viewer.
 */
private suspend fun PointerInputScope.detectZoomOrDismissGestures(
    isZoomed: () -> Boolean,
    dismissDragStart: () -> Offset,
    onTransform: (pan: Offset, zoom: Float) -> Unit,
    onDismissDrag: (drag: Offset) -> Unit,
    onDismissDragEnd: (drag: Offset, velocity: Velocity) -> Unit,
) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        var gesture = ImageGesture.Undecided
        var zoomBeforeSlop = 1f
        var panBeforeSlop = Offset.Zero
        var drag = Offset.Zero
        val velocityTracker = VelocityTracker()
        do {
            val event = awaitPointerEvent()
            val isCanceled = event.changes.fastAny { it.isConsumed }
            if (!isCanceled) {
                val zoom = event.calculateZoom()
                val pan = event.calculatePan()
                if (gesture == ImageGesture.Undecided) {
                    zoomBeforeSlop *= zoom
                    panBeforeSlop += pan
                    val zoomMotion = abs(1f - zoomBeforeSlop) * event.calculateCentroidSize(useCurrent = false)
                    val touchSlop = viewConfiguration.touchSlop
                    if (zoomMotion > touchSlop || panBeforeSlop.getDistance() > touchSlop) {
                        val fingers = event.changes.count { it.pressed }
                        val isSteep = abs(panBeforeSlop.y) > abs(panBeforeSlop.x) * DismissDragSteepness
                        gesture = if (!isZoomed() && fingers == 1 && isSteep) {
                            drag = dismissDragStart()
                            ImageGesture.Dismiss
                        } else {
                            ImageGesture.Transform
                        }
                    }
                }
                when (gesture) {
                    ImageGesture.Undecided -> Unit

                    ImageGesture.Transform -> if (zoom != 1f || pan != Offset.Zero) onTransform(pan, zoom)

                    ImageGesture.Dismiss -> {
                        drag += pan
                        onDismissDrag(drag)
                        velocityTracker.addPosition(event.changes.first().uptimeMillis, drag)
                    }
                }
                if (gesture != ImageGesture.Undecided) {
                    event.changes.fastForEach { if (it.positionChanged()) it.consume() }
                }
            }
        } while (!isCanceled && event.changes.fastAny { it.pressed })

        if (gesture == ImageGesture.Dismiss) {
            onDismissDragEnd(drag, velocityTracker.calculateVelocity())
        }
    }
}

internal fun dismissProgress(
    drag: Offset,
    range: Float,
): Float = (abs(drag.y) / range).coerceIn(0f, 1f)

/** A fling closes the viewer only away from the middle, so flicking the image back keeps it open. */
internal fun closesOnRelease(
    dragY: Float,
    velocityY: Float,
    minDistance: Float,
    minVelocity: Float,
): Boolean = if (abs(velocityY) > minVelocity) {
    sign(velocityY) == sign(dragY)
} else {
    abs(dragY) > minDistance
}

private fun clampOffsetForScale(
    containerSize: IntSize,
    scale: Float,
    offset: Offset,
): Offset {
    if (scale <= MinScale) {
        return Offset.Zero
    }

    val maxX = (containerSize.width * (scale - 1f)) / 2f
    val maxY = (containerSize.height * (scale - 1f)) / 2f
    return Offset(
        x = offset.x.coerceIn(-maxX, maxX),
        y = offset.y.coerceIn(-maxY, maxY),
    )
}

private fun calculateOffsetForDoubleTap(
    containerSize: IntSize,
    tapOffset: Offset,
    currentOffset: Offset,
    currentScale: Float,
    updatedScale: Float,
): Offset {
    if (currentScale <= 0f) {
        return Offset.Zero
    }

    val center = Offset(
        x = containerSize.width / 2f,
        y = containerSize.height / 2f,
    )
    val tapFromCenter = tapOffset - center
    val contentPoint = (tapFromCenter - currentOffset) / currentScale
    return Offset(
        x = -contentPoint.x * updatedScale,
        y = -contentPoint.y * updatedScale,
    )
}

@Preview
@Composable
private fun ImageViewerScreenContentPreview(
    @PreviewParameter(ImageViewerScreenStateProvider::class) state: ImageViewerScreenState,
) {
    PodkopPreview(darkTheme = false) {
        ImageViewerScreenContent(
            paddingValues = PaddingValues(),
            state = state,
            actions = NoOpImageViewerActions,
        )
    }
}

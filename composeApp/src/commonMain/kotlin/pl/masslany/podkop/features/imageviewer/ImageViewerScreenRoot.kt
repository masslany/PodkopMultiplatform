package pl.masslany.podkop.features.imageviewer

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.common.snackbar.LocalAppSnackbarHostState
import pl.masslany.podkop.features.imageviewer.preview.ImageViewerScreenStateProvider
import pl.masslany.podkop.features.imageviewer.preview.NoOpImageViewerActions
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_topbar_back
import podkop.composeapp.generated.resources.accessibility_topbar_downloads
import podkop.composeapp.generated.resources.ic_arrow_back
import podkop.composeapp.generated.resources.ic_download

private const val MAX_SCALE = 5f
private const val MIN_SCALE = 1f
private const val SCALE_RESET_THRESHOLD = 1.01f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ImageViewerScreenRoot(
    imageUrl: String,
    paddingValues: PaddingValues,
) {
    val viewModel = koinViewModel<ImageViewerViewModel>(
        parameters = { parametersOf(imageUrl) },
    )
    val snackbarHostState = LocalAppSnackbarHostState.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    ImageViewerScreenContent(
        paddingValues = paddingValues,
        state = state,
        actions = viewModel,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageViewerScreenContent(
    paddingValues: PaddingValues,
    state: ImageViewerScreenState,
    actions: ImageViewerActions,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier
            .padding(
                start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
            )
            .fillMaxSize(),
        topBar = {
            TopAppBar(
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
                    IconButton(onClick = { actions.onDownloadClicked(state.imageUrl) }) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = vectorResource(resource = Res.drawable.ic_download),
                            contentDescription = stringResource(resource = Res.string.accessibility_topbar_downloads),
                        )
                    }
                },
                windowInsets = WindowInsets(top = paddingValues.calculateTopPadding()),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                ),
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Black,
    ) { innerPaddingValues ->
        ZoomableImage(
            modifier = Modifier
                .padding(top = innerPaddingValues.calculateTopPadding())
                .fillMaxSize(),
            imageUrl = state.imageUrl,
        )
    }
}

@Composable
private fun ZoomableImage(
    modifier: Modifier = Modifier,
    imageUrl: String,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .background(Color.Black)
            .onSizeChanged {
                containerSize = it
            }
            .pointerInput(containerSize) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val updatedScale = (scale * zoom).coerceIn(MIN_SCALE, MAX_SCALE)

                    scale = updatedScale
                    offset = clampOffsetForScale(
                        containerSize = containerSize,
                        scale = updatedScale,
                        offset = offset + pan,
                    )
                }
            }
            .pointerInput(containerSize, scale) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        val currentScale = scale
                        val shouldResetZoom = currentScale > SCALE_RESET_THRESHOLD
                        val updatedScale = if (shouldResetZoom) {
                            MIN_SCALE
                        } else {
                            (currentScale * 2f).coerceIn(MIN_SCALE, MAX_SCALE)
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
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Fit,
        )
    }
}

private fun clampOffsetForScale(
    containerSize: IntSize,
    scale: Float,
    offset: Offset,
): Offset {
    if (scale <= MIN_SCALE) {
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
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

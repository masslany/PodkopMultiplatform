package pl.masslany.podkop.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberConstraintsSizeResolver
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.size.Scale
import dev.chrisbanes.haze.hazeEffect
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.models.EmbedImageState
import pl.masslany.podkop.common.settings.LocalAppSettings
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.embed_adult_image
import podkop.composeapp.generated.resources.embed_gif_badge
import podkop.composeapp.generated.resources.embed_image_source

@Composable
fun EmbedImage(
    modifier: Modifier = Modifier,
    state: EmbedImageState,
    onImageClick: () -> Unit,
) {
    val appSettings = LocalAppSettings.current
    val context = LocalPlatformContext.current
    val sizeResolver = rememberConstraintsSizeResolver()
    val isGifAutoplayEnabled by appSettings.autoplayGifs.collectAsStateWithLifecycle(initialValue = true)
    var isAdultOverlayVisible by rememberSaveable(state.url) { mutableStateOf(state.isAdult) }
    var isGifPlaybackEnabled by rememberSaveable(state.url) { mutableStateOf(false) }
    var latestSuccessResult by remember(state.url) { mutableStateOf<SuccessResult?>(null) }
    var hasLoadedImage by remember(state.url) { mutableStateOf(false) }
    var isImageLoading by remember(state.url) { mutableStateOf(true) }
    var isPlatformGifReady by remember(state.url) { mutableStateOf(false) }
    var aspectRatio by rememberSaveable(state.url, state.width, state.height) { mutableStateOf(state.aspectRatio) }

    LaunchedEffect(state.url, state.isGif, isGifAutoplayEnabled) {
        isGifPlaybackEnabled = !state.isGif || isGifAutoplayEnabled
    }

    LaunchedEffect(isGifPlaybackEnabled, latestSuccessResult) {
        val result = latestSuccessResult ?: return@LaunchedEffect
        if (state.isGif) {
            setImageAnimationPlaying(
                image = result.image,
                isPlaying = isGifPlaybackEnabled,
            )
        }
    }

    LaunchedEffect(state.url, state.isGif, isGifPlaybackEnabled) {
        if (state.isGif && supportsPlatformGifImage && isGifPlaybackEnabled) {
            isPlatformGifReady = false
        }
    }

    val usePlatformGifRenderer = state.isGif && supportsPlatformGifImage && isGifPlaybackEnabled
    val isGifOverlayVisible = state.isGif && !isGifAutoplayEnabled && !isGifPlaybackEnabled
    val imageContainerModifier = Modifier
        .then(sizeResolver)
        .fillMaxWidth()
        .then(
            if (aspectRatio != null) {
                Modifier.aspectRatio(aspectRatio!!)
            } else {
                Modifier.heightIn(min = 120.dp)
            },
        )
        .onSizeChanged { size ->
            if (hasLoadedImage && size.width > 0 && size.height > 0) {
                val measuredRatio = size.width.toFloat() / size.height.toFloat()
                if (measuredRatio.isFinite() && measuredRatio > 0f) {
                    if (aspectRatio != measuredRatio) {
                        aspectRatio = measuredRatio
                    }
                }
            }
        }
        .clickable {
            when {
                isAdultOverlayVisible -> {
                    isAdultOverlayVisible = false
                }

                isGifOverlayVisible -> {
                    isGifPlaybackEnabled = true
                }

                else -> {
                    onImageClick()
                }
            }
        }
        .then(
            if (isAdultOverlayVisible) {
                Modifier.hazeEffect { blurEnabled = true }
            } else {
                Modifier
            },
        )

    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                shape = RoundedCornerShape(8.dp),
            )
            .padding(8.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = imageContainerModifier) {
                if (isImageLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
                                shape = RoundedCornerShape(6.dp),
                            ),
                    )
                }

                if (usePlatformGifRenderer) {
                    AsyncImage(
                        modifier = Modifier.fillMaxSize(),
                        model = ImageRequest.Builder(context)
                            .data(state.url)
                            .memoryCacheKey(state.url)
                            .diskCacheKey(state.url)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .networkCachePolicy(CachePolicy.ENABLED)
                            .scale(Scale.FIT)
                            .size(sizeResolver)
                            .build(),
                        onSuccess = {
                            hasLoadedImage = true
                            isImageLoading = false
                            latestSuccessResult = it.result
                        },
                        onError = {
                            isImageLoading = false
                        },
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                    )

                    PlatformGifImage(
                        modifier = Modifier.fillMaxSize(),
                        url = state.url,
                        onSuccess = {
                            hasLoadedImage = true
                            isPlatformGifReady = true
                            isImageLoading = false
                        },
                        onError = {
                            isImageLoading = false
                        },
                    )
                } else {
                    AsyncImage(
                        modifier = Modifier.fillMaxSize(),
                        model = ImageRequest.Builder(context)
                            .data(state.url)
                            .memoryCacheKey(state.url)
                            .diskCacheKey(state.url)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .networkCachePolicy(CachePolicy.ENABLED)
                            .scale(Scale.FIT)
                            .size(sizeResolver)
                            .build(),
                        onSuccess = { success ->
                            hasLoadedImage = true
                            isImageLoading = false
                            latestSuccessResult = success.result
                            if (state.isGif) {
                                setImageAnimationPlaying(
                                    image = success.result.image,
                                    isPlaying = isGifPlaybackEnabled,
                                )
                            }
                        },
                        onError = {
                            isImageLoading = false
                        },
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                    )
                }
            }

            if (usePlatformGifRenderer && !isPlatformGifReady && !isAdultOverlayVisible) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(
                            color = Color.Black.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(24.dp),
                        )
                        .padding(8.dp),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White,
                    )
                }
            }

            if (isGifOverlayVisible && !isAdultOverlayVisible) {
                Text(
                    text = stringResource(resource = Res.string.embed_gif_badge),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(4.dp),
                        )
                        .padding(
                            horizontal = 8.dp,
                            vertical = 2.dp,
                        ),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (isAdultOverlayVisible) {
                Text(
                    text = stringResource(resource = Res.string.embed_adult_image),
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                        .copy(
                            drawStyle = Stroke(
                                miter = 10f,
                                width = 6f,
                                join = StrokeJoin.Round,
                            ),
                        ),
                )

                Text(
                    text = stringResource(resource = Res.string.embed_adult_image),
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = stringResource(
                resource = Res.string.embed_image_source,
                state.source,
            ),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@file:OptIn(
    kotlinx.cinterop.ExperimentalForeignApi::class,
    kotlinx.cinterop.BetaInteropApi::class,
)

package pl.masslany.podkop.common.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.reinterpret
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreFoundation.CFDataRef
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.ImageIO.CGImageSourceCreateImageAtIndex
import platform.ImageIO.CGImageSourceCreateWithData
import platform.ImageIO.CGImageSourceGetCount
import platform.UIKit.UIImage
import platform.UIKit.UIImageView

internal actual val supportsPlatformGifImage: Boolean = true

// https://www.nmvasani.com/post/seamless-gif-loading-in-compose-multiplatform-android-ios-implementation

@Composable
internal actual fun PlatformGifImage(
    modifier: Modifier,
    url: String,
    onSuccess: () -> Unit,
    onError: () -> Unit,
) {
    val imageView = remember {
        UIImageView().apply {
            clipsToBounds = true
            userInteractionEnabled = false
        }
    }

    LaunchedEffect(url, imageView) {
        imageView.stopAnimating()
        imageView.image = null

        val data = withContext(Dispatchers.Default) { fetchGifData(url) }
        if (data == null) {
            onError()
            return@LaunchedEffect
        }

        val image = withContext(Dispatchers.Default) {
            animatedGifImage(data)
        }
        if (image == null) {
            onError()
            return@LaunchedEffect
        }

        imageView.image = image
        imageView.startAnimating()
        onSuccess()
    }

    DisposableEffect(imageView) {
        onDispose {
            imageView.stopAnimating()
            imageView.image = null
        }
    }

    UIKitView(
        modifier = modifier,
        factory = { imageView },
        update = { },
    )
}

private fun fetchGifData(url: String): NSData? {
    val nsUrl = NSURL.URLWithString(url) ?: return null
    return runCatching { NSData.create(contentsOfURL = nsUrl) }.getOrNull()
}

private fun animatedGifImage(data: NSData): UIImage? {
    val cfData: CFDataRef? = CFBridgingRetain(data)?.reinterpret()
    val imageSource = CGImageSourceCreateWithData(
        data = cfData,
        options = null,
    ) ?: run {
        CFBridgingRelease(cfData)
        return null
    }
    val frameCount = CGImageSourceGetCount(imageSource).toInt()

    if (frameCount <= 1) {
        CFBridgingRelease(cfData)
        return UIImage(data = data)
    }

    val frames = ArrayList<UIImage>(frameCount)
    for (index in 0 until frameCount) {
        val cgImage = CGImageSourceCreateImageAtIndex(
            isrc = imageSource,
            index = index.toULong(),
            options = null,
        ) ?: continue
        frames.add(UIImage.imageWithCGImage(cgImage))
    }

    if (frames.isEmpty()) {
        CFBridgingRelease(cfData)
        return null
    }

    val durationSeconds = (frameCount * 0.1).coerceAtLeast(0.1)
    val animatedImage = UIImage.animatedImageWithImages(frames, durationSeconds)
    CFBridgingRelease(cfData)
    return animatedImage
}

package pl.masslany.podkop.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.glance.appwidget.updateAll
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pl.masslany.podkop.business.hits.domain.main.HitsRepository
import pl.masslany.podkop.business.hits.domain.models.request.HitsSortType
import pl.masslany.podkop.business.startup.api.StartupManager
import pl.masslany.podkop.business.startup.models.AppState
import pl.masslany.podkop.common.logging.api.AppLogger

class HitsWidgetWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params), KoinComponent {
    private val hitsRepository by inject<HitsRepository>()
    private val startupManager by inject<StartupManager>()
    private val store by inject<HitsWidgetStore>()
    private val logger by inject<AppLogger>()

    override suspend fun doWork(): Result {
        store.load()
        store.setRefreshing(true)
        HitsWidget().updateAll(applicationContext)

        return try {
            refresh()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (exception: Exception) {
            failed(reason = "unexpected error", throwable = exception)
        } finally {
            store.setRefreshing(false)
            withContext(NonCancellable) { HitsWidget().updateAll(applicationContext) }
        }
    }

    private suspend fun refresh(): Result {
        if (!awaitStartup()) {
            return failed(reason = "app startup did not finish", throwable = null)
        }

        val hits = hitsRepository.getLinkHits(hitsSortType = HitsSortType.Day)
            .getOrElse { return failed(reason = "loading hits failed", throwable = it) }

        val items = hits.data.toHitsWidgetItems()
        val thumbnails = downloadThumbnails(items)
        store.save(
            snapshot = HitsWidgetSnapshot(updatedAtMillis = System.currentTimeMillis(), items = items),
            thumbnails = thumbnails,
        )
        return Result.success()
    }

    /** The API token comes from app startup, which a widget refresh may race in a fresh process. */
    private suspend fun awaitStartup(): Boolean {
        val state = withTimeoutOrNull(STARTUP_TIMEOUT_MILLIS) {
            startupManager.state.first { it !is AppState.Initializing }
        }
        if (state == AppState.Ready) return true

        if (state == AppState.Error) startupManager.retry()
        return startupManager.state.value == AppState.Ready
    }

    private fun failed(reason: String, throwable: Throwable?): Result {
        logger.warn("Hits widget refresh failed: $reason", throwable)
        store.markRefreshFailed()
        return if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
    }

    private suspend fun downloadThumbnails(items: List<HitsWidgetItem>): Map<Int, ByteArray> = coroutineScope {
        items
            .mapNotNull { item -> item.thumbnailUrl?.let { url -> item.id to url } }
            .map { (linkId, url) -> async(Dispatchers.IO) { downloadThumbnail(url)?.let { linkId to it } } }
            .awaitAll()
            .filterNotNull()
            .toMap()
    }

    private suspend fun downloadThumbnail(url: String): ByteArray? = withContext(Dispatchers.IO) {
        runCatching {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = NETWORK_TIMEOUT_MILLIS
            connection.readTimeout = NETWORK_TIMEOUT_MILLIS
            val bytes = try {
                connection.inputStream.use { it.readBytes() }
            } finally {
                connection.disconnect()
            }
            bytes.toThumbnailJpeg()
        }.onFailure {
            logger.debug("Skipping hits widget thumbnail: ${it.message}")
        }.getOrNull()
    }

    companion object {
        private const val PERIODIC_WORK_NAME = "hits_widget_periodic_refresh"
        private const val REFRESH_WORK_NAME = "hits_widget_refresh"
        private const val REFRESH_INTERVAL_HOURS = 1L
        private const val STARTUP_TIMEOUT_MILLIS = 30_000L
        private const val NETWORK_TIMEOUT_MILLIS = 10_000
        private const val MAX_RETRIES = 2

        private val networkConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        /** Keeps the hourly refresh running while any widget exists; scheduling again keeps the existing one. */
        fun ensureScheduled(context: Context) {
            val request = PeriodicWorkRequestBuilder<HitsWidgetWorker>(REFRESH_INTERVAL_HOURS, TimeUnit.HOURS)
                .setConstraints(networkConstraints)
                // A new widget already gets an immediate refresh, so the hourly one starts an hour later.
                .setInitialDelay(REFRESH_INTERVAL_HOURS, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(PERIODIC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        fun refreshNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<HitsWidgetWorker>()
                .setConstraints(networkConstraints)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(REFRESH_WORK_NAME, ExistingWorkPolicy.KEEP, request)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).apply {
                cancelUniqueWork(PERIODIC_WORK_NAME)
                cancelUniqueWork(REFRESH_WORK_NAME)
            }
        }
    }
}

private const val THUMBNAIL_WIDTH_PX = 168
private const val THUMBNAIL_HEIGHT_PX = 126
private const val THUMBNAIL_JPEG_QUALITY = 85

/** Center-crops to the widget's 4:3 thumbnail and re-encodes it small enough for widget updates. */
private fun ByteArray.toThumbnailJpeg(): ByteArray? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(this, 0, size, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    while (bounds.outWidth / (sampleSize * 2) >= THUMBNAIL_WIDTH_PX &&
        bounds.outHeight / (sampleSize * 2) >= THUMBNAIL_HEIGHT_PX
    ) {
        sampleSize *= 2
    }
    val decoded = BitmapFactory.decodeByteArray(this, 0, size, BitmapFactory.Options().apply { inSampleSize = sampleSize })
        ?: return null

    val targetRatio = THUMBNAIL_WIDTH_PX.toFloat() / THUMBNAIL_HEIGHT_PX
    val cropWidth = minOf(decoded.width, (decoded.height * targetRatio).toInt()).coerceAtLeast(1)
    val cropHeight = minOf(decoded.height, (decoded.width / targetRatio).toInt()).coerceAtLeast(1)
    val cropped = Bitmap.createBitmap(
        decoded,
        (decoded.width - cropWidth) / 2,
        (decoded.height - cropHeight) / 2,
        cropWidth,
        cropHeight,
    )
    val scaled = Bitmap.createScaledBitmap(cropped, THUMBNAIL_WIDTH_PX, THUMBNAIL_HEIGHT_PX, true)

    return ByteArrayOutputStream().use { output ->
        scaled.compress(Bitmap.CompressFormat.JPEG, THUMBNAIL_JPEG_QUALITY, output)
        output.toByteArray()
    }
}

package pl.masslany.podkop

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class CrashlyticsTree(
    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance(),
) : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < Log.INFO) {
            return
        }

        val priorityLabel = when (priority) {
            Log.INFO -> "I"
            Log.WARN -> "W"
            Log.ERROR -> "E"
            Log.ASSERT -> "A"
            else -> priority.toString()
        }
        val resolvedTag = tag ?: "Podkop"

        crashlytics.log("$priorityLabel/$resolvedTag: $message")

        if (priority >= Log.WARN && t != null) {
            crashlytics.recordException(t)
        }
    }
}

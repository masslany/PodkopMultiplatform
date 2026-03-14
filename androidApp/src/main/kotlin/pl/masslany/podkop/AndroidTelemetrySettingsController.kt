package pl.masslany.podkop

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import pl.masslany.podkop.common.persistence.api.KeyValueStorage
import pl.masslany.podkop.common.settings.TelemetrySettingsController

class AndroidTelemetrySettingsController(
    application: Application,
    private val keyValueStorage: KeyValueStorage,
) : TelemetrySettingsController {
    private val firebaseAnalytics by lazy { FirebaseAnalytics.getInstance(application) }
    private val firebaseCrashlytics by lazy { FirebaseCrashlytics.getInstance() }

    override val supportsControls: Boolean = true

    override val analyticsEnabled: Flow<Boolean> =
        keyValueStorage.observeBoolean(ANALYTICS_ENABLED_KEY)
            .map { it ?: true }
            .distinctUntilChanged()

    override val crashReportingEnabled: Flow<Boolean> =
        keyValueStorage.observeBoolean(CRASH_REPORTING_ENABLED_KEY)
            .map { it ?: true }
            .distinctUntilChanged()

    override suspend fun setAnalyticsEnabled(enabled: Boolean) {
        keyValueStorage.putBoolean(ANALYTICS_ENABLED_KEY, enabled)
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
    }

    override suspend fun setCrashReportingEnabled(enabled: Boolean) {
        keyValueStorage.putBoolean(CRASH_REPORTING_ENABLED_KEY, enabled)
        firebaseCrashlytics.isCrashlyticsCollectionEnabled = enabled
    }

    override suspend fun syncCollectionStates() {
        firebaseAnalytics.setAnalyticsCollectionEnabled(
            keyValueStorage.getBoolean(ANALYTICS_ENABLED_KEY) ?: true,
        )
        firebaseCrashlytics.isCrashlyticsCollectionEnabled = keyValueStorage.getBoolean(CRASH_REPORTING_ENABLED_KEY)
            ?: true
    }

    private companion object {
        const val ANALYTICS_ENABLED_KEY = "SETTINGS_ANALYTICS_ENABLED"
        const val CRASH_REPORTING_ENABLED_KEY = "SETTINGS_CRASH_REPORTING_ENABLED"
    }
}

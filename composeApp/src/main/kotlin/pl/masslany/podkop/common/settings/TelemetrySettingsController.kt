package pl.masslany.podkop.common.settings

import kotlinx.coroutines.flow.Flow

interface TelemetrySettingsController {
    val supportsControls: Boolean
    val analyticsEnabled: Flow<Boolean>
    val crashReportingEnabled: Flow<Boolean>

    suspend fun setAnalyticsEnabled(enabled: Boolean)

    suspend fun setCrashReportingEnabled(enabled: Boolean)

    suspend fun syncCollectionStates()
}

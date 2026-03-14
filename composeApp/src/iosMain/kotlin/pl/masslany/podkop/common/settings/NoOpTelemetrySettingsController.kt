package pl.masslany.podkop.common.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class NoOpTelemetrySettingsController : TelemetrySettingsController {
    override val supportsControls: Boolean = false
    override val analyticsEnabled: Flow<Boolean> = flowOf(false)
    override val crashReportingEnabled: Flow<Boolean> = flowOf(false)

    override suspend fun setAnalyticsEnabled(enabled: Boolean) = Unit

    override suspend fun setCrashReportingEnabled(enabled: Boolean) = Unit

    override suspend fun syncCollectionStates() = Unit
}

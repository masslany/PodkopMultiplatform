package pl.masslany.podkop.test.fakes

import kotlinx.coroutines.flow.MutableStateFlow
import pl.masslany.podkop.common.settings.TelemetrySettingsController

/** Keeps the telemetry settings in memory and never turns Firebase collection back on. */
class FakeTelemetrySettingsController : TelemetrySettingsController {
    override val supportsControls: Boolean = true
    override val analyticsEnabled = MutableStateFlow(true)
    override val crashReportingEnabled = MutableStateFlow(true)

    override suspend fun setAnalyticsEnabled(enabled: Boolean) {
        analyticsEnabled.value = enabled
    }

    override suspend fun setCrashReportingEnabled(enabled: Boolean) {
        crashReportingEnabled.value = enabled
    }

    override suspend fun syncCollectionStates() = Unit
}

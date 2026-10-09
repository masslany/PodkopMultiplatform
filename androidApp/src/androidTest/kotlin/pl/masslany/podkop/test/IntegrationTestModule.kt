package pl.masslany.podkop.test

import org.koin.dsl.module
import pl.masslany.podkop.common.network.infrastructure.main.NetworkConfig
import pl.masslany.podkop.common.settings.TelemetrySettingsController
import pl.masslany.podkop.test.fakes.FakeTelemetrySettingsController

fun integrationTestModule(baseUrl: String) = module {
    single {
        NetworkConfig(baseUrl = baseUrl)
    }
    single<TelemetrySettingsController> {
        FakeTelemetrySettingsController()
    }
}

package pl.masslany.podkop.test

import org.koin.dsl.module
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.notifications.domain.main.NotificationsRepository
import pl.masslany.podkop.business.startup.api.StartupManager
import pl.masslany.podkop.common.network.infrastructure.main.NetworkConfig
import pl.masslany.podkop.test.fakes.FakeAuthRepository
import pl.masslany.podkop.test.fakes.FakeNotificationsRepository
import pl.masslany.podkop.test.fakes.FakeStartupManager

val integrationTestModule = module {
    single {
        NetworkConfig(baseUrl = IntegrationTestNetwork.baseUrl)
    }
    single<StartupManager> {
        FakeStartupManager()
    }
    single<AuthRepository> {
        FakeAuthRepository()
    }
    single<NotificationsRepository> {
        FakeNotificationsRepository()
    }
}

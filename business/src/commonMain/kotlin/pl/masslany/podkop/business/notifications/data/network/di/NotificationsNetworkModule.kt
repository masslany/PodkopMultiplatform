package pl.masslany.podkop.business.notifications.data.network.di

import org.koin.dsl.module
import pl.masslany.podkop.business.notifications.data.api.NotificationsDataSource
import pl.masslany.podkop.business.notifications.data.network.api.NotificationsApi
import pl.masslany.podkop.business.notifications.data.network.client.NotificationsApiClient
import pl.masslany.podkop.business.notifications.data.network.main.NotificationsDataSourceImpl

val notificationsNetworkModule = module {
    single<NotificationsApi> { NotificationsApiClient(apiClient = get()) }
    single<NotificationsDataSource> { NotificationsDataSourceImpl(notificationsApi = get()) }
}

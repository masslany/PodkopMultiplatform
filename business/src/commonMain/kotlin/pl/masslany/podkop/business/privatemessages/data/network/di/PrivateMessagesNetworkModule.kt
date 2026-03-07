package pl.masslany.podkop.business.privatemessages.data.network.di

import org.koin.dsl.module
import pl.masslany.podkop.business.privatemessages.data.api.PrivateMessagesDataSource
import pl.masslany.podkop.business.privatemessages.data.network.api.PrivateMessagesApi
import pl.masslany.podkop.business.privatemessages.data.network.client.PrivateMessagesApiClient
import pl.masslany.podkop.business.privatemessages.data.network.main.PrivateMessagesDataSourceImpl

val privateMessagesNetworkModule = module {
    single<PrivateMessagesApi> { PrivateMessagesApiClient(apiClient = get()) }
    single<PrivateMessagesDataSource> { PrivateMessagesDataSourceImpl(privateMessagesApi = get()) }
}

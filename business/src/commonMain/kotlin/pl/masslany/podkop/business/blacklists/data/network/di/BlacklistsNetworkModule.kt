package pl.masslany.podkop.business.blacklists.data.network.di

import org.koin.dsl.module
import pl.masslany.podkop.business.blacklists.data.api.BlacklistsDataSource
import pl.masslany.podkop.business.blacklists.data.network.api.BlacklistsApi
import pl.masslany.podkop.business.blacklists.data.network.client.BlacklistsApiClient
import pl.masslany.podkop.business.blacklists.data.network.main.BlacklistsDataSourceImpl

val blacklistsNetworkModule = module {
    single<BlacklistsApi> { BlacklistsApiClient(apiClient = get()) }
    single<BlacklistsDataSource> { BlacklistsDataSourceImpl(blacklistsApi = get()) }
}

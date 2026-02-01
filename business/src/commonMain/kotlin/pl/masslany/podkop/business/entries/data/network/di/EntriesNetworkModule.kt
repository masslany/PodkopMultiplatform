package pl.masslany.podkop.business.entries.data.network.di

import org.koin.dsl.module
import pl.masslany.podkop.business.entries.data.api.EntriesDataSource
import pl.masslany.podkop.business.entries.data.network.api.EntriesApi
import pl.masslany.podkop.business.entries.data.network.client.EntriesApiClient
import pl.masslany.podkop.business.entries.data.network.main.EntriesDataSourceImpl

val entriesNetworkModule = module {
    single<EntriesApi> { EntriesApiClient(apiClient = get()) }
    single<EntriesDataSource> { EntriesDataSourceImpl(entriesApi = get()) }
}
package pl.masslany.podkop.business.search.data.network.di

import org.koin.dsl.module
import pl.masslany.podkop.business.search.data.api.SearchDataSource
import pl.masslany.podkop.business.search.data.network.api.SearchApi
import pl.masslany.podkop.business.search.data.network.client.SearchApiClient
import pl.masslany.podkop.business.search.data.network.main.SearchDataSourceImpl

val searchNetworkModule = module {
    single<SearchApi> { SearchApiClient(apiClient = get()) }
    single<SearchDataSource> { SearchDataSourceImpl(searchApi = get()) }
}

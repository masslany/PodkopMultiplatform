package pl.masslany.podkop.business.observed.data.network.di

import org.koin.dsl.module
import pl.masslany.podkop.business.observed.data.api.ObservedDataSource
import pl.masslany.podkop.business.observed.data.network.api.ObservedApi
import pl.masslany.podkop.business.observed.data.network.client.ObservedApiClient
import pl.masslany.podkop.business.observed.data.network.main.ObservedDataSourceImpl

val observedNetworkModule = module {
    single<ObservedApi> { ObservedApiClient(apiClient = get()) }
    single<ObservedDataSource> { ObservedDataSourceImpl(observedApi = get()) }
}

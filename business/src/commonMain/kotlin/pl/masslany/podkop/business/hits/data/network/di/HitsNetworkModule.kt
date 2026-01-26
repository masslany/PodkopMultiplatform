package pl.masslany.podkop.business.hits.data.network.di

import org.koin.dsl.module
import pl.masslany.podkop.business.hits.data.api.HitsDataSource
import pl.masslany.podkop.business.hits.data.network.api.HitsApi
import pl.masslany.podkop.business.hits.data.network.client.HitsApiClient
import pl.masslany.podkop.business.hits.data.network.main.HitsDataSourceImpl

val hitsNetworkModule = module {
    single<HitsApi> { HitsApiClient(get()) }
    single<HitsDataSource> { HitsDataSourceImpl(get()) }
}

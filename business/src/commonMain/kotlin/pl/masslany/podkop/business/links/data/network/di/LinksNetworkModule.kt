package pl.masslany.podkop.business.links.data.network.di

import org.koin.dsl.module
import pl.masslany.podkop.business.links.data.api.LinksDataSource
import pl.masslany.podkop.business.links.data.network.api.LinksApi
import pl.masslany.podkop.business.links.data.network.client.LinksApiClient
import pl.masslany.podkop.business.links.data.network.main.LinksDataSourceImpl

val linksNetworkModule = module {
    single<LinksApi> { LinksApiClient(get()) }
    single<LinksDataSource> { LinksDataSourceImpl(get()) }
}

package pl.masslany.podkop.business.embeds.data.network.di

import org.koin.dsl.module
import pl.masslany.podkop.business.embeds.data.api.StreamableVideoDataSource
import pl.masslany.podkop.business.embeds.data.network.api.StreamableVideoApi
import pl.masslany.podkop.business.embeds.data.network.client.StreamableVideoApiClient
import pl.masslany.podkop.business.embeds.data.network.main.StreamableVideoDataSourceImpl

val streamableVideoNetworkModule = module {
    single<StreamableVideoApi> { StreamableVideoApiClient(get()) }
    single<StreamableVideoDataSource> { StreamableVideoDataSourceImpl(get()) }
}

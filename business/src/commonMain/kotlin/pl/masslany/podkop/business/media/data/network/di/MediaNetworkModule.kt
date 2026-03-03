package pl.masslany.podkop.business.media.data.network.di

import org.koin.dsl.module
import pl.masslany.podkop.business.media.data.api.MediaDataSource
import pl.masslany.podkop.business.media.data.network.api.MediaApi
import pl.masslany.podkop.business.media.data.network.client.MediaApiClient
import pl.masslany.podkop.business.media.data.network.main.MediaDataSourceImpl

val mediaNetworkModule = module {
    single<MediaApi> { MediaApiClient(apiClient = get()) }
    single<MediaDataSource> { MediaDataSourceImpl(mediaApi = get()) }
}

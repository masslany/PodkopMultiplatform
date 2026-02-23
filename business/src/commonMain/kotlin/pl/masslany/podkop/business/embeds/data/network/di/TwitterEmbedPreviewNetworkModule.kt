package pl.masslany.podkop.business.embeds.data.network.di

import org.koin.dsl.module
import pl.masslany.podkop.business.embeds.data.api.TwitterEmbedPreviewDataSource
import pl.masslany.podkop.business.embeds.data.network.api.TwitterEmbedPreviewApi
import pl.masslany.podkop.business.embeds.data.network.client.TwitterEmbedPreviewApiClient
import pl.masslany.podkop.business.embeds.data.network.main.TwitterEmbedPreviewDataSourceImpl

val twitterEmbedPreviewNetworkModule = module {
    single<TwitterEmbedPreviewApi> { TwitterEmbedPreviewApiClient(get()) }
    single<TwitterEmbedPreviewDataSource> { TwitterEmbedPreviewDataSourceImpl(get()) }
}

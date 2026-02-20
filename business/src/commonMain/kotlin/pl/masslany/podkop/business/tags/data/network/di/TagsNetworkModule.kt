package pl.masslany.podkop.business.tags.data.network.di

import org.koin.dsl.module
import pl.masslany.podkop.business.tags.data.api.TagsDataSource
import pl.masslany.podkop.business.tags.data.network.api.TagsApi
import pl.masslany.podkop.business.tags.data.network.client.TagsApiClient
import pl.masslany.podkop.business.tags.data.network.main.TagsDataSourceImpl

val tagsNetworkModule = module {
    single<TagsApi> { TagsApiClient(apiClient = get()) }
    single<TagsDataSource> { TagsDataSourceImpl(tagsApi = get()) }
}

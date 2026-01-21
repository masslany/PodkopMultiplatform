package pl.masslany.podkop.common.network.infrastructure.di

import io.ktor.client.HttpClient
import org.koin.dsl.module
import pl.masslany.podkop.common.network.api.ApiClient
import pl.masslany.podkop.common.network.infrastructure.main.ApiClientImpl
import pl.masslany.podkop.common.network.infrastructure.main.HttpClientFactory

val networkModule = module {

    single<HttpClient> {
        HttpClientFactory(
            configStorage = get(),
        ).create()
    }

    single<ApiClient> {
        ApiClientImpl(
            httpClient = get(),
        )
    }

}

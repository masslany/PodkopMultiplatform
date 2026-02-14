package pl.masslany.podkop.common.network.infrastructure.di

import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import pl.masslany.podkop.common.network.api.ApiClient
import pl.masslany.podkop.common.network.infrastructure.main.ApiClientImpl
import pl.masslany.podkop.common.network.infrastructure.main.HttpClientFactory
import pl.masslany.podkop.common.network.infrastructure.main.TokenRefreshCoordinator

val networkModule = module {
    single<Json> {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    single {
        TokenRefreshCoordinator(
            configStorage = get(),
            json = get(),
        )
    }

    single<HttpClient> {
        HttpClientFactory(
            configStorage = get(),
            tokenRefreshCoordinator = get(),
            json = get(),
        ).create()
    }

    single<ApiClient> {
        ApiClientImpl(
            httpClient = get(),
        )
    }

}

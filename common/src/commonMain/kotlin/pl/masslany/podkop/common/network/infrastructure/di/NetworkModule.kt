package pl.masslany.podkop.common.network.infrastructure.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.Logger
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import pl.masslany.podkop.common.network.api.ApiClient
import pl.masslany.podkop.common.network.infrastructure.main.ApiClientImpl
import pl.masslany.podkop.common.network.infrastructure.main.HttpClientFactory
import pl.masslany.podkop.common.network.infrastructure.main.KtorClientLogger
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
            logger = get(),
        )
    }

    single<Logger> {
        KtorClientLogger(
            appLogger = get(),
        )
    }

    single<HttpClient> {
        HttpClientFactory(
            configStorage = get(),
            tokenRefreshCoordinator = get(),
            json = get(),
            logger = get(),
        ).create()
    }

    single<ApiClient> {
        ApiClientImpl(
            httpClient = get(),
        )
    }

}

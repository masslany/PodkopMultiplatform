package pl.masslany.podkop.business.auth.data.di

import org.koin.dsl.module
import pl.masslany.podkop.business.auth.data.api.AuthDataSource
import pl.masslany.podkop.business.auth.data.main.AuthRepositoryImpl
import pl.masslany.podkop.business.auth.data.network.api.AuthApi
import pl.masslany.podkop.business.auth.data.network.client.AuthApiClient
import pl.masslany.podkop.business.auth.data.network.main.AuthDataSourceImpl
import pl.masslany.podkop.business.auth.domain.AuthRepository

val authDataModule = module {
    single<AuthApi> { AuthApiClient(get()) }
    single<AuthDataSource> {
        AuthDataSourceImpl(get(), get(), get())
    }
    factory<AuthRepository> { AuthRepositoryImpl(get()) }
}

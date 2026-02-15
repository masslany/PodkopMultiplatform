package pl.masslany.podkop.business.profile.data.network.di

import org.koin.dsl.module
import pl.masslany.podkop.business.profile.data.api.ProfileDataSource
import pl.masslany.podkop.business.profile.data.network.api.ProfileApi
import pl.masslany.podkop.business.profile.data.network.client.ProfileApiClient
import pl.masslany.podkop.business.profile.data.network.main.ProfileDataSourceImpl
val profileNetworkModule = module {
    single<ProfileApi> { ProfileApiClient(get()) }
    single<ProfileDataSource> { ProfileDataSourceImpl(get()) }
}

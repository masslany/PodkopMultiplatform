package pl.masslany.podkop.business.profile.data.di

import org.koin.dsl.module
import pl.masslany.podkop.business.profile.data.api.ProfileDataSource
import pl.masslany.podkop.business.profile.data.main.ProfileRepositoryImpl
import pl.masslany.podkop.business.profile.domain.main.ProfileRepository

val profileDataModule = module {
    factory<ProfileRepository> {
        ProfileRepositoryImpl(
            profileDataSource = get<ProfileDataSource>(),
            dispatcherProvider = get(),
        )
    }
}

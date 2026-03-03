package pl.masslany.podkop.business.profile.data.local.di

import org.koin.dsl.module
import pl.masslany.podkop.business.profile.data.local.api.ProfileLocalDataSource
import pl.masslany.podkop.business.profile.data.local.main.ProfileLocalDataSourceImpl

val profileLocalModule = module {
    single<ProfileLocalDataSource> { ProfileLocalDataSourceImpl() }
}

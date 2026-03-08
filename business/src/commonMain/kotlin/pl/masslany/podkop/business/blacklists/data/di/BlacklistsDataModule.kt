package pl.masslany.podkop.business.blacklists.data.di

import org.koin.dsl.module
import pl.masslany.podkop.business.blacklists.data.main.BlacklistsRepositoryImpl
import pl.masslany.podkop.business.blacklists.domain.main.BlacklistsRepository

val blacklistsDataModule = module {
    single<BlacklistsRepository> {
        BlacklistsRepositoryImpl(
            blacklistsDataSource = get(),
            dispatcherProvider = get(),
        )
    }
}

package pl.masslany.podkop.business.accountsettings.data.di

import org.koin.dsl.module
import pl.masslany.podkop.business.accountsettings.data.main.AccountSettingsRepositoryImpl
import pl.masslany.podkop.business.accountsettings.domain.main.AccountSettingsRepository

val accountSettingsDataModule = module {
    single<AccountSettingsRepository> {
        AccountSettingsRepositoryImpl(
            accountSettingsDataSource = get(),
            dispatcherProvider = get(),
        )
    }
}

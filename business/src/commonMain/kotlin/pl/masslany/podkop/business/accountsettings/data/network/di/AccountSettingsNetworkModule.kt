package pl.masslany.podkop.business.accountsettings.data.network.di

import org.koin.dsl.module
import pl.masslany.podkop.business.accountsettings.data.api.AccountSettingsDataSource
import pl.masslany.podkop.business.accountsettings.data.network.api.AccountSettingsApi
import pl.masslany.podkop.business.accountsettings.data.network.client.AccountSettingsApiClient
import pl.masslany.podkop.business.accountsettings.data.network.main.AccountSettingsDataSourceImpl

val accountSettingsNetworkModule = module {
    single<AccountSettingsApi> { AccountSettingsApiClient(apiClient = get()) }
    single<AccountSettingsDataSource> { AccountSettingsDataSourceImpl(accountSettingsApi = get()) }
}

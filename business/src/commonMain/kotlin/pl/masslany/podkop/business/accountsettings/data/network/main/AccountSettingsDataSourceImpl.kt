package pl.masslany.podkop.business.accountsettings.data.network.main

import pl.masslany.podkop.business.accountsettings.data.api.AccountSettingsDataSource
import pl.masslany.podkop.business.accountsettings.data.network.api.AccountSettingsApi

class AccountSettingsDataSourceImpl(
    private val accountSettingsApi: AccountSettingsApi,
) : AccountSettingsDataSource {
    override suspend fun getGeneralSettings() = accountSettingsApi.getGeneralSettings()
}

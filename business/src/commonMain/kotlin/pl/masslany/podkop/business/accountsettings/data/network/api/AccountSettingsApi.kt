package pl.masslany.podkop.business.accountsettings.data.network.api

import pl.masslany.podkop.business.accountsettings.data.network.models.GeneralSettingsResponseDto

interface AccountSettingsApi {
    suspend fun getGeneralSettings(): Result<GeneralSettingsResponseDto>
}

package pl.masslany.podkop.business.testsupport.fakes

import pl.masslany.podkop.business.accountsettings.data.api.AccountSettingsDataSource
import pl.masslany.podkop.business.accountsettings.data.network.models.GeneralSettingsResponseDto

class FakeAccountSettingsDataSource : AccountSettingsDataSource {
    var getGeneralSettingsResult: Result<GeneralSettingsResponseDto> =
        unstubbedResult("AccountSettingsDataSource.getGeneralSettings")

    var getGeneralSettingsCalls = 0

    override suspend fun getGeneralSettings(): Result<GeneralSettingsResponseDto> {
        getGeneralSettingsCalls += 1
        return getGeneralSettingsResult
    }
}

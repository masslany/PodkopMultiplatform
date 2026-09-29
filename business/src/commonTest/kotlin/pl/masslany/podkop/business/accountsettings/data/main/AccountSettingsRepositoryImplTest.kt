package pl.masslany.podkop.business.accountsettings.data.main

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import pl.masslany.podkop.business.accountsettings.data.network.models.GeneralSettingsDto
import pl.masslany.podkop.business.accountsettings.data.network.models.GeneralSettingsResponseDto
import pl.masslany.podkop.business.accountsettings.domain.models.AccountContentSettings
import pl.masslany.podkop.business.testsupport.fakes.FakeAccountSettingsDataSource
import pl.masslany.podkop.business.testsupport.fakes.FakeDispatcherProvider

class AccountSettingsRepositoryImplTest {
    @Test
    fun `get content settings maps show18`() = runBlocking {
        val dataSource = FakeAccountSettingsDataSource().apply {
            getGeneralSettingsResult = Result.success(GeneralSettingsResponseDto(GeneralSettingsDto(show18 = true)))
        }
        val sut = AccountSettingsRepositoryImpl(dataSource, FakeDispatcherProvider())

        assertEquals(AccountContentSettings(showAdult = true), sut.getContentSettings().getOrThrow())
        assertEquals(1, dataSource.getGeneralSettingsCalls)
    }

    @Test
    fun `get content settings keeps adult content off by default`() = runBlocking {
        val dataSource = FakeAccountSettingsDataSource().apply {
            getGeneralSettingsResult = Result.success(GeneralSettingsResponseDto(GeneralSettingsDto()))
        }
        val sut = AccountSettingsRepositoryImpl(dataSource, FakeDispatcherProvider())

        assertEquals(AccountContentSettings(showAdult = false), sut.getContentSettings().getOrThrow())
    }

    @Test
    fun `get content settings forwards failures`() = runBlocking {
        val dataSource = FakeAccountSettingsDataSource().apply {
            getGeneralSettingsResult = Result.failure(IllegalStateException("offline"))
        }
        val sut = AccountSettingsRepositoryImpl(dataSource, FakeDispatcherProvider())

        assertTrue(sut.getContentSettings().isFailure)
    }
}

package pl.masslany.podkop.business.blacklists.data.main

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import pl.masslany.podkop.business.testsupport.fakes.FakeBlacklistsDataSource
import pl.masslany.podkop.business.testsupport.fakes.FakeDispatcherProvider

class BlacklistsRepositoryImplTest {

    @Test
    fun `add blacklisted user forwards username`() = runBlocking {
        val dataSource = FakeBlacklistsDataSource().apply {
            addBlacklistedUserResult = Result.success(Unit)
        }
        val sut = BlacklistsRepositoryImpl(
            blacklistsDataSource = dataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )

        val actual = sut.addBlacklistedUser("alice")

        assertEquals(Unit, actual.getOrThrow())
        assertEquals(listOf("alice"), dataSource.addBlacklistedUserCalls)
    }

    @Test
    fun `remove blacklisted user forwards username`() = runBlocking {
        val dataSource = FakeBlacklistsDataSource().apply {
            removeBlacklistedUserResult = Result.success(Unit)
        }
        val sut = BlacklistsRepositoryImpl(
            blacklistsDataSource = dataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )

        val actual = sut.removeBlacklistedUser("alice")

        assertEquals(Unit, actual.getOrThrow())
        assertEquals(listOf("alice"), dataSource.removeBlacklistedUserCalls)
    }

    @Test
    fun `add blacklisted tag forwards tag`() = runBlocking {
        val dataSource = FakeBlacklistsDataSource().apply {
            addBlacklistedTagResult = Result.success(Unit)
        }
        val sut = BlacklistsRepositoryImpl(
            blacklistsDataSource = dataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )

        val actual = sut.addBlacklistedTag("kotlin")

        assertEquals(Unit, actual.getOrThrow())
        assertEquals(listOf("kotlin"), dataSource.addBlacklistedTagCalls)
    }

    @Test
    fun `remove blacklisted tag forwards tag`() = runBlocking {
        val dataSource = FakeBlacklistsDataSource().apply {
            removeBlacklistedTagResult = Result.success(Unit)
        }
        val sut = BlacklistsRepositoryImpl(
            blacklistsDataSource = dataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )

        val actual = sut.removeBlacklistedTag("kotlin")

        assertEquals(Unit, actual.getOrThrow())
        assertEquals(listOf("kotlin"), dataSource.removeBlacklistedTagCalls)
    }
}

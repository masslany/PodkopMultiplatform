package pl.masslany.podkop.business.blacklists.data.main

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import pl.masslany.podkop.business.blacklists.data.network.models.BlacklistedDomainDto
import pl.masslany.podkop.business.blacklists.data.network.models.BlacklistedDomainsResponseDto
import pl.masslany.podkop.business.blacklists.data.network.models.BlacklistedTagDto
import pl.masslany.podkop.business.blacklists.data.network.models.BlacklistedTagsResponseDto
import pl.masslany.podkop.business.blacklists.data.network.models.BlacklistedUserDto
import pl.masslany.podkop.business.blacklists.data.network.models.BlacklistedUsersResponseDto
import pl.masslany.podkop.business.common.data.network.models.common.PaginationDto
import pl.masslany.podkop.business.common.domain.models.common.Gender
import pl.masslany.podkop.business.common.domain.models.common.NameColor
import pl.masslany.podkop.business.testsupport.fakes.FakeBlacklistsDataSource
import pl.masslany.podkop.business.testsupport.fakes.FakeDispatcherProvider

class BlacklistsRepositoryImplTest {
    @Test
    fun `get blacklisted users forwards page and maps payload`() = runBlocking {
        val dataSource = FakeBlacklistsDataSource().apply {
            getBlacklistedUsersResult = Result.success(
                BlacklistedUsersResponseDto(
                    data = listOf(
                        BlacklistedUserDto(
                            username = "alice",
                            createdAt = "2026-03-22 11:20:22",
                            gender = "f",
                            color = "green",
                            avatar = "https://example.com/avatar.jpg",
                        ),
                    ),
                    pagination = PaginationDto(
                        perPage = 30,
                        total = 31,
                    ),
                ),
            )
        }
        val sut = BlacklistsRepositoryImpl(
            blacklistsDataSource = dataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )

        val actual = sut.getBlacklistedUsers(page = 1).getOrThrow()

        assertEquals(listOf(1), dataSource.getBlacklistedUsersCalls)
        assertEquals(1, actual.data.size)
        assertEquals("alice", actual.data.single().username)
        assertEquals(Gender.Female, actual.data.single().gender)
        assertEquals(NameColor.Green, actual.data.single().color)
        assertEquals("2", actual.pagination?.next)
    }

    @Test
    fun `get blacklisted tags forwards page and maps payload`() = runBlocking {
        val dataSource = FakeBlacklistsDataSource().apply {
            getBlacklistedTagsResult = Result.success(
                BlacklistedTagsResponseDto(
                    data = listOf(
                        BlacklistedTagDto(
                            name = "kotlin",
                            createdAt = "2026-03-22 11:20:22",
                        ),
                    ),
                    pagination = PaginationDto(
                        perPage = 30,
                        total = 30,
                    ),
                ),
            )
        }
        val sut = BlacklistsRepositoryImpl(
            blacklistsDataSource = dataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )

        val actual = sut.getBlacklistedTags(page = 1).getOrThrow()

        assertEquals(listOf(1), dataSource.getBlacklistedTagsCalls)
        assertEquals("kotlin", actual.data.single().name)
        assertEquals("", actual.pagination?.next)
    }

    @Test
    fun `get blacklisted domains forwards page and maps payload`() = runBlocking {
        val dataSource = FakeBlacklistsDataSource().apply {
            getBlacklistedDomainsResult = Result.success(
                BlacklistedDomainsResponseDto(
                    data = listOf(
                        BlacklistedDomainDto(
                            domain = "example.com",
                            createdAt = "2026-03-22 11:20:22",
                        ),
                    ),
                    pagination = PaginationDto(
                        perPage = 30,
                        total = 1,
                    ),
                ),
            )
        }
        val sut = BlacklistsRepositoryImpl(
            blacklistsDataSource = dataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )

        val actual = sut.getBlacklistedDomains(page = 1).getOrThrow()

        assertEquals(listOf(1), dataSource.getBlacklistedDomainsCalls)
        assertEquals("example.com", actual.data.single().domain)
        assertEquals("", actual.pagination?.next)
    }

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

    @Test
    fun `add blacklisted domain forwards domain`() = runBlocking {
        val dataSource = FakeBlacklistsDataSource().apply {
            addBlacklistedDomainResult = Result.success(Unit)
        }
        val sut = BlacklistsRepositoryImpl(
            blacklistsDataSource = dataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )

        val actual = sut.addBlacklistedDomain("example.com")

        assertEquals(Unit, actual.getOrThrow())
        assertEquals(listOf("example.com"), dataSource.addBlacklistedDomainCalls)
    }

    @Test
    fun `remove blacklisted domain forwards domain`() = runBlocking {
        val dataSource = FakeBlacklistsDataSource().apply {
            removeBlacklistedDomainResult = Result.success(Unit)
        }
        val sut = BlacklistsRepositoryImpl(
            blacklistsDataSource = dataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )

        val actual = sut.removeBlacklistedDomain("example.com")

        assertEquals(Unit, actual.getOrThrow())
        assertEquals(listOf("example.com"), dataSource.removeBlacklistedDomainCalls)
    }
}

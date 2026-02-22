package pl.masslany.podkop.business.profile.data.main

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.profile.data.main.ProfileRepositoryImpl
import pl.masslany.podkop.business.testsupport.fakes.FakeDispatcherProvider
import pl.masslany.podkop.business.testsupport.fakes.FakeProfileDataSource
import pl.masslany.podkop.business.testsupport.fixtures.BusinessFixtures as Fixtures

class ProfileRepositoryImplTest {

    @Test
    fun `get profile short maps dto`() = runBlocking {
        val profileDataSource = FakeProfileDataSource().apply {
            getProfileShortResult = Result.success(
                Fixtures.profileShortDto(
                    data = Fixtures.profileShortDataDto(username = "shorty", avatar = "avatar"),
                ),
            )
        }
        val sut = createSut(profileDataSource)

        val actual = sut.getProfileShort()

        assertEquals(1, profileDataSource.getProfileShortCalls)
        assertEquals(
            Fixtures.profileShort(name = "shorty", avatarUrl = "avatar"),
            actual.getOrThrow(),
        )
    }

    @Test
    fun `get profile without name delegates to no arg endpoint and maps dto`() = runBlocking {
        val profileDataSource = FakeProfileDataSource().apply {
            getProfileResult = Result.success(
                Fixtures.profileDto(
                    data = Fixtures.profileDataDto(username = "john", name = "John"),
                ),
            )
        }
        val sut = createSut(profileDataSource)

        val actual = sut.getProfile()

        assertEquals(1, profileDataSource.getProfileCalls)
        assertEquals("john", actual.getOrThrow().name)
    }

    @Test
    fun `get profile with name delegates to named endpoint and maps dto`() = runBlocking {
        val profileDataSource = FakeProfileDataSource().apply {
            getProfileByNameResult = Result.success(
                Fixtures.profileDto(
                    data = Fixtures.profileDataDto(username = "alice"),
                ),
            )
        }
        val sut = createSut(profileDataSource)

        val actual = sut.getProfile("alice")

        assertEquals(listOf("alice"), profileDataSource.getProfileByNameCalls)
        assertEquals("alice", actual.getOrThrow().name)
    }

    @Test
    fun `get users auto complete forwards query and maps response`() = runBlocking {
        val profileDataSource = FakeProfileDataSource().apply {
            getUsersAutoCompleteResult = Result.success(
                Fixtures.usersAutoCompleteResponseDto(
                    data = listOf(Fixtures.usersAutoCompleteDataDto(username = "u1")),
                ),
            )
        }
        val sut = createSut(profileDataSource)

        val actual = sut.getUsersAutoComplete("u")

        assertEquals(listOf("u"), profileDataSource.getUsersAutoCompleteCalls)
        assertEquals(Fixtures.usersAutoComplete(users = listOf(Fixtures.userAutoCompleteItem(username = "u1"))), actual.getOrThrow())
    }

    @Test
    fun `profile resource endpoints reuse shared mapping path`() = runBlocking {
        data class Case(
            val endpoint: FakeProfileDataSource.ResourceEndpoint,
            val invoke: suspend ProfileRepositoryImpl.() -> Result<Resources>,
        )

        val username = "tester"
        val page = 3
        val cases = listOf(
            Case(FakeProfileDataSource.ResourceEndpoint.Actions) { getProfileActions(username, page) },
            Case(FakeProfileDataSource.ResourceEndpoint.EntriesAdded) { getProfileEntriesAdded(username, page) },
            Case(FakeProfileDataSource.ResourceEndpoint.EntriesVoted) { getProfileEntriesVoted(username, page) },
            Case(FakeProfileDataSource.ResourceEndpoint.EntriesCommented) { getProfileEntriesCommented(username, page) },
            Case(FakeProfileDataSource.ResourceEndpoint.LinksAdded) { getProfileLinksAdded(username, page) },
            Case(FakeProfileDataSource.ResourceEndpoint.LinksPublished) { getProfileLinksPublished(username, page) },
            Case(FakeProfileDataSource.ResourceEndpoint.LinksUp) { getProfileLinksUp(username, page) },
            Case(FakeProfileDataSource.ResourceEndpoint.LinksDown) { getProfileLinksDown(username, page) },
            Case(FakeProfileDataSource.ResourceEndpoint.LinksCommented) { getProfileLinksCommented(username, page) },
            Case(FakeProfileDataSource.ResourceEndpoint.LinksRelated) { getProfileLinksRelated(username, page) },
        )

        for (case in cases) {
            val profileDataSource = FakeProfileDataSource().apply {
                resourceResult = Result.success(Fixtures.resourceResponseDto())
            }
            val sut = createSut(profileDataSource)

            val actual = case.invoke(sut)

            assertEquals(Fixtures.resources(), actual.getOrThrow())
            assertEquals(
                listOf(
                    FakeProfileDataSource.ResourceCall(
                        endpoint = case.endpoint,
                        username = username,
                        page = page,
                    ),
                ),
                profileDataSource.resourceCalls,
            )
        }
    }

    @Test
    fun `get profile observed tags maps response and forwards args`() = runBlocking {
        val profileDataSource = FakeProfileDataSource().apply {
            observedTagsResult = Result.success(
                Fixtures.observedTagsResponseDto(
                    data = listOf(Fixtures.observedTagDto(name = "#kotlin", pinned = true)),
                ),
            )
        }
        val sut = createSut(profileDataSource)

        val actual = sut.getProfileObservedTags(username = "john", page = 4)

        assertEquals(listOf("john" to 4), profileDataSource.getProfileObservedTagsCalls)
        assertEquals(
            Fixtures.observedTags(data = listOf(Fixtures.observedTag(name = "#kotlin", pinned = true))),
            actual.getOrThrow(),
        )
    }

    @Test
    fun `get profile observed users following maps response and forwards args`() = runBlocking {
        val profileDataSource = FakeProfileDataSource().apply {
            observedUsersFollowingResult = Result.success(
                Fixtures.observedUsersResponseDto(
                    data = listOf(Fixtures.userDto(username = "followed")),
                ),
            )
        }
        val sut = createSut(profileDataSource)

        val actual = sut.getProfileObservedUsersFollowing(username = "john", page = 5)

        assertEquals(
            listOf(FakeProfileDataSource.ObservedUsersCall(username = "john", page = 5)),
            profileDataSource.getProfileObservedUsersFollowingCalls,
        )
        assertEquals(
            Fixtures.observedUsers(data = listOf(Fixtures.observedUser(username = "followed"))),
            actual.getOrThrow(),
        )
    }

    @Test
    fun `get profile observed users followers maps response and forwards args`() = runBlocking {
        val profileDataSource = FakeProfileDataSource().apply {
            observedUsersFollowersResult = Result.success(
                Fixtures.observedUsersResponseDto(
                    data = listOf(Fixtures.userDto(username = "follower")),
                ),
            )
        }
        val sut = createSut(profileDataSource)

        val actual = sut.getProfileObservedUsersFollowers(username = "john", page = 6)

        assertEquals(
            listOf(FakeProfileDataSource.ObservedUsersCall(username = "john", page = 6)),
            profileDataSource.getProfileObservedUsersFollowersCalls,
        )
        assertEquals(
            Fixtures.observedUsers(data = listOf(Fixtures.observedUser(username = "follower"))),
            actual.getOrThrow(),
        )
    }

    @Test
    fun `profile resource endpoints propagate failure`() = runBlocking {
        val expected = IllegalStateException("failure")
        val profileDataSource = FakeProfileDataSource().apply {
            resourceResult = Result.failure(expected)
        }
        val sut = createSut(profileDataSource)

        val actual = sut.getProfileActions(username = "john", page = 1)

        assertTrue(actual.isFailure)
        assertSame(expected, actual.exceptionOrNull())
    }

    private fun createSut(
        profileDataSource: FakeProfileDataSource = FakeProfileDataSource(),
    ): ProfileRepositoryImpl {
        return ProfileRepositoryImpl(
            profileDataSource = profileDataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )
    }
}

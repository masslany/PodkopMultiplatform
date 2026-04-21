package pl.masslany.podkop.features.blacklists

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pl.masslany.podkop.business.blacklists.domain.models.BlacklistedDomain
import pl.masslany.podkop.business.blacklists.domain.models.BlacklistedDomains
import pl.masslany.podkop.business.blacklists.domain.models.BlacklistedTag
import pl.masslany.podkop.business.blacklists.domain.models.BlacklistedTags
import pl.masslany.podkop.business.blacklists.domain.models.BlacklistedUser
import pl.masslany.podkop.business.blacklists.domain.models.BlacklistedUsers
import pl.masslany.podkop.business.common.domain.models.common.Gender
import pl.masslany.podkop.business.common.domain.models.common.NameColor
import pl.masslany.podkop.business.common.domain.models.common.Pagination
import pl.masslany.podkop.business.tags.domain.models.TagsAutoComplete
import pl.masslany.podkop.business.tags.domain.models.TagsAutoCompleteItem
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.navigation.GenericDialog
import pl.masslany.podkop.common.preview.NoOpTopBarActions
import pl.masslany.podkop.features.blacklists.models.BlacklistCategoryState
import pl.masslany.podkop.features.blacklists.models.BlacklistCategoryType
import pl.masslany.podkop.features.blacklists.models.BlacklistEntryState
import pl.masslany.podkop.features.blacklists.models.BlacklistSuggestionsStatus
import pl.masslany.podkop.features.profile.ProfileScreen
import pl.masslany.podkop.features.tag.TagScreen
import pl.masslany.podkop.testsupport.fakes.FakeAppLogger
import pl.masslany.podkop.testsupport.fakes.FakeBlacklistsRepository
import pl.masslany.podkop.testsupport.fakes.FakeProfileRepository
import pl.masslany.podkop.testsupport.fakes.FakeSnackbarManager
import pl.masslany.podkop.testsupport.fakes.FakeTagsRepository
import pl.masslany.podkop.testsupport.navigation.createTestAppNavigator

@OptIn(ExperimentalCoroutinesApi::class)
class BlacklistsViewModelTest {

    @Test
    fun `init loads first page for all categories`() = runBlacklistsViewModelTest {
        val blacklistsRepository = FakeBlacklistsRepository().apply {
            getBlacklistedUsersHandler = { page ->
                Result.success(
                    blacklistedUsersPage(
                        page = page,
                        users = listOf(blacklistedUser(username = "user1")),
                        total = 1,
                    ),
                )
            }
            getBlacklistedTagsHandler = { page ->
                Result.success(
                    blacklistedTagsPage(
                        page = page,
                        tags = listOf(blacklistedTag(name = "heheszki")),
                        total = 1,
                    ),
                )
            }
            getBlacklistedDomainsHandler = { page ->
                Result.success(
                    blacklistedDomainsPage(
                        page = page,
                        domains = listOf(blacklistedDomain(domain = "masslany.pl")),
                        total = 1,
                    ),
                )
            }
        }

        val sut = createSut(blacklistsRepository = blacklistsRepository)
        collectState(sut)

        advanceUntilIdle()

        assertEquals(listOf(1), blacklistsRepository.getBlacklistedUsersCalls)
        assertEquals(listOf(1), blacklistsRepository.getBlacklistedTagsCalls)
        assertEquals(listOf(1), blacklistsRepository.getBlacklistedDomainsCalls)
        assertEquals(
            listOf("user1"),
            sut.categoryState(BlacklistCategoryType.Users).items.map {
                (it as BlacklistEntryState.BlacklistedUserItemState).username
            },
        )
        assertEquals(
            listOf("heheszki"),
            sut.categoryState(BlacklistCategoryType.Tags).items.map {
                (it as BlacklistEntryState.BlacklistedTagItemState).name
            },
        )
        assertEquals(1, sut.categoryState(BlacklistCategoryType.Domains).totalCount)
    }

    @Test
    fun `entry click opens profile and tag destinations`() = runBlacklistsViewModelTest {
        val appNavigator = createTestAppNavigator(backgroundScope)
        val sut = createSut(
            blacklistsRepository = FakeBlacklistsRepository.withEmptyPages(),
            appNavigator = appNavigator,
        )

        collectState(sut)
        advanceUntilIdle()

        sut.onEntryClicked(
            BlacklistEntryState.BlacklistedUserItemState(
                username = "user1",
                avatarState = blacklistedUser(username = "user1").toItemState().avatarState,
                nameColorType = blacklistedUser(username = "user1").toItemState().nameColorType,
            ),
        )
        sut.onEntryClicked(BlacklistEntryState.BlacklistedTagItemState(name = "android"))

        assertEquals(ProfileScreen(username = "user1"), appNavigator.state.value.rootStack[1])
        assertEquals(TagScreen(tag = "android"), appNavigator.state.value.rootStack[2])
    }

    @Test
    fun `confirmed remove deletes loaded tag locally without refreshing`() = runBlacklistsViewModelTest {
        val blacklistsRepository = FakeBlacklistsRepository().apply {
            getBlacklistedUsersHandler =
                { page -> Result.success(blacklistedUsersPage(page = page, users = emptyList())) }
            getBlacklistedDomainsHandler = { page ->
                Result.success(blacklistedDomainsPage(page = page, domains = emptyList()))
            }
            getBlacklistedTagsHandler = { page ->
                Result.success(
                    when (page) {
                        1 -> blacklistedTagsPage(
                            page = page,
                            tags = listOf(
                                blacklistedTag(name = "java"),
                                blacklistedTag(name = "kotlin"),
                            ),
                            total = 3,
                            next = "2",
                        )

                        2 -> blacklistedTagsPage(
                            page = page,
                            tags = listOf(blacklistedTag(name = "android")),
                            total = 3,
                        )

                        else -> error("Unexpected page $page")
                    },
                )
            }
            removeBlacklistedTagResult = Result.success(Unit)
        }
        val appNavigator = createTestAppNavigator(backgroundScope)
        val sut = createSut(
            blacklistsRepository = blacklistsRepository,
            appNavigator = appNavigator,
        )

        collectState(sut)
        advanceUntilIdle()

        sut.onCategorySelected(BlacklistCategoryType.Tags)
        advanceUntilIdle()
        sut.paginate()
        advanceUntilIdle()

        val itemToRemove = sut.categoryState(BlacklistCategoryType.Tags)
            .items
            .last() as BlacklistEntryState.BlacklistedTagItemState

        sut.onRemoveClicked(itemToRemove)
        advanceUntilIdle()

        val dialog = appNavigator.state.value.rootStack.last() as GenericDialog
        appNavigator.publishResult(dialog.key, true)
        advanceUntilIdle()

        assertEquals(listOf("android"), blacklistsRepository.removeBlacklistedTagCalls)
        assertEquals(
            listOf("java", "kotlin"),
            sut.categoryState(BlacklistCategoryType.Tags).items.map {
                (it as BlacklistEntryState.BlacklistedTagItemState).name
            },
        )
        assertEquals(2, sut.categoryState(BlacklistCategoryType.Tags).totalCount)
        assertEquals(listOf(1, 2), blacklistsRepository.getBlacklistedTagsCalls)
    }

    @Test
    fun `add clicked normalizes domain clears input and refreshes category`() = runBlacklistsViewModelTest {
        val blacklistsRepository = FakeBlacklistsRepository().apply {
            getBlacklistedUsersHandler =
                { page -> Result.success(blacklistedUsersPage(page = page, users = emptyList())) }
            getBlacklistedTagsHandler = { page -> Result.success(blacklistedTagsPage(page = page, tags = emptyList())) }
            getBlacklistedDomainsHandler = { page ->
                Result.success(
                    if (getBlacklistedDomainsCalls.size <= 1) {
                        blacklistedDomainsPage(
                            page = page,
                            domains = listOf(blacklistedDomain(domain = "onet.pl")),
                            total = 1,
                        )
                    } else {
                        blacklistedDomainsPage(
                            page = page,
                            domains = listOf(
                                blacklistedDomain(domain = "onet.pl"),
                                blacklistedDomain(domain = "masslany.pl"),
                            ),
                            total = 2,
                        )
                    },
                )
            }
            addBlacklistedDomainResult = Result.success(Unit)
        }

        val sut = createSut(blacklistsRepository = blacklistsRepository)
        collectState(sut)
        advanceUntilIdle()

        sut.onCategorySelected(BlacklistCategoryType.Domains)
        advanceUntilIdle()
        sut.onAddInputChanged(" maSslaNy.pl ")
        advanceUntilIdle()
        sut.onAddClicked()
        advanceUntilIdle()

        assertEquals(listOf("masslany.pl"), blacklistsRepository.addBlacklistedDomainCalls)
        assertEquals(listOf(1, 1), blacklistsRepository.getBlacklistedDomainsCalls)
        assertEquals("", sut.categoryState(BlacklistCategoryType.Domains).addInput)
        assertEquals(
            listOf("onet.pl", "masslany.pl"),
            sut.categoryState(BlacklistCategoryType.Domains).items.map { it.displayLabel },
        )
    }

    @Test
    fun `tag suggestions load after debounce and skip already blacklisted entries`() = runBlacklistsViewModelTest {
        val blacklistsRepository = FakeBlacklistsRepository().apply {
            getBlacklistedUsersHandler =
                { page -> Result.success(blacklistedUsersPage(page = page, users = emptyList())) }
            getBlacklistedDomainsHandler = { page ->
                Result.success(blacklistedDomainsPage(page = page, domains = emptyList()))
            }
            getBlacklistedTagsHandler = { page ->
                Result.success(
                    blacklistedTagsPage(
                        page = page,
                        tags = listOf(blacklistedTag(name = "java")),
                        total = 1,
                    ),
                )
            }
        }
        val tagsRepository = FakeTagsRepository().apply {
            getAutoCompleteTagsHandler = { query ->
                Result.success(
                    TagsAutoComplete(
                        tags = listOf(
                            TagsAutoCompleteItem(name = "java", observedQuantity = 10),
                            TagsAutoCompleteItem(name = "kotlin", observedQuantity = 20),
                            TagsAutoCompleteItem(name = "android", observedQuantity = 30),
                        ),
                    ),
                )
            }
        }

        val sut = createSut(
            blacklistsRepository = blacklistsRepository,
            tagsRepository = tagsRepository,
        )
        collectState(sut)
        advanceUntilIdle()

        sut.onCategorySelected(BlacklistCategoryType.Tags)
        advanceUntilIdle()
        sut.onAddInputChanged("#kot")
        advanceTimeBy(300)
        advanceUntilIdle()

        val suggestions = sut.categoryState(BlacklistCategoryType.Tags).suggestions

        assertEquals(listOf("kot"), tagsRepository.getAutoCompleteTagsCalls)
        assertEquals(BlacklistSuggestionsStatus.Content, suggestions.status)
        assertEquals(listOf("kotlin", "android"), suggestions.items.map { it.key.removePrefix("tag:") })
    }

    @Test
    fun `failed initial load shows error state and snackbar`() = runBlacklistsViewModelTest {
        val blacklistsRepository = FakeBlacklistsRepository().apply {
            getBlacklistedUsersHandler = { Result.failure(IllegalStateException("boom")) }
            getBlacklistedTagsHandler = { page -> Result.success(blacklistedTagsPage(page = page, tags = emptyList())) }
            getBlacklistedDomainsHandler = { page ->
                Result.success(blacklistedDomainsPage(page = page, domains = emptyList()))
            }
        }
        val snackbarManager = FakeSnackbarManager()

        val sut = createSut(
            blacklistsRepository = blacklistsRepository,
            snackbarManager = snackbarManager,
        )
        collectState(sut)
        advanceUntilIdle()

        assertTrue(sut.categoryState(BlacklistCategoryType.Users).isError)
        assertFalse(sut.categoryState(BlacklistCategoryType.Tags).isError)
        assertEquals(1, snackbarManager.emittedEvents.size)
    }

    private fun TestScope.createSut(
        blacklistsRepository: FakeBlacklistsRepository = FakeBlacklistsRepository.withEmptyPages(),
        profileRepository: FakeProfileRepository = FakeProfileRepository(),
        tagsRepository: FakeTagsRepository = FakeTagsRepository(),
        appNavigator: AppNavigator = createTestAppNavigator(backgroundScope),
        logger: FakeAppLogger = FakeAppLogger(),
        snackbarManager: FakeSnackbarManager = FakeSnackbarManager(),
    ): BlacklistsViewModel = BlacklistsViewModel(
        blacklistsRepository = blacklistsRepository,
        profileRepository = profileRepository,
        tagsRepository = tagsRepository,
        appNavigator = appNavigator,
        logger = logger,
        snackbarManager = snackbarManager,
        topBarActions = NoOpTopBarActions,
    )

    private fun TestScope.collectState(sut: BlacklistsViewModel) {
        backgroundScope.launch {
            sut.state.collect { /* keep upstream state active */ }
        }
    }

    private fun BlacklistsViewModel.categoryState(type: BlacklistCategoryType): BlacklistCategoryState =
        state.value.categories.first { it.type == type }

    private fun runBlacklistsViewModelTest(block: suspend TestScope.() -> Unit) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            block()
        } finally {
            Dispatchers.resetMain()
        }
    }
}

private fun blacklistedUser(username: String) = BlacklistedUser(
    username = username,
    createdAt = "2026-03-22 11:20:22",
    gender = Gender.Male,
    color = NameColor.Orange,
    avatarUrl = "https://wykop.pl/$username.jpg",
)

private fun blacklistedTag(name: String) = BlacklistedTag(
    name = name,
    createdAt = "2026-03-22 11:20:22",
)

private fun blacklistedDomain(domain: String) = BlacklistedDomain(
    domain = domain,
    createdAt = "2026-03-22 11:20:22",
)

private fun blacklistedUsersPage(
    page: Int,
    users: List<BlacklistedUser>,
    total: Int = users.size,
    next: String = "",
) = BlacklistedUsers(
    data = users,
    pagination = pagination(page = page, total = total, next = next),
)

private fun blacklistedTagsPage(
    page: Int,
    tags: List<BlacklistedTag>,
    total: Int = tags.size,
    next: String = "",
) = BlacklistedTags(
    data = tags,
    pagination = pagination(page = page, total = total, next = next),
)

private fun blacklistedDomainsPage(
    page: Int,
    domains: List<BlacklistedDomain>,
    total: Int = domains.size,
    next: String = "",
) = BlacklistedDomains(
    data = domains,
    pagination = pagination(page = page, total = total, next = next),
)

private fun pagination(page: Int, total: Int, next: String) = Pagination(
    perPage = 30,
    total = total,
    next = next,
    prev = if (page > 1) (page - 1).toString() else "",
)

package pl.masslany.podkop.features.home

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pl.masslany.podkop.common.navigation.NavTarget
import pl.masslany.podkop.common.navigation.NavigationConfigProvider
import pl.masslany.podkop.common.navigation.TopLevelDestination
import pl.masslany.podkop.features.entries.EntriesScreen
import pl.masslany.podkop.features.entrydetails.EntryDetailsScreen
import pl.masslany.podkop.features.linkdetails.LinkDetailsScreen
import pl.masslany.podkop.features.links.LinksScreen
import pl.masslany.podkop.features.upcoming.UpcomingScreen
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.ic_home
import podkop.composeapp.generated.resources.navigation_label_homepage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeNavigatorTest {

    @Test
    fun `initial state selects the first destination and creates root stacks`() = runHomeNavigatorTest {
        val sut = createNavigator(
            destinations = persistentListOf(
                topLevelDestination(LinksScreen),
                topLevelDestination(UpcomingScreen),
                topLevelDestination(EntriesScreen),
            ),
        )
        try {
            advanceUntilIdle()

            assertEquals(LinksScreen, sut.state.value.currentTabRoot)
            assertEquals(
                persistentListOf(LinksScreen),
                sut.currentStack(),
            )
        } finally {
            sut.close()
        }
    }

    @Test
    fun `inline details navigation replaces previous inline destination on the current tab`() = runHomeNavigatorTest {
        val sut = createNavigator(
            destinations = persistentListOf(
                topLevelDestination(LinksScreen),
                topLevelDestination(EntriesScreen),
            ),
        )
        try {
            advanceUntilIdle()
            sut.navigateToLinkDetails(11)
            sut.navigateToLinkDetails(22)

            assertEquals(
                persistentListOf<NavTarget>(LinksScreen, LinkDetailsScreen(22)),
                sut.currentStack(),
            )
        } finally {
            sut.close()
        }
    }

    @Test
    fun `detachCurrentInlineDetailsDestination removes it from the active stack`() = runHomeNavigatorTest {
        val sut = createNavigator(
            destinations = persistentListOf(
                topLevelDestination(EntriesScreen),
                topLevelDestination(UpcomingScreen),
            ),
        )
        try {
            advanceUntilIdle()
            sut.navigateToEntryDetails(EntryDetailsScreen.forEntry(44))

            val detached = sut.detachCurrentInlineDetailsDestination()

            assertEquals(EntryDetailsScreen.forEntry(44), detached)
            assertEquals(
                persistentListOf<NavTarget>(EntriesScreen),
                sut.currentStack(),
            )
        } finally {
            sut.close()
        }
    }

    @Test
    fun `back on a root tab switches to the first tab before exiting home`() = runHomeNavigatorTest {
        val sut = createNavigator(
            destinations = persistentListOf(
                topLevelDestination(LinksScreen),
                topLevelDestination(UpcomingScreen),
            ),
        )
        try {
            advanceUntilIdle()
            sut.onTabChanged(UpcomingScreen)

            val consumed = sut.onBack()

            assertTrue(consumed)
            assertEquals(LinksScreen, sut.state.value.currentTabRoot)
            assertEquals(
                persistentListOf<NavTarget>(LinksScreen),
                sut.currentStack(),
            )
        } finally {
            sut.close()
        }
    }

    @Test
    fun `back returns false when already on the first tab root`() = runHomeNavigatorTest {
        val sut = createNavigator(
            destinations = persistentListOf(
                topLevelDestination(LinksScreen),
                topLevelDestination(UpcomingScreen),
            ),
        )
        try {
            advanceUntilIdle()

            assertFalse(sut.onBack())
        } finally {
            sut.close()
        }
    }

    @Test
    fun `restored state keeps the selected tab and inline stack after destinations load`() = runHomeNavigatorTest {
        val sut = createNavigator(
            destinations = persistentListOf(
                topLevelDestination(LinksScreen),
                topLevelDestination(UpcomingScreen),
                topLevelDestination(EntriesScreen),
            ),
        )
        val serializedState = HomeNavigatorStateSerializer.serialize(
            HomeNavigatorState(
                currentTabRoot = EntriesScreen,
                stacks = persistentMapOf(
                    LinksScreen to persistentListOf(LinksScreen),
                    UpcomingScreen to persistentListOf(UpcomingScreen),
                    EntriesScreen to persistentListOf(
                        EntriesScreen,
                        EntryDetailsScreen.forEntry(44),
                    ),
                ),
            ),
        )
        try {
            sut.restoreState(serializedState)
            advanceUntilIdle()

            assertEquals(EntriesScreen, sut.state.value.currentTabRoot)
            assertEquals(
                persistentListOf(
                    EntriesScreen,
                    EntryDetailsScreen.forEntry(44),
                ),
                sut.currentStack(),
            )
        } finally {
            sut.close()
        }
    }
}

private fun createNavigator(
    destinations: ImmutableList<TopLevelDestination>,
): HomeNavigator = HomeNavigator(
    configProvider = FakeNavigationConfigProvider(destinations = destinations),
)

private fun topLevelDestination(root: NavTarget): TopLevelDestination = TopLevelDestination(
    root = root,
    iconRes = Res.drawable.ic_home,
    labelRes = Res.string.navigation_label_homepage,
)

private class FakeNavigationConfigProvider(destinations: ImmutableList<TopLevelDestination>) :
    NavigationConfigProvider {
    private val destinationsFlow = MutableStateFlow(destinations)

    override suspend fun resolveStartDestination(): NavTarget = LinksScreen

    override val topLevelDestinations: Flow<ImmutableList<TopLevelDestination>> = destinationsFlow
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun runHomeNavigatorTest(block: suspend TestScope.() -> Unit) {
    val dispatcher = StandardTestDispatcher()
    Dispatchers.setMain(dispatcher)
    try {
        runTest(dispatcher, testBody = block)
    } finally {
        Dispatchers.resetMain()
    }
}

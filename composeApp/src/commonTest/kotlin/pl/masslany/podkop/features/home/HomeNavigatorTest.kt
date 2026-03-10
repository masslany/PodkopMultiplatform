package pl.masslany.podkop.features.home

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
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

class HomeNavigatorTest {

    @Test
    fun `initial state selects the first destination and creates root stacks`() = runBlocking {
        val sut = createNavigator(
            destinations = persistentListOf(
                topLevelDestination(LinksScreen),
                topLevelDestination(UpcomingScreen),
                topLevelDestination(EntriesScreen),
            ),
        )

        yield()

        assertEquals(LinksScreen, sut.state.value.currentTabRoot)
        assertEquals(
            persistentListOf(LinksScreen),
            sut.currentStack(),
        )
    }

    @Test
    fun `inline details navigation replaces previous inline destination on the current tab`() = runBlocking {
        val sut = createNavigator(
            destinations = persistentListOf(
                topLevelDestination(LinksScreen),
                topLevelDestination(EntriesScreen),
            ),
        )

        yield()
        sut.navigateToLinkDetails(11)
        sut.navigateToLinkDetails(22)

        assertEquals(
            persistentListOf<NavTarget>(LinksScreen, LinkDetailsScreen(22)),
            sut.currentStack(),
        )
    }

    @Test
    fun `detachCurrentInlineDetailsDestination removes it from the active stack`() = runBlocking {
        val sut = createNavigator(
            destinations = persistentListOf(
                topLevelDestination(EntriesScreen),
                topLevelDestination(UpcomingScreen),
            ),
        )

        yield()
        sut.navigateToEntryDetails(EntryDetailsScreen.forEntry(44))

        val detached = sut.detachCurrentInlineDetailsDestination()

        assertEquals(EntryDetailsScreen.forEntry(44), detached)
        assertEquals(
            persistentListOf<NavTarget>(EntriesScreen),
            sut.currentStack(),
        )
    }

    @Test
    fun `back on a root tab switches to the first tab before exiting home`() = runBlocking {
        val sut = createNavigator(
            destinations = persistentListOf(
                topLevelDestination(LinksScreen),
                topLevelDestination(UpcomingScreen),
            ),
        )

        yield()
        sut.onTabChanged(UpcomingScreen)

        val consumed = sut.onBack()

        assertTrue(consumed)
        assertEquals(LinksScreen, sut.state.value.currentTabRoot)
        assertEquals(
            persistentListOf<NavTarget>(LinksScreen),
            sut.currentStack(),
        )
    }

    @Test
    fun `back returns false when already on the first tab root`() = runBlocking {
        val sut = createNavigator(
            destinations = persistentListOf(
                topLevelDestination(LinksScreen),
                topLevelDestination(UpcomingScreen),
            ),
        )

        yield()

        assertFalse(sut.onBack())
    }

    @Test
    fun `restored state keeps the selected tab and inline stack after destinations load`() = runBlocking {
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

        sut.restoreState(serializedState)
        yield()

        assertEquals(EntriesScreen, sut.state.value.currentTabRoot)
        assertEquals(
            persistentListOf(
                EntriesScreen,
                EntryDetailsScreen.forEntry(44),
            ),
            sut.currentStack(),
        )
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

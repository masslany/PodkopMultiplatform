package pl.masslany.podkop.common.navigation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pl.masslany.podkop.features.entries.EntriesScreen
import pl.masslany.podkop.features.links.LinksScreen
import pl.masslany.podkop.features.upcoming.UpcomingScreen
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.ic_home
import podkop.composeapp.generated.resources.ic_nav_letter_m
import podkop.composeapp.generated.resources.ic_nav_shovel
import podkop.composeapp.generated.resources.navigation_label_entries
import podkop.composeapp.generated.resources.navigation_label_homepage
import podkop.composeapp.generated.resources.navigation_label_upcoming

class AppConfigProvider : NavigationConfigProvider {

    override suspend fun resolveStartDestination(): NavTarget = MainApp

    override val topLevelDestinations: Flow<ImmutableList<TopLevelDestination>> = flow {
        val links = TopLevelDestination(
            root = LinksScreen,
            iconRes = Res.drawable.ic_home,
            labelRes = Res.string.navigation_label_homepage,
            enabled = true,
        )

        val upcoming = TopLevelDestination(
            root = UpcomingScreen,
            iconRes = Res.drawable.ic_nav_shovel,
            labelRes = Res.string.navigation_label_upcoming,
            enabled = true,
        )

        val entries = TopLevelDestination(
            root = EntriesScreen,
            iconRes = Res.drawable.ic_nav_letter_m,
            labelRes = Res.string.navigation_label_entries,
            enabled = true,
        )

        emit(
            persistentListOf(links, upcoming, entries),
        )
    }
}

package pl.masslany.podkop.common.navigation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pl.masslany.podkop.features.links.LinksScreen
import pl.masslany.podkop.features.upcoming.UpcomingScreen
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.home
import podkop.composeapp.generated.resources.ic_nav_letter_m
import podkop.composeapp.generated.resources.ic_nav_shovel
import podkop.composeapp.generated.resources.upcoming

class AppConfigProvider(
) : NavigationConfigProvider {

    override suspend fun resolveStartDestination(): NavTarget {
        return MainApp
    }

    override val topLevelDestinations: Flow<ImmutableList<TopLevelDestination>> = flow {
        val links = TopLevelDestination(
            root = LinksScreen,
            iconRes = Res.drawable.ic_nav_shovel,
            labelRes = Res.string.home,
            enabled = true,
        )

        val upcoming = TopLevelDestination(
            root = UpcomingScreen,
            iconRes = Res.drawable.ic_nav_letter_m,
            labelRes = Res.string.upcoming,
            enabled = true,
        )

        emit(
            persistentListOf(links, upcoming)
        )
    }

}
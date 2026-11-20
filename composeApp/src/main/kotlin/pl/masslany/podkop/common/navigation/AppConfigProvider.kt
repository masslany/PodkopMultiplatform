package pl.masslany.podkop.common.navigation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pl.masslany.podkop.business.notifications.domain.main.NotificationsRepository
import pl.masslany.podkop.composeapp.R
import pl.masslany.podkop.features.entries.EntriesScreen
import pl.masslany.podkop.features.links.LinksScreen
import pl.masslany.podkop.features.more.MoreScreen
import pl.masslany.podkop.features.upcoming.UpcomingScreen

class AppConfigProvider(private val notificationsRepository: NotificationsRepository) : NavigationConfigProvider {

    override suspend fun resolveStartDestination(): NavTarget = HomeScreen

    override val topLevelDestinations: Flow<ImmutableList<TopLevelDestination>> =
        notificationsRepository.unreadCount.map { notificationsUnreadCount ->
            persistentListOf(
                TopLevelDestination(
                    root = LinksScreen,
                    iconRes = R.drawable.ic_home,
                    labelRes = R.string.navigation_label_homepage,
                    enabled = true,
                ),
                TopLevelDestination(
                    root = UpcomingScreen,
                    iconRes = R.drawable.ic_nav_shovel,
                    labelRes = R.string.navigation_label_upcoming,
                    enabled = true,
                ),
                TopLevelDestination(
                    root = EntriesScreen,
                    iconRes = R.drawable.ic_nav_letter_m,
                    labelRes = R.string.navigation_label_entries,
                    enabled = true,
                ),
                TopLevelDestination(
                    root = MoreScreen,
                    iconRes = R.drawable.ic_more_horizontal,
                    labelRes = R.string.navigation_label_more,
                    enabled = true,
                ),
            )
        }
}

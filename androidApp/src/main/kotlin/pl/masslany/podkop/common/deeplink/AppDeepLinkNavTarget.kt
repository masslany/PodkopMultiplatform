package pl.masslany.podkop.common.deeplink

import pl.masslany.podkop.common.navigation.NavTarget
import pl.masslany.podkop.features.entrydetails.EntryDetailsScreen
import pl.masslany.podkop.features.linkdetails.LinkDetailsScreen
import pl.masslany.podkop.features.privatemessages.PrivateMessagesScreen
import pl.masslany.podkop.features.profile.ProfileScreen
import pl.masslany.podkop.features.tag.TagScreen

/** The screen a deep link opens, or null for links that carry data instead, like login callbacks. */
fun AppDeepLink.toNavTarget(): NavTarget? = when (this) {
    is AppDeepLink.LoginCallback -> null
    is AppDeepLink.LinkDetails -> LinkDetailsScreen(id = id)
    is AppDeepLink.EntryDetails -> EntryDetailsScreen.forEntry(id = id)
    is AppDeepLink.Profile -> ProfileScreen(username = username)
    is AppDeepLink.Tag -> TagScreen(tag = name)
    AppDeepLink.PrivateMessagesInbox -> PrivateMessagesScreen
}

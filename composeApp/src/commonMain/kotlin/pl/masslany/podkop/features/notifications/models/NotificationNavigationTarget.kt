package pl.masslany.podkop.features.notifications.models

sealed interface NotificationNavigationTarget {
    data class Link(val id: Int) : NotificationNavigationTarget

    data class Entry(val id: Int) : NotificationNavigationTarget

    data class Profile(val username: String) : NotificationNavigationTarget

    data class Tag(val name: String) : NotificationNavigationTarget

    data class External(val url: String) : NotificationNavigationTarget

    data object None : NotificationNavigationTarget
}

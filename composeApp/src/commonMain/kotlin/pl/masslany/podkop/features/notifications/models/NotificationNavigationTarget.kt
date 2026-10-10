package pl.masslany.podkop.features.notifications.models

sealed interface NotificationNavigationTarget {
    data class Link(val id: Int) : NotificationNavigationTarget

    data class Entry(val id: Int) : NotificationNavigationTarget

    data class Conversation(val username: String) : NotificationNavigationTarget

    data class Profile(val username: String) : NotificationNavigationTarget

    /** [content] narrows the tag to what the notification group is about, as website does. */
    data class Tag(
        val name: String,
        val content: GroupedTagContentType? = null,
    ) : NotificationNavigationTarget

    data class External(val url: String) : NotificationNavigationTarget

    data object None : NotificationNavigationTarget
}

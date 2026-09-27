package pl.masslany.podkop.business.notifications.domain.models

/** Where opening a notification leads. Shared by Android and iOS presentation. */
sealed interface NotificationTarget {
    data class Link(val id: Int) : NotificationTarget
    data class Entry(val id: Int) : NotificationTarget
    data class Conversation(val username: String) : NotificationTarget
    data class Profile(val username: String) : NotificationTarget
    data class Tag(val name: String) : NotificationTarget
    data class External(val url: String) : NotificationTarget
    data object None : NotificationTarget
}

fun NotificationItem.target(): NotificationTarget {
    if (group == NotificationGroup.PrivateMessages) {
        return NotificationTarget.Conversation(username = actor?.username.orEmpty().ifBlank { id })
    }
    linkId?.let { return NotificationTarget.Link(it) }
    entryId?.let { return NotificationTarget.Entry(it) }
    profileUsername?.takeIf(String::isNotBlank)?.let { return NotificationTarget.Profile(it) }
    tagName?.takeIf(String::isNotBlank)?.let { return NotificationTarget.Tag(it) }
    url?.takeIf(String::isNotBlank)?.let { return NotificationTarget.External(it) }
    return NotificationTarget.None
}

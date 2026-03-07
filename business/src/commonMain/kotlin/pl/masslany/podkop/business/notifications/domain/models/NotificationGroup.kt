package pl.masslany.podkop.business.notifications.domain.models

enum class NotificationGroup(val pathSegment: String) {
    Entries(pathSegment = "entries"),
    PrivateMessages(pathSegment = "pm"),
    Tags(pathSegment = "tags"),
    ObservedDiscussions(pathSegment = "observed-discussions"),
}

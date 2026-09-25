package pl.masslany.podkop.ios

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.business.notifications.domain.models.NotificationItem
import pl.masslany.podkop.business.notifications.domain.models.NotificationTarget
import pl.masslany.podkop.business.notifications.domain.models.NotificationsStatus
import pl.masslany.podkop.business.notifications.domain.models.target
import pl.masslany.podkop.business.privatemessages.domain.main.PrivateMessagesRepository
import pl.masslany.podkop.business.privatemessages.domain.models.PrivateMessage
import pl.masslany.podkop.common.pagination.PageRequest
import pl.masslany.podkop.common.pagination.PaginationMode
import pl.masslany.podkop.common.pagination.initialRequest

/**
 * One notification row. [targetKind] is link, entry, conversation, profile, tag, external or none;
 * [targetValue] carries the id, username, tag or URL for that kind.
 */
class IOSNotification(
    val id: String,
    val group: String,
    val isRead: Boolean,
    val groupId: String?,
    val groupCount: Int,
    val createdAt: String,
    val createdAtEpochMillis: Long,
    val actorUsername: String?,
    val actorColor: String?,
    val actorGender: String?,
    val message: String?,
    val issueTitle: String?,
    val badgeName: String?,
    val tagName: String?,
    val linkId: Int?,
    val linkTitle: String?,
    val entryId: Int?,
    val entryContent: String?,
    val targetKind: String,
    val targetValue: String?,
)
class IOSNotificationPage(val items: List<IOSNotification>, val next: IOSPageRequest?, val total: Int?)

class IOSConversation(
    val username: String,
    val color: String,
    val gender: String,
    val lastMessage: String?,
    val lastMessageAt: String,
    val unread: Boolean,
)
class IOSConversationPage(val items: List<IOSConversation>, val next: IOSPageRequest?, val total: Int?)

class IOSPrivateMessage(
    val key: String,
    val content: String?,
    val createdAt: String,
    /** Device-zone epoch used to order messages exactly as Android's merge does. */
    val createdAtEpochMillis: Long,
    val incoming: Boolean,
    val adult: Boolean,
    val senderUsername: String?,
    val senderColor: String?,
    val photo: IOSPhoto?,
    val embedUrl: String?,
)
class IOSMessagePage(val items: List<IOSPrivateMessage>, val next: IOSPageRequest?, val total: Int?)

class MessagesService internal constructor(private val client: PodkopClient) {
    private val repository: PrivateMessagesRepository = client.koin.get()
    private val authRepository: AuthRepository = client.koin.get()

    /** Android trims and drops a leading `@` before looking up or opening a conversation. */
    fun normalizeUsername(value: String): String = value.trim().removePrefix("@")

    fun firstRequest(): IOSPageRequest = PaginationMode.Numbered.initialRequest().toIOS()

    fun conversations(
        request: IOSPageRequest,
        loaded: Int,
        completion: (IOSConversationPage?, IOSFailure?) -> Unit,
    ): IOSOperation = client.operation(completion) {
        requireAccount()
        val page = request.toDomain()
        val result = repository.getConversations(page.number()).getOrThrow()
        IOSConversationPage(
            items = result.data.map {
                IOSConversation(
                    username = it.username,
                    color = it.nameColor.toIOS(),
                    gender = it.gender.toIOS(),
                    lastMessage = it.lastMessageContent,
                    lastMessageAt = it.lastMessageCreatedAt.toString(),
                    unread = it.unread,
                )
            },
            next = result.nextAfter(PaginationMode.Numbered, page, loaded),
            total = result.pagination?.total,
        )
    }

    /** Numbered pages of a thread; page 1 holds the newest messages, later pages are older. */
    fun thread(
        username: String,
        request: IOSPageRequest,
        loaded: Int,
        completion: (IOSMessagePage?, IOSFailure?) -> Unit,
    ): IOSOperation = client.operation(completion) {
        requireAccount()
        val page = request.toDomain()
        val result = repository.getConversationMessages(username.normalized(), page.number()).getOrThrow()
        IOSMessagePage(
            items = result.data.map { it.toIOS() },
            next = result.nextAfter(PaginationMode.Numbered, page, loaded),
            total = result.pagination?.total,
        )
    }

    fun newer(username: String, completion: (List<IOSPrivateMessage>?, IOSFailure?) -> Unit): IOSOperation =
        client.operation(completion) {
            requireAccount()
            repository.getConversationMessagesNewer(username.normalized()).getOrThrow().data.map { it.toIOS() }
        }

    fun send(
        username: String,
        content: String,
        adult: Boolean,
        photoKey: String?,
        completion: (IOSPrivateMessage?, IOSFailure?) -> Unit,
    ): IOSOperation = client.operation(completion) {
        requireAccount()
        val text = content.trim()
        require(text.isNotEmpty() || photoKey != null) { "empty message" }
        repository.openConversation(username.normalized(), text, adult, photoKey, embed = null).getOrThrow().toIOS()
    }

    /** Marks every private message read; the shared status then refreshes the badge. */
    fun readAll(completion: (IOSSuccess?, IOSFailure?) -> Unit): IOSOperation = client.operation(completion) {
        requireAccount()
        repository.readAll().getOrThrow()
        IOSSuccess()
    }

    private suspend fun requireAccount() {
        require(authRepository.isLoggedIn()) { "account required" }
    }

    private fun String.normalized(): String = normalizeUsername(this).also {
        require(it.isNotBlank()) { "empty username" }
    }

    private fun PageRequest.number(): Int =
        (this as? PageRequest.Number)?.value ?: throw IllegalArgumentException("numbered page expected")
}

internal fun String.toNotificationGroup(): NotificationGroup = when (this) {
    "entries" -> NotificationGroup.Entries
    "pm" -> NotificationGroup.PrivateMessages
    "tags" -> NotificationGroup.Tags
    "observedDiscussions" -> NotificationGroup.ObservedDiscussions
    else -> throw IllegalArgumentException("invalid notification group")
}

internal fun NotificationsStatus.toIOS() = IOSNotificationStatus(
    totalUnreadCount = totalUnreadCount,
    privateMessagesUnreadCount = privateMessagesUnreadCount,
    entriesUnreadCount = entriesUnreadCount,
    tagsUnreadCount = tagsUnreadCount,
    observedDiscussionsUnreadCount = observedDiscussionsUnreadCount,
)

internal fun NotificationItem.toIOS(): IOSNotification {
    val (kind, value) = when (val target = target()) {
        is NotificationTarget.Link -> "link" to target.id.toString()
        is NotificationTarget.Entry -> "entry" to target.id.toString()
        is NotificationTarget.Conversation -> "conversation" to target.username
        is NotificationTarget.Profile -> "profile" to target.username
        is NotificationTarget.Tag -> "tag" to target.name
        is NotificationTarget.External -> "external" to target.url
        NotificationTarget.None -> "none" to null
    }
    return IOSNotification(
        id = id,
        group = when (group) {
            NotificationGroup.Entries -> "entries"
            NotificationGroup.PrivateMessages -> "pm"
            NotificationGroup.Tags -> "tags"
            NotificationGroup.ObservedDiscussions -> "observedDiscussions"
        },
        isRead = isRead,
        groupId = groupId,
        groupCount = groupCount,
        createdAt = createdAt.toString(),
        createdAtEpochMillis = createdAt.epochMillis(),
        actorUsername = actor?.username,
        actorColor = actor?.nameColor?.toIOS(),
        actorGender = actor?.gender?.toIOS(),
        message = message,
        issueTitle = issueTitle,
        badgeName = badgeName,
        tagName = tagName,
        linkId = linkId,
        linkTitle = linkTitle,
        entryId = entryId,
        entryContent = entryContent,
        targetKind = kind,
        targetValue = value,
    )
}

internal fun PrivateMessage.toIOS() = IOSPrivateMessage(
    key = key,
    content = content,
    createdAt = createdAt.toString(),
    createdAtEpochMillis = createdAt.epochMillis(),
    incoming = type == 1,
    adult = adult,
    senderUsername = sender?.username,
    senderColor = sender?.nameColor?.toIOS(),
    photo = photo?.let { IOSPhoto(it.url, it.width, it.height, it.mimeType, it.key) },
    embedUrl = embed?.url,
)

private fun LocalDateTime.epochMillis(): Long = toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

package pl.masslany.podkop.ios

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.usePinned
import io.ktor.client.plugins.ResponseException
import pl.masslany.podkop.common.network.api.HttpStatusFailure
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import platform.Foundation.NSData
import platform.posix.memcpy
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.common.domain.models.common.Comment
import pl.masslany.podkop.business.common.domain.models.common.Photo
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.Deleted
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.NameColor
import pl.masslany.podkop.business.common.domain.models.common.Voted
import pl.masslany.podkop.business.common.domain.models.common.VoteReason
import pl.masslany.podkop.business.di.businessModule
import pl.masslany.podkop.business.embeds.domain.main.StreamableVideoRepository
import pl.masslany.podkop.business.embeds.domain.main.TwitterEmbedPreviewRepository
import pl.masslany.podkop.business.entries.domain.main.EntriesRepository
import pl.masslany.podkop.business.entries.domain.models.request.EntriesSortType
import pl.masslany.podkop.business.entries.domain.models.request.HotSortType
import pl.masslany.podkop.business.hits.domain.main.HitsRepository
import pl.masslany.podkop.business.hits.domain.models.request.HitsSortType
import pl.masslany.podkop.business.favourites.domain.main.FavouritesRepository
import pl.masslany.podkop.business.favourites.domain.models.FavouriteType
import pl.masslany.podkop.business.links.domain.main.LinksRepository
import pl.masslany.podkop.business.links.domain.models.request.LinksSortType
import pl.masslany.podkop.business.links.domain.models.request.LinksType
import pl.masslany.podkop.business.links.domain.models.request.UpdateLinkDraft
import pl.masslany.podkop.business.links.domain.models.request.PublishLinkDraft
import pl.masslany.podkop.business.links.domain.models.LinkDraftDetails
import pl.masslany.podkop.business.media.domain.main.MediaPhotoType
import pl.masslany.podkop.business.media.domain.main.MediaRepository
import pl.masslany.podkop.business.tags.domain.main.TagsRepository
import pl.masslany.podkop.business.links.domain.models.request.CommentsSortType
import pl.masslany.podkop.business.notifications.domain.main.NotificationsRepository
import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.business.startup.api.StartupManager
import pl.masslany.podkop.business.startup.models.AppState
import pl.masslany.podkop.common.deeplink.AppDeepLink
import pl.masslany.podkop.common.deeplink.AppDeepLinkParser
import pl.masslany.podkop.common.deeplink.AuthSessionEvent
import pl.masslany.podkop.common.deeplink.AuthSessionEvents
import pl.masslany.podkop.common.pagination.PageRequest
import pl.masslany.podkop.common.pagination.initialRequest
import pl.masslany.podkop.common.pagination.nextRequest
import pl.masslany.podkop.common.persistence.api.KeyValueStorage
import pl.masslany.podkop.common.settings.AppSettings
import pl.masslany.podkop.common.settings.AppSettingsImpl
import pl.masslany.podkop.common.settings.ThemeOverride
import pl.masslany.podkop.features.pagination.FeaturePaginationPolicies

/** Public values do not expose business, Koin, coroutine, or Compose types to Swift. */
class IOSFailure(val category: String, val code: String? = null)
class IOSSuccess(val completed: Boolean = true)
class IOSStartupState(val phase: String)
class IOSSessionState(val isLoggedIn: Boolean, val revision: Int)
class IOSSettings(
    val autoplayGifs: Boolean,
    val themeOverride: String,
    val dynamicColorsEnabled: Boolean,
    val playVideosInline: Boolean,
)
class IOSNotificationStatus(
    val totalUnreadCount: Int,
    val privateMessagesUnreadCount: Int,
    val entriesUnreadCount: Int = 0,
    val tagsUnreadCount: Int = 0,
    val observedDiscussionsUnreadCount: Int = 0,
)
class IOSLinkIntent(val kind: String, val id: Int? = null)
class IOSPageRequest(val kind: String, val value: String? = null)
class IOSPagePolicy(val kind: String, val initial: IOSPageRequest)
class IOSPhoto(
    val url: String,
    val width: Int,
    val height: Int,
    val mimeType: String,
    val key: String,
    /** Where the image came from, shown as "źródło: …" under it. */
    val label: String = "",
)
class IOSEmbed(val key: String, val url: String, val thumbnailUrl: String, val type: String)
class IOSSurveyAnswer(val id: Int, val text: String, val count: Int, val selected: Boolean)
class IOSSurvey(
    val question: String,
    val answers: List<IOSSurveyAnswer>,
    val count: Int,
    val canVote: Boolean,
    val selectedOption: Int?,
)
class IOSTweetPreview(
    val authorName: String,
    val authorHandle: String,
    val avatarUrl: String?,
    val text: String,
    val replyCount: Int,
    val retweetCount: Int,
    val likeCount: Int,
    val mediaThumbnailUrl: String?,
    val mediaAspectRatio: Float?,
)
class IOSStreamableVideo(val mp4Url: String, val aspectRatio: Float?)
class IOSResource(
    val id: Int,
    val kind: String,
    val title: String,
    val content: String,
    val author: String?,
    val adult: Boolean,
    val deleted: Boolean,
    val editable: Boolean,
    val favourite: Boolean,
    val description: String,
    val authorAvatarUrl: String?,
    val authorColor: String?,
    val authorVerified: Boolean,
    val authorOnline: Boolean,
    val authorRank: Int?,
    /** male, female or unspecified; drives the gender bar under the avatar. */
    val authorGender: String?,
    val deletionReason: String?,
    val parentId: Int?,
    val createdAt: String?,
    val commentsCount: Int,
    val votesUp: Int,
    val votesDown: Int,
    val voted: String,
    val canVoteUp: Boolean,
    val canVoteDown: Boolean,
    val canUndoVote: Boolean,
    val canDelete: Boolean,
    val tags: List<String>,
    val photo: IOSPhoto?,
    val embed: IOSEmbed?,
    val survey: IOSSurvey?,
    val sourceUrl: String?,
    val sourceLabel: String?,
    val hot: Boolean,
    val recommended: Boolean,
    val slug: String,
    val canReply: Boolean = false,
    val canFavourite: Boolean = false,
    /** The newest comments the API embeds with a resource (entries in lists, link comment replies). */
    val inlineComments: List<IOSResource> = emptyList(),
    /** The author (or, for comments, the comment itself) is on the reader's blacklist. */
    val blacklisted: Boolean = false,
)
class IOSResourcePage(
    val items: List<IOSResource>,
    val next: String?,
    val total: Int?,
)
class IOSVoter(
    val username: String,
    val avatarUrl: String,
    val verified: Boolean,
    val reason: String?,
    /** orange, burgundy, green or black. */
    val color: String? = null,
    /** male, female or unspecified. */
    val gender: String? = null,
)
class IOSVoterPage(val items: List<IOSVoter>, val next: String?, val total: Int?)
class IOSUploadedPhoto(val key: String, val url: String, val mimeType: String)
class IOSLinkDraftCheck(val key: String, val duplicate: Boolean, val similar: List<IOSResource>)
class IOSLinkDraft(
    val key: String,
    val url: String,
    val title: String,
    val description: String,
    val tags: List<String>,
    val adult: Boolean,
    val photoKey: String?,
    val photoUrl: String?,
    val suggestedImages: List<String>,
    val selectedImageIndex: Int?,
)

class IOSOperation internal constructor(private val job: Job) {
    fun cancel() { job.cancel() }
}

class IOSObservation internal constructor(private val job: Job) {
    fun cancel() { job.cancel() }
}

/** All public methods are called from Swift's main actor. Callbacks are posted to Main. */
class PodkopClient private constructor(
    private val app: KoinApplication,
    private val scope: CoroutineScope,
) {
    private val startupManager: StartupManager = app.koin.get()
    private val authRepository: AuthRepository = app.koin.get()
    private val sessionEvents: AuthSessionEvents = app.koin.get()
    private val settingsStore: AppSettings = AppSettingsImpl(app.koin.get<KeyValueStorage>())
    private val notificationsRepository: NotificationsRepository = app.koin.get()
    private val linksRepository: LinksRepository = app.koin.get()
    private val entriesRepository: EntriesRepository = app.koin.get()
    private val hitsRepository: HitsRepository = app.koin.get()
    private val favouritesRepository: FavouritesRepository = app.koin.get()
    private val mediaRepository: MediaRepository = app.koin.get()
    private val tagsRepository: TagsRepository = app.koin.get()
    private val twitterPreviewRepository: TwitterEmbedPreviewRepository = app.koin.get()
    private val streamableVideoRepository: StreamableVideoRepository = app.koin.get()
    private val parser = AppDeepLinkParser()
    private var closed = false

    val startup = StartupService()
    val session = SessionService()
    val settings = SettingsService()
    val notifications = NotificationsService()
    val links = LinksService()
    val entries = EntriesService()
    val details = DetailsService()
    val mutations = MutationService()
    val composer = ComposerService()
    val media = MediaService()
    val linkDrafts = LinkDraftService()
    val search = SearchService(this)
    val hits = HitsService(this)
    val rank = RankService(this)
    val favourites = FavouritesService(this)
    val observed = ObservedService(this)
    val tag = TagService(this)
    val profile = ProfileService(this)
    val blacklists = BlacklistsService(this)
    val accountSettings = AccountSettingsService(this)
    val messages = MessagesService(this)
    val about = AboutService()
    val voters = VotersService()
    val embeds = EmbedsService()

    fun close() {
        if (closed) return
        closed = true
        scope.cancel()
        notificationsRepository.stopPolling()
        app.close()
        if (active === this) active = null
    }

    internal val koin get() = app.koin

    internal fun <T : Any> operation(
        completion: (T?, IOSFailure?) -> Unit,
        block: suspend () -> T,
    ): IOSOperation {
        val job = scope.launch {
            yield() // callbacks never run before the operation handle is returned
            try {
                val value = block()
                if (isActiveAndOpen()) completion(value, null)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Throwable) {
                if (isActiveAndOpen()) completion(null, failure.toIOSFailure())
            }
        }
        return IOSOperation(job)
    }

    private fun isActiveAndOpen(): Boolean = !closed && scope.coroutineContext[Job]?.isActive == true

    private fun <T : Any> observe(
        callback: (T) -> Unit,
        collectValues: suspend (emit: (T) -> Unit) -> Unit,
    ): IOSObservation {
        val job = scope.launch {
            yield()
            collectValues { value -> if (isActiveAndOpen()) callback(value) }
        }
        return IOSObservation(job)
    }

    inner class StartupService {
        fun start(key: String, secret: String, completion: (IOSSuccess?, IOSFailure?) -> Unit): IOSOperation =
            operation(completion) {
                startupManager.init(key, secret)
                if (startupManager.state.value is AppState.Error) error("startup failed")
                IOSSuccess()
            }

        fun retry(completion: (IOSSuccess?, IOSFailure?) -> Unit): IOSOperation = operation(completion) {
            startupManager.retry()
            if (startupManager.state.value is AppState.Error) error("startup failed")
            IOSSuccess()
        }

        fun observe(onChange: (IOSStartupState) -> Unit): IOSObservation = observe(onChange) { emit ->
            startupManager.state.collect { state ->
                emit(IOSStartupState(when (state) {
                    AppState.Initializing -> "initializing"
                    AppState.Ready -> "ready"
                    AppState.Error -> "error"
                }))
            }
        }
    }

    inner class SessionService {
        fun current(completion: (IOSSessionState?, IOSFailure?) -> Unit): IOSOperation = operation(completion) {
            IOSSessionState(authRepository.isLoggedIn(), 0)
        }

        fun loginUrl(completion: (String?, IOSFailure?) -> Unit): IOSOperation = operation(completion) {
            authRepository.getWykopConnect().getOrThrow()
        }

        fun acceptUrl(url: String, completion: (IOSLinkIntent?, IOSFailure?) -> Unit): IOSOperation =
            operation(completion) {
                when (val intent = parser.parse(url)) {
                    is AppDeepLink.LoginCallback -> {
                        authRepository.storeSessionTokens(intent.token, intent.refreshToken)
                        sessionEvents.tryEmit(AuthSessionEvent.TokensUpdated)
                        IOSLinkIntent("login")
                    }
                    is AppDeepLink.LinkDetails -> IOSLinkIntent("link", intent.id)
                    is AppDeepLink.EntryDetails -> IOSLinkIntent("entry", intent.id)
                    AppDeepLink.PrivateMessagesInbox -> IOSLinkIntent("messages")
                    AppDeepLink.Hits -> IOSLinkIntent("hits")
                    null -> throw IllegalArgumentException("unsupported URL")
                }
            }

        /** Whether an embedded login page must stop at [url] and hand it to [acceptUrl]. */
        fun isAppUrl(url: String): Boolean = parser.isAppHost(url)

        fun logout(completion: (IOSSuccess?, IOSFailure?) -> Unit): IOSOperation = operation(completion) {
            val result = authRepository.logout()
            sessionEvents.tryEmit(AuthSessionEvent.TokensUpdated)
            result.getOrThrow()
            IOSSuccess()
        }

        fun observe(onChange: (IOSSessionState) -> Unit): IOSObservation = observe(onChange) { emit ->
            var revision = 0
            emit(IOSSessionState(authRepository.isLoggedIn(), revision))
            sessionEvents.events.collect {
                revision += 1
                emit(IOSSessionState(authRepository.isLoggedIn(), revision))
            }
        }
    }

    inner class SettingsService {
        fun observe(onChange: (IOSSettings) -> Unit): IOSObservation = observe(onChange) { emit ->
            combine(
                settingsStore.autoplayGifs,
                settingsStore.themeOverride,
                settingsStore.dynamicColorsEnabled,
                settingsStore.playVideosInline,
            ) { autoplay, theme, dynamic, inlineVideos ->
                IOSSettings(autoplay, theme.name, dynamic, inlineVideos)
            }.collect { emit(it) }
        }

        fun setAutoplayGifs(enabled: Boolean, completion: (IOSSuccess?, IOSFailure?) -> Unit): IOSOperation =
            operation(completion) { settingsStore.setAutoplayGifs(enabled); IOSSuccess() }

        fun setTheme(name: String, completion: (IOSSuccess?, IOSFailure?) -> Unit): IOSOperation =
            operation(completion) {
                val theme = ThemeOverride.entries.firstOrNull { it.name == name }
                    ?: throw IllegalArgumentException("invalid theme")
                settingsStore.setThemeOverride(theme)
                IOSSuccess()
            }

        fun setDynamicColors(enabled: Boolean, completion: (IOSSuccess?, IOSFailure?) -> Unit): IOSOperation =
            operation(completion) { settingsStore.setDynamicColorsEnabled(enabled); IOSSuccess() }

        fun setPlayVideosInline(enabled: Boolean, completion: (IOSSuccess?, IOSFailure?) -> Unit): IOSOperation =
            operation(completion) { settingsStore.setPlayVideosInline(enabled); IOSSuccess() }
    }

    inner class NotificationsService {
        fun startPolling() { notificationsRepository.startPolling() }

        fun stopPolling() { notificationsRepository.stopPolling() }

        fun observeStatus(onChange: (IOSNotificationStatus) -> Unit): IOSObservation = observe(onChange) { emit ->
            notificationsRepository.status.collect { status -> emit(status.toIOS()) }
        }

        fun refreshStatus(completion: (IOSNotificationStatus?, IOSFailure?) -> Unit): IOSOperation = operation(completion) {
            notificationsRepository.refreshStatus().getOrThrow().toIOS()
        }

        /** Android's rule: a signed-in session refreshes counts, a signed-out one clears them. */
        fun onSessionChanged(completion: (IOSSuccess?, IOSFailure?) -> Unit): IOSOperation = operation(completion) {
            if (authRepository.isLoggedIn()) notificationsRepository.refreshStatus().getOrThrow()
            else notificationsRepository.clearUnreadCount()
            IOSSuccess()
        }

        /** [group]: entries, pm, tags, observedDiscussions. */
        fun firstRequest(group: String): IOSPageRequest =
            FeaturePaginationPolicies.notifications(group.toNotificationGroup()).initialRequest().toIOS()

        fun load(
            group: String,
            request: IOSPageRequest,
            loaded: Int,
            completion: (IOSNotificationPage?, IOSFailure?) -> Unit,
        ): IOSOperation = operation(completion) {
            val notificationGroup = group.toNotificationGroup()
            val page = request.toDomain()
            val result = notificationsRepository.getNotifications(notificationGroup, page).getOrThrow()
            IOSNotificationPage(
                items = result.data.map { it.toIOS() },
                next = result.nextAfter(FeaturePaginationPolicies.notifications(notificationGroup), page, loaded),
                total = result.pagination?.total,
            )
        }

        fun markAsRead(group: String, id: String, completion: (IOSSuccess?, IOSFailure?) -> Unit): IOSOperation =
            operation(completion) {
                require(id.isNotBlank()) { "empty notification id" }
                notificationsRepository.markAsRead(group.toNotificationGroup(), id).getOrThrow()
                IOSSuccess()
            }

        fun markAllAsRead(group: String, completion: (IOSSuccess?, IOSFailure?) -> Unit): IOSOperation =
            operation(completion) {
                val notificationGroup = group.toNotificationGroup()
                require(notificationGroup != NotificationGroup.PrivateMessages) { "use messages.readAll" }
                notificationsRepository.markAllAsRead(notificationGroup).getOrThrow()
                IOSSuccess()
            }
    }

    inner class LinksService {
        fun policy(isLoggedIn: Boolean, isUpcoming: Boolean): IOSPagePolicy {
            val mode = FeaturePaginationPolicies.links(isLoggedIn, isUpcoming)
            return IOSPagePolicy(mode.name, mode.initialRequest().toIOS())
        }

        fun nextRequest(isLoggedIn: Boolean, isUpcoming: Boolean, next: String?, nextNumber: Int): IOSPageRequest? =
            FeaturePaginationPolicies.links(isLoggedIn, isUpcoming).nextRequest(next, nextNumber)?.toIOS()

        fun load(
            request: IOSPageRequest,
            isUpcoming: Boolean,
            sort: String,
            completion: (IOSResourcePage?, IOSFailure?) -> Unit,
        ): IOSOperation = operation(completion) {
            val page = request.toDomain()
            val sortType = when (sort) {
                "newest" -> LinksSortType.Newest
                "active" -> LinksSortType.Active
                "commented" -> LinksSortType.Commented
                "digged" -> LinksSortType.Digged
                else -> throw IllegalArgumentException("invalid sort")
            }
            val result = linksRepository.getLinks(
                page = page,
                limit = null,
                linksSortType = sortType,
                linksType = if (isUpcoming) LinksType.UPCOMING else LinksType.HOMEPAGE,
                category = null,
                bucket = null,
            ).getOrThrow()
            IOSResourcePage(
                items = result.data.map(ResourceItem::toIOSResource),
                next = result.pagination?.next,
                total = result.pagination?.total,
            )
        }

        fun hits(completion: (IOSResourcePage?, IOSFailure?) -> Unit): IOSOperation = operation(completion) {
            val result = hitsRepository.getLinkHits(hitsSortType = HitsSortType.Day).getOrThrow()
            IOSResourcePage(result.data.map(ResourceItem::toIOSResource), result.pagination?.next, result.pagination?.total)
        }
    }

    inner class EntriesService {
        fun policy(isLoggedIn: Boolean): IOSPagePolicy {
            val mode = FeaturePaginationPolicies.entries(isLoggedIn)
            return IOSPagePolicy(mode.name, mode.initialRequest().toIOS())
        }

        fun nextRequest(isLoggedIn: Boolean, next: String?, nextNumber: Int): IOSPageRequest? =
            FeaturePaginationPolicies.entries(isLoggedIn).nextRequest(next, nextNumber)?.toIOS()

        fun load(
            request: IOSPageRequest,
            sort: String,
            hotHours: Int,
            completion: (IOSResourcePage?, IOSFailure?) -> Unit,
        ): IOSOperation = operation(completion) {
            val sortType = when (sort) {
                "newest" -> EntriesSortType.Newest
                "active" -> EntriesSortType.Active
                "hot" -> EntriesSortType.Hot
                else -> throw IllegalArgumentException("invalid sort")
            }
            val period = when (hotHours) {
                2 -> HotSortType.TwoHours
                6 -> HotSortType.SixHours
                12 -> HotSortType.TwelveHours
                24 -> HotSortType.TwentyFourHours
                else -> throw IllegalArgumentException("invalid hot period")
            }
            val result = entriesRepository.getEntries(
                page = request.toDomain(), limit = null, entriesSortType = sortType,
                hotSortType = period, category = null, bucket = null,
            ).getOrThrow()
            IOSResourcePage(result.data.map(ResourceItem::toIOSResource), result.pagination?.next, result.pagination?.total)
        }
    }

    inner class DetailsService {
        fun link(id: Int, completion: (IOSResource?, IOSFailure?) -> Unit): IOSOperation =
            operation(completion) { linksRepository.getLink(id).getOrThrow().data.toIOSResource() }

        fun entry(id: Int, completion: (IOSResource?, IOSFailure?) -> Unit): IOSOperation =
            operation(completion) { entriesRepository.getEntry(id).getOrThrow().toIOSResource() }

        fun linkComments(
            linkId: Int,
            page: Int,
            sort: String,
            completion: (IOSResourcePage?, IOSFailure?) -> Unit,
        ): IOSOperation = operation(completion) {
            require(page > 0) { "invalid page" }
            val sortType = when (sort) {
                "best" -> CommentsSortType.Best
                "newest" -> CommentsSortType.Newest
                "oldest" -> CommentsSortType.Oldest
                else -> throw IllegalArgumentException("invalid comment sort")
            }
            val result = linksRepository.getComments(linkId, page, null, sortType, null).getOrThrow()
            IOSResourcePage(result.data.map(ResourceItem::toIOSResource), result.pagination?.next, result.pagination?.total)
        }

        fun entryComments(
            entryId: Int,
            page: Int,
            completion: (IOSResourcePage?, IOSFailure?) -> Unit,
        ): IOSOperation = operation(completion) {
            require(page > 0) { "invalid page" }
            val result = entriesRepository.getEntryComments(entryId, page).getOrThrow()
            IOSResourcePage(result.data.map(ResourceItem::toIOSResource), result.pagination?.next, result.pagination?.total)
        }

        fun linkReplies(
            linkId: Int,
            commentId: Int,
            page: Int,
            completion: (IOSResourcePage?, IOSFailure?) -> Unit,
        ): IOSOperation = operation(completion) {
            require(page > 0) { "invalid page" }
            val result = linksRepository.getSubComments(linkId, commentId, page).getOrThrow()
            // The API names the parent comment as a reply's parent, but votes and other actions
            // on a link comment address it under its link, like the replies embedded in a page.
            IOSResourcePage(
                result.data.map { it.copy(parentId = linkId).toIOSResource() },
                result.pagination?.next,
                result.pagination?.total,
            )
        }

        fun relatedLinks(linkId: Int, completion: (IOSResourcePage?, IOSFailure?) -> Unit): IOSOperation =
            operation(completion) {
                val result = linksRepository.getRelatedLinks(linkId).getOrThrow()
                IOSResourcePage(result.data.map(ResourceItem::toIOSResource), result.pagination?.next, result.pagination?.total)
            }
    }

    inner class ComposerService {
        fun submit(
            kind: String,
            rootId: Int?,
            commentId: Int?,
            content: String,
            adult: Boolean,
            photoKey: String?,
            completion: (IOSResource?, IOSFailure?) -> Unit,
        ): IOSOperation = operation(completion) {
            val text = content.trim()
            require(text.isNotEmpty()) { "empty content" }
            val result = when (kind) {
                "createEntry" -> entriesRepository.createEntry(text, adult, photoKey)
                "createEntryComment" -> entriesRepository.createEntryComment(
                    requireNotNull(rootId), text, adult, photoKey,
                )
                "createLinkComment" -> {
                    val linkId = requireNotNull(rootId)
                    if (commentId == null) linksRepository.createLinkComment(linkId, text, adult, photoKey)
                    else linksRepository.createLinkCommentReply(linkId, commentId, text, adult, photoKey)
                }
                "editEntry" -> entriesRepository.updateEntry(
                    requireNotNull(rootId), text, adult, photoKey,
                )
                "editEntryComment" -> entriesRepository.updateEntryComment(
                    requireNotNull(rootId), requireNotNull(commentId), text, adult, photoKey,
                )
                "editLinkComment" -> linksRepository.updateLinkComment(
                    requireNotNull(rootId), requireNotNull(commentId), text, adult, photoKey,
                )
                else -> throw IllegalArgumentException("invalid composer kind")
            }.getOrThrow()
            result.toIOSResource()
        }
    }

    inner class MediaService {
        fun uploadUrl(
            url: String, forLink: Boolean,
            completion: (IOSUploadedPhoto?, IOSFailure?) -> Unit,
        ): IOSOperation = operation(completion) {
            require(url.startsWith("https://", ignoreCase = true) ||
                    url.startsWith("http://", ignoreCase = true)) { "invalid photo url" }
            val photo = mediaRepository.uploadPhotoFromUrl(
                url, if (forLink) MediaPhotoType.Links else MediaPhotoType.Comments,
            ).getOrThrow()
            IOSUploadedPhoto(photo.key, photo.url, photo.mimeType)
        }

        @OptIn(ExperimentalForeignApi::class)
        fun uploadDevice(
            data: NSData, fileName: String?, mimeType: String?, forLink: Boolean,
            completion: (IOSUploadedPhoto?, IOSFailure?) -> Unit,
        ): IOSOperation = operation(completion) {
            require(data.length in 1uL..(20uL * 1024uL * 1024uL)) { "invalid photo size" }
            val bytes = ByteArray(data.length.toInt())
            bytes.usePinned { pinned -> memcpy(pinned.addressOf(0), data.bytes, data.length) }
            val photo = mediaRepository.uploadPhotoFromDevice(
                bytes, fileName, mimeType,
                if (forLink) MediaPhotoType.Links else MediaPhotoType.Comments,
            ).getOrThrow()
            IOSUploadedPhoto(photo.key, photo.url, photo.mimeType)
        }

        fun delete(key: String, completion: (IOSSuccess?, IOSFailure?) -> Unit): IOSOperation =
            operation(completion) {
                require(key.isNotBlank()) { "empty photo key" }
                mediaRepository.deletePhoto(key).getOrThrow()
                IOSSuccess()
            }
    }

    inner class LinkDraftService {
        fun suggest(query: String, completion: (List<String>?, IOSFailure?) -> Unit): IOSOperation =
            operation(completion) {
                val normalized = query.trim().removePrefix("#")
                require(normalized.length >= 2) { "query too short" }
                tagsRepository.getAutoCompleteTags(normalized).getOrThrow()
                    .tags.map { it.name }
            }

        fun check(url: String, completion: (IOSLinkDraftCheck?, IOSFailure?) -> Unit): IOSOperation =
            operation(completion) {
                require(url.startsWith("https://", ignoreCase = true) ||
                        url.startsWith("http://", ignoreCase = true)) { "invalid link url" }
                val result = linksRepository.createLinkDraft(url).getOrThrow()
                IOSLinkDraftCheck(result.key, result.duplicate,
                    result.similar.map(ResourceItem::toIOSResource))
            }

        fun list(completion: (List<IOSLinkDraft>?, IOSFailure?) -> Unit): IOSOperation =
            operation(completion) {
                linksRepository.getLinkDrafts().getOrThrow().map { it.toIOSLinkDraft() }
            }

        fun get(key: String, completion: (IOSLinkDraft?, IOSFailure?) -> Unit): IOSOperation =
            operation(completion) { linksRepository.getLinkDraft(key).getOrThrow().toIOSLinkDraft() }

        fun save(
            key: String, title: String, description: String?, tags: List<String>,
            photoKey: String?, adult: Boolean, selectedImageIndex: Int?,
            completion: (IOSSuccess?, IOSFailure?) -> Unit,
        ): IOSOperation = operation(completion) {
            linksRepository.updateLinkDraft(key, UpdateLinkDraft(
                title, description, tags, photoKey, adult, selectedImageIndex,
            )).getOrThrow()
            IOSSuccess()
        }

        fun publish(
            key: String, title: String, description: String?, tags: List<String>,
            photoKey: String?, adult: Boolean, selectedImageIndex: Int?,
            completion: (IOSSuccess?, IOSFailure?) -> Unit,
        ): IOSOperation = operation(completion) {
            require(title.isNotBlank() && tags.isNotEmpty()) { "title and tags required" }
            linksRepository.publishLinkDraft(key, PublishLinkDraft(
                title, description, tags, photoKey, adult, selectedImageIndex,
            )).getOrThrow()
            IOSSuccess()
        }

        fun delete(key: String, completion: (IOSSuccess?, IOSFailure?) -> Unit): IOSOperation =
            operation(completion) {
                linksRepository.deleteLinkDraft(key).getOrThrow()
                IOSSuccess()
            }
    }

    inner class MutationService {
        fun voteUp(
            kind: String, id: Int, parentId: Int?, remove: Boolean,
            completion: (IOSSuccess?, IOSFailure?) -> Unit,
        ): IOSOperation = operation(completion) {
            when (kind) {
                "link" -> if (remove) linksRepository.removeVoteOnLink(id).getOrThrow()
                    else linksRepository.voteOnLink(id).getOrThrow()
                "entry" -> if (remove) entriesRepository.removeVoteUp(id).getOrThrow()
                    else entriesRepository.voteUp(id).getOrThrow()
                "linkComment" -> {
                    val linkId = requireNotNull(parentId) { "missing parent link" }
                    if (remove) linksRepository.removeVoteOnLinkComment(linkId, id).getOrThrow()
                    else linksRepository.voteOnLinkComment(linkId, id).getOrThrow()
                }
                "entryComment" -> {
                    val entryId = requireNotNull(parentId) { "missing parent entry" }
                    if (remove) entriesRepository.removeVoteUpComment(entryId, id).getOrThrow()
                    else entriesRepository.voteUpComment(entryId, id).getOrThrow()
                }
                else -> throw IllegalArgumentException("invalid resource kind")
            }
            IOSSuccess()
        }

        fun voteDown(
            kind: String, id: Int, parentId: Int?, remove: Boolean, reason: String?,
            completion: (IOSSuccess?, IOSFailure?) -> Unit,
        ): IOSOperation = operation(completion) {
            when (kind) {
                "link" -> {
                    if (remove) linksRepository.removeVoteOnLink(id).getOrThrow()
                    else linksRepository.voteDownOnLink(id, reason.toVoteReason()).getOrThrow()
                }
                "linkComment" -> {
                    val linkId = requireNotNull(parentId) { "missing parent link" }
                    if (remove) linksRepository.removeVoteOnLinkComment(linkId, id).getOrThrow()
                    else linksRepository.voteDownOnLinkComment(linkId, id).getOrThrow()
                }
                else -> throw IllegalArgumentException("downvote unavailable")
            }
            IOSSuccess()
        }

        fun setFavourite(
            kind: String, id: Int, enabled: Boolean,
            completion: (IOSSuccess?, IOSFailure?) -> Unit,
        ): IOSOperation = operation(completion) {
            val type = when (kind) {
                "link" -> FavouriteType.Link
                "entry" -> FavouriteType.Entry
                "linkComment" -> FavouriteType.LinkComment
                "entryComment" -> FavouriteType.EntryComment
                else -> throw IllegalArgumentException("invalid resource kind")
            }
            if (enabled) favouritesRepository.createFavourite(type, id).getOrThrow()
            else favouritesRepository.deleteFavourite(type, id).getOrThrow()
            IOSSuccess()
        }

        fun voteSurvey(
            entryId: Int, option: Int,
            completion: (IOSSuccess?, IOSFailure?) -> Unit,
        ): IOSOperation = operation(completion) {
            require(option > 0) { "invalid survey option" }
            entriesRepository.voteSurvey(entryId, option).getOrThrow()
            IOSSuccess()
        }

        fun voteRelated(
            linkId: Int, relatedId: Int, remove: Boolean, down: Boolean,
            completion: (IOSSuccess?, IOSFailure?) -> Unit,
        ): IOSOperation = operation(completion) {
            when {
                remove -> linksRepository.removeVoteOnRelatedLink(linkId, relatedId)
                down -> linksRepository.voteDownOnRelatedLink(linkId, relatedId)
                else -> linksRepository.voteUpOnRelatedLink(linkId, relatedId)
            }.getOrThrow()
            IOSSuccess()
        }

        fun delete(
            kind: String, id: Int, parentId: Int?,
            completion: (IOSSuccess?, IOSFailure?) -> Unit,
        ): IOSOperation = operation(completion) {
            when (kind) {
                "entry" -> entriesRepository.deleteEntry(id).getOrThrow()
                "entryComment" -> entriesRepository.deleteEntryComment(
                    requireNotNull(parentId) { "missing parent entry" }, id,
                ).getOrThrow()
                else -> throw IllegalArgumentException("delete unavailable")
            }
            IOSSuccess()
        }

        private fun String?.toVoteReason(): VoteReason = when (this) {
            "duplicate" -> VoteReason.Duplicate
            "spam" -> VoteReason.Spam
            "fake" -> VoteReason.Fake
            "wrong" -> VoteReason.Wrong
            "invalid" -> VoteReason.Invalid
            else -> throw IllegalArgumentException("invalid downvote reason")
        }
    }

    inner class VotersService {
        fun load(
            kind: String, rootId: Int, commentId: Int?, side: String, page: Int,
            completion: (IOSVoterPage?, IOSFailure?) -> Unit,
        ): IOSOperation = operation(completion) {
            require(page > 0) { "invalid page" }
            val result = when (kind) {
                "entry" -> entriesRepository.getEntryVotes(rootId, page)
                "entryComment" -> entriesRepository.getEntryCommentVotes(
                    rootId, requireNotNull(commentId) { "missing comment" }, page,
                )
                "link" -> {
                    require(side == "up" || side == "down") { "invalid side" }
                    linksRepository.getLinkUpvotes(rootId, side, page)
                }
                else -> throw IllegalArgumentException("voters unavailable")
            }.getOrThrow()
            IOSVoterPage(
                result.data.map {
                    IOSVoter(it.username, it.avatar, it.verified, it.reason?.name, it.color.toIOS(), it.gender.toIOS())
                },
                result.pagination?.next,
                result.pagination?.total,
            )
        }
    }

    inner class EmbedsService {
        fun twitterPreview(
            url: String,
            completion: (IOSTweetPreview?, IOSFailure?) -> Unit,
        ): IOSOperation = operation(completion) {
            require(url.startsWith("https://")) { "invalid URL" }
            val preview = twitterPreviewRepository.getTweet(url).getOrThrow()
            IOSTweetPreview(
                authorName = preview.authorName,
                authorHandle = preview.authorHandle,
                avatarUrl = preview.avatarUrl,
                text = preview.text,
                replyCount = preview.replyCount,
                retweetCount = preview.retweetCount,
                likeCount = preview.likeCount,
                mediaThumbnailUrl = preview.mediaThumbnailUrl,
                mediaAspectRatio = preview.mediaAspectRatio,
            )
        }

        /** Resolve right before playback: the returned MP4 url is signed and expires. */
        fun streamableVideo(
            url: String,
            completion: (IOSStreamableVideo?, IOSFailure?) -> Unit,
        ): IOSOperation = operation(completion) {
            val video = streamableVideoRepository.getVideo(url).getOrThrow()
            IOSStreamableVideo(mp4Url = video.mp4Url, aspectRatio = video.aspectRatio)
        }
    }

    companion object {
        private var active: PodkopClient? = null

        fun create(): PodkopClient = active?.takeUnless { it.closed } ?: run {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
            PodkopClient(
                app = koinApplication {
                    modules(businessModule, module { single<CoroutineScope> { scope } })
                },
                scope = scope,
            ).also { active = it }
        }
    }
}

internal fun ResourceItem.toIOSResource(): IOSResource {
    val selectedSurveyOption = media?.survey?.let { survey ->
        survey.answers.indexOfFirst { it.voted > 0 }
            .takeIf { it >= 0 }?.plus(1)
            ?: survey.voted.takeIf { it in 1..survey.answers.size }
    }
    return IOSResource(
        id = id,
        kind = when (resource) {
            Resource.Link -> "link"
            Resource.Entry -> "entry"
            Resource.EntryComment -> "entryComment"
            Resource.LinkComment -> "linkComment"
            Resource.Unknown -> "unknown"
        },
        title = title,
        content = content,
        author = author?.username,
        adult = adult,
        deleted = deleted != Deleted.None,
        editable = editable,
        favourite = favourite,
        description = description,
        authorAvatarUrl = author?.avatar,
        authorColor = when (author?.color) {
            NameColor.Orange -> "orange"
            NameColor.Burgundy -> "burgundy"
            NameColor.Green -> "green"
            NameColor.Black -> "black"
            null -> null
        },
        authorVerified = author?.verified ?: false,
        authorOnline = author?.online ?: false,
        authorRank = author?.rank?.position,
        authorGender = author?.gender?.toIOS(),
        deletionReason = when (deleted) {
            Deleted.Moderator -> "moderator"
            Deleted.Author -> "author"
            Deleted.Host -> "entryAuthor"
            Deleted.None -> null
        },
        parentId = parentId ?: parent?.id,
        createdAt = createdAt?.toString(),
        commentsCount = comments?.count ?: 0,
        votesUp = votes?.up ?: 0,
        votesDown = votes?.down ?: 0,
        voted = when (voted) {
            Voted.Positive -> "positive"
            Voted.Negative -> "negative"
            Voted.None -> "none"
        },
        canVoteUp = actions?.voteUp ?: false,
        canVoteDown = actions?.voteDown ?: false,
        canUndoVote = actions?.undoVote ?: false,
        canDelete = deletable && (actions?.delete ?: false),
        tags = tags,
        photo = media?.photo?.toIOS(),
        embed = media?.embed?.let { IOSEmbed(it.key, it.url, it.thumbnail, it.type) },
        survey = media?.survey?.let { survey ->
            IOSSurvey(
                question = survey.question,
                answers = survey.answers.map { answer ->
                    IOSSurveyAnswer(answer.id, answer.text, answer.count, answer.voted > 0)
                },
                count = survey.count,
                canVote = survey.actions.vote && selectedSurveyOption == null,
                selectedOption = selectedSurveyOption,
            )
        },
        sourceUrl = source?.url,
        sourceLabel = source?.label,
        hot = hot,
        recommended = recommended,
        slug = slug,
        canReply = actions?.create ?: false,
        canFavourite = actions?.let { it.createFavourite || it.deleteFavourite } ?: false,
        blacklisted = author?.blacklist == true,
        inlineComments = comments?.items.orEmpty().map {
            it.toIOSResource(rootId = if (resource == Resource.Entry || resource == Resource.Link) id else parentId ?: parent?.id)
        },
    )
}

private fun Photo.toIOS() = IOSPhoto(url, width, height, mimeType, key, label)

/** An embedded comment, mapped like a full resource so Swift renders both the same way. */
internal fun Comment.toIOSResource(rootId: Int? = null): IOSResource = IOSResource(
    id = id,
    kind = if (resource == Resource.LinkComment) "linkComment" else "entryComment",
    title = "",
    content = content,
    author = author.username,
    adult = adult,
    deleted = deleted != Deleted.None,
    editable = editable,
    favourite = favourite,
    description = "",
    authorAvatarUrl = author.avatar,
    authorColor = when (author.color) {
        NameColor.Orange -> "orange"
        NameColor.Burgundy -> "burgundy"
        NameColor.Green -> "green"
        NameColor.Black -> "black"
    },
    authorVerified = author.verified,
    authorOnline = author.online,
    authorRank = author.rank.position,
    authorGender = author.gender.toIOS(),
    deletionReason = when (deleted) {
        Deleted.Moderator -> "moderator"
        Deleted.Author -> "author"
        Deleted.Host -> "entryAuthor"
        Deleted.None -> null
    },
    parentId = rootId ?: parentId.takeIf { it > 0 },
    createdAt = createdAt?.toString(),
    commentsCount = comments?.count ?: 0,
    votesUp = votes.up,
    votesDown = votes.down,
    voted = when (voted) {
        Voted.Positive -> "positive"
        Voted.Negative -> "negative"
        Voted.None -> "none"
    },
    canVoteUp = actions.voteUp,
    canVoteDown = actions.voteDown,
    canUndoVote = actions.undoVote,
    canDelete = deletable && actions.delete,
    tags = tags,
    photo = media.photo?.toIOS(),
    embed = media.embed?.let { IOSEmbed(it.key, it.url, it.thumbnail, it.type) },
    survey = null,
    sourceUrl = null,
    sourceLabel = null,
    hot = false,
    recommended = false,
    slug = slug,
    canReply = actions.create,
    canFavourite = actions.createFavourite || actions.deleteFavourite,
    blacklisted = blacklist || author.blacklist,
    inlineComments = comments?.items.orEmpty().map { it.toIOSResource(rootId = rootId ?: parentId.takeIf { it > 0 }) },
)

private fun LinkDraftDetails.toIOSLinkDraft() = IOSLinkDraft(
    key, url, title.orEmpty(), description.orEmpty(), tags, adult,
    photoKey, photoUrl, suggestedImages, selectedImageIndex,
)

internal fun PageRequest.toIOS(): IOSPageRequest = when (this) {
    PageRequest.Initial -> IOSPageRequest("initial")
    is PageRequest.Number -> IOSPageRequest("number", value.toString())
    is PageRequest.PageCursor -> IOSPageRequest("pageCursor", value)
    is PageRequest.KeyCursor -> IOSPageRequest("keyCursor", value)
}

internal fun IOSPageRequest.toDomain(): PageRequest = when (kind) {
    "initial" -> PageRequest.Initial
    "number" -> PageRequest.Number(value?.toIntOrNull()?.takeIf { it > 0 } ?: error("invalid page"))
    "pageCursor" -> PageRequest.PageCursor(value?.takeIf { it.isNotBlank() } ?: error("invalid cursor"))
    "keyCursor" -> PageRequest.KeyCursor(value?.takeIf { it.isNotBlank() } ?: error("invalid cursor"))
    else -> error("invalid request")
}

internal fun Throwable.toIOSFailure(): IOSFailure = when (this) {
    is IllegalArgumentException -> IOSFailure("validation")
    is HttpStatusFailure -> httpFailure(statusCode)
    is ResponseException -> httpFailure(response.status.value)
    else -> IOSFailure("unknown")
}

/** The API client reports HTTP errors as [HttpStatusFailure]; Ktor's own exceptions are kept as a fallback. */
internal fun httpFailure(status: Int): IOSFailure = IOSFailure(
    category = when (status) {
        400, 422 -> "validation"
        401 -> "unauthorized"
        403 -> "forbidden"
        404 -> "notFound"
        429 -> "rateLimited"
        in 500..599 -> "server"
        else -> "unknown"
    },
    code = status.toString(),
)

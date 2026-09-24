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
import io.ktor.client.plugins.ResponseException
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.Deleted
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.NameColor
import pl.masslany.podkop.business.common.domain.models.common.Voted
import pl.masslany.podkop.business.di.businessModule
import pl.masslany.podkop.business.embeds.domain.main.TwitterEmbedPreviewRepository
import pl.masslany.podkop.business.entries.domain.main.EntriesRepository
import pl.masslany.podkop.business.entries.domain.models.request.EntriesSortType
import pl.masslany.podkop.business.entries.domain.models.request.HotSortType
import pl.masslany.podkop.business.hits.domain.main.HitsRepository
import pl.masslany.podkop.business.hits.domain.models.request.HitsSortType
import pl.masslany.podkop.business.links.domain.main.LinksRepository
import pl.masslany.podkop.business.links.domain.models.request.LinksSortType
import pl.masslany.podkop.business.links.domain.models.request.LinksType
import pl.masslany.podkop.business.notifications.domain.main.NotificationsRepository
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
class IOSSettings(val autoplayGifs: Boolean, val themeOverride: String, val dynamicColorsEnabled: Boolean)
class IOSNotificationStatus(val totalUnreadCount: Int, val privateMessagesUnreadCount: Int)
class IOSLinkIntent(val kind: String, val id: Int? = null)
class IOSPageRequest(val kind: String, val value: String? = null)
class IOSPagePolicy(val kind: String, val initial: IOSPageRequest)
class IOSPhoto(val url: String, val width: Int, val height: Int, val mimeType: String)
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
)
class IOSResourcePage(
    val items: List<IOSResource>,
    val next: String?,
    val total: Int?,
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
    private val twitterPreviewRepository: TwitterEmbedPreviewRepository = app.koin.get()
    private val parser = AppDeepLinkParser()
    private var closed = false

    val startup = StartupService()
    val session = SessionService()
    val settings = SettingsService()
    val notifications = NotificationsService()
    val links = LinksService()
    val entries = EntriesService()
    val embeds = EmbedsService()

    fun close() {
        if (closed) return
        closed = true
        scope.cancel()
        notificationsRepository.stopPolling()
        app.close()
        if (active === this) active = null
    }

    private fun <T : Any> operation(
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
                    null -> throw IllegalArgumentException("unsupported URL")
                }
            }

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
            combine(settingsStore.autoplayGifs, settingsStore.themeOverride, settingsStore.dynamicColorsEnabled) {
                    autoplay, theme, dynamic -> IOSSettings(autoplay, theme.name, dynamic)
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
    }

    inner class NotificationsService {
        fun startPolling() { notificationsRepository.startPolling() }

        fun stopPolling() { notificationsRepository.stopPolling() }

        fun observeStatus(onChange: (IOSNotificationStatus) -> Unit): IOSObservation = observe(onChange) { emit ->
            notificationsRepository.status.collect { status ->
                emit(IOSNotificationStatus(status.totalUnreadCount, status.privateMessagesUnreadCount))
            }
        }

        fun refreshStatus(completion: (IOSNotificationStatus?, IOSFailure?) -> Unit): IOSOperation = operation(completion) {
            val status = notificationsRepository.refreshStatus().getOrThrow()
            IOSNotificationStatus(status.totalUnreadCount, status.privateMessagesUnreadCount)
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
        photo = media?.photo?.let { IOSPhoto(it.url, it.width, it.height, it.mimeType) },
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
    )
}

private fun PageRequest.toIOS(): IOSPageRequest = when (this) {
    PageRequest.Initial -> IOSPageRequest("initial")
    is PageRequest.Number -> IOSPageRequest("number", value.toString())
    is PageRequest.PageCursor -> IOSPageRequest("pageCursor", value)
    is PageRequest.KeyCursor -> IOSPageRequest("keyCursor", value)
}

private fun IOSPageRequest.toDomain(): PageRequest = when (kind) {
    "initial" -> PageRequest.Initial
    "number" -> PageRequest.Number(value?.toIntOrNull()?.takeIf { it > 0 } ?: error("invalid page"))
    "pageCursor" -> PageRequest.PageCursor(value?.takeIf { it.isNotBlank() } ?: error("invalid cursor"))
    "keyCursor" -> PageRequest.KeyCursor(value?.takeIf { it.isNotBlank() } ?: error("invalid cursor"))
    else -> error("invalid request")
}

private fun Throwable.toIOSFailure(): IOSFailure = when (this) {
    is IllegalArgumentException -> IOSFailure("validation")
    is ResponseException -> IOSFailure(
        category = when (response.status.value) {
            400, 422 -> "validation"
            401 -> "unauthorized"
            403 -> "forbidden"
            404 -> "notFound"
            429 -> "rateLimited"
            in 500..599 -> "server"
            else -> "unknown"
        },
        code = response.status.value.toString(),
    )
    else -> IOSFailure("unknown")
}

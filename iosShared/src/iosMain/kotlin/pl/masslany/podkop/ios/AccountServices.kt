package pl.masslany.podkop.ios

import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.blacklists.domain.main.BlacklistsRepository
import pl.masslany.podkop.business.blacklists.domain.models.BlacklistNormalization
import pl.masslany.podkop.business.common.domain.models.common.PaginatedData
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.profile.domain.main.ProfileRepository
import pl.masslany.podkop.business.profile.domain.models.permissionsFor
import pl.masslany.podkop.common.pagination.PageRequest
import pl.masslany.podkop.common.pagination.PaginationMode
import pl.masslany.podkop.common.pagination.initialRequest

class IOSProfile(
    val username: String,
    val avatarUrl: String,
    val backgroundUrl: String,
    val rankPosition: Int?,
    val color: String,
    val gender: String,
    val memberSince: String?,
    val actions: Int,
    val links: Int,
    val entries: Int,
    val followers: Int,
    val following: Int,
    val observed: Boolean,
    val blacklisted: Boolean,
    val isLoggedIn: Boolean,
    val isOwnProfile: Boolean,
    val canManageObservation: Boolean,
    val canBlacklist: Boolean,
    val canSendPrivateMessage: Boolean,
)
class IOSProfileBadge(
    val label: String,
    val slug: String,
    val description: String,
    val iconUrl: String,
    val colorHex: String,
    val colorHexDark: String,
    val level: Int?,
    val progress: Int?,
    val achievedAt: String?,
)
class IOSProfileUser(
    val username: String,
    val avatarUrl: String,
    val color: String,
    val gender: String,
    val online: Boolean,
    val verified: Boolean,
    val company: Boolean,
    val status: String,
)
class IOSProfileTag(val name: String, val pinned: Boolean)

/** Exactly one list is filled, according to the requested section's content kind. */
class IOSProfileSectionPage(
    val resources: List<IOSResource>,
    val users: List<IOSProfileUser>,
    val tags: List<IOSProfileTag>,
    val next: IOSPageRequest?,
    val total: Int?,
)

class IOSBlacklistEntry(
    /** The normalized value used for removal and routing: username, tag name, or domain. */
    val value: String,
    val avatarUrl: String?,
    val color: String?,
    val gender: String?,
)
class IOSBlacklistPage(val items: List<IOSBlacklistEntry>, val next: IOSPageRequest?, val total: Int?)

class ProfileService internal constructor(private val client: PodkopClient) {
    private val profileRepository: ProfileRepository = client.koin.get()
    private val blacklistsRepository: BlacklistsRepository = client.koin.get()
    private val authRepository: AuthRepository = client.koin.get()

    /** The signed-in viewer's username, used to open the own profile. */
    fun ownUsername(completion: (String?, IOSFailure?) -> Unit): IOSOperation = client.operation(completion) {
        require(authRepository.isLoggedIn()) { "account required" }
        profileRepository.getProfileShort().getOrThrow().name
    }

    fun load(username: String, completion: (IOSProfile?, IOSFailure?) -> Unit): IOSOperation =
        client.operation(completion) {
            val name = username.normalizedUsername()
            val isLoggedIn = authRepository.isLoggedIn()
            // As on Android, an unknown viewer name narrows permissions instead of failing the screen.
            val viewer = if (isLoggedIn) profileRepository.getProfileShort().getOrNull()?.name else null
            val profile = profileRepository.getProfile(name).getOrThrow()
            val permissions = profile.permissionsFor(isLoggedIn = isLoggedIn, viewerUsername = viewer)
            IOSProfile(
                username = profile.name,
                avatarUrl = profile.avatarUrl,
                backgroundUrl = profile.backgroundUrl,
                rankPosition = profile.rankPosition,
                color = profile.color.toIOS(),
                gender = profile.gender.toIOS(),
                memberSince = profile.memberSince?.toString(),
                actions = profile.summary.actions,
                links = profile.summary.links,
                entries = profile.summary.entries,
                followers = profile.summary.followers,
                following = profile.summary.followingTags + profile.summary.followingUsers,
                observed = profile.isObserved,
                blacklisted = profile.isBlacklisted,
                isLoggedIn = isLoggedIn,
                isOwnProfile = permissions.isOwnProfile,
                canManageObservation = permissions.canManageObservation,
                canBlacklist = permissions.canBlacklist,
                canSendPrivateMessage = permissions.canSendPrivateMessage,
            )
        }

    fun badges(username: String, completion: (List<IOSProfileBadge>?, IOSFailure?) -> Unit): IOSOperation =
        client.operation(completion) {
            profileRepository.getProfileBadges(username.normalizedUsername()).getOrThrow().map {
                IOSProfileBadge(
                    label = it.label,
                    slug = it.slug,
                    description = it.description,
                    iconUrl = it.iconUrl,
                    colorHex = it.colorHex,
                    colorHexDark = it.colorHexDark,
                    level = it.level,
                    progress = it.progress,
                    achievedAt = it.achievedAt?.toString(),
                )
            }
        }

    fun note(username: String, completion: (String?, IOSFailure?) -> Unit): IOSOperation =
        client.operation(completion) {
            require(authRepository.isLoggedIn()) { "account required" }
            profileRepository.getProfileNote(username.normalizedUsername()).getOrThrow().content
        }

    fun saveNote(username: String, content: String, completion: (IOSSuccess?, IOSFailure?) -> Unit): IOSOperation =
        client.operation(completion) {
            require(authRepository.isLoggedIn()) { "account required" }
            profileRepository.updateProfileNote(username.normalizedUsername(), content).getOrThrow()
            IOSSuccess()
        }

    fun setObserved(username: String, enabled: Boolean, completion: (IOSSuccess?, IOSFailure?) -> Unit): IOSOperation =
        client.operation(completion) {
            val name = username.normalizedUsername()
            (if (enabled) profileRepository.observeUser(name) else profileRepository.unobserveUser(name)).getOrThrow()
            IOSSuccess()
        }

    fun setBlacklisted(
        username: String,
        enabled: Boolean,
        completion: (IOSSuccess?, IOSFailure?) -> Unit,
    ): IOSOperation = client.operation(completion) {
        val name = username.normalizedUsername()
        (if (enabled) blacklistsRepository.addBlacklistedUser(name) else blacklistsRepository.removeBlacklistedUser(name))
            .getOrThrow()
        IOSSuccess()
    }

    fun firstSectionRequest(): IOSPageRequest = PaginationMode.Numbered.initialRequest().toIOS()

    /**
     * [section]: actions, entriesAdded, entriesVoted, entriesCommented, linksAdded, linksPublished,
     * linksUp, linksDown, linksCommented, linksRelated, followers, followingTags, followingUsers.
     */
    fun section(
        username: String,
        section: String,
        request: IOSPageRequest,
        loaded: Int,
        completion: (IOSProfileSectionPage?, IOSFailure?) -> Unit,
    ): IOSOperation = client.operation(completion) {
        val name = username.normalizedUsername()
        val page = request.toDomain()
        val number = (page as? PageRequest.Number)?.value ?: throw IllegalArgumentException("numbered page expected")
        val repository = profileRepository
        when (section) {
            "followers", "followingUsers" -> {
                val result = (
                    if (section == "followers") repository.getProfileObservedUsersFollowers(name, number)
                    else repository.getProfileObservedUsersFollowing(name, number)
                    ).getOrThrow()
                result.sectionPage(page, loaded, users = result.data.map {
                    IOSProfileUser(
                        it.username, it.avatar, it.color.toIOS(), it.gender.toIOS(),
                        it.online, it.verified, it.company, it.status,
                    )
                })
            }
            "followingTags" -> {
                val result = repository.getProfileObservedTags(name, number).getOrThrow()
                result.sectionPage(page, loaded, tags = result.data.map { IOSProfileTag(it.name, it.pinned) })
            }
            else -> {
                val result = when (section) {
                    "actions" -> repository.getProfileActions(name, number)
                    "entriesAdded" -> repository.getProfileEntriesAdded(name, number)
                    "entriesVoted" -> repository.getProfileEntriesVoted(name, number)
                    "entriesCommented" -> repository.getProfileEntriesCommented(name, number)
                    "linksAdded" -> repository.getProfileLinksAdded(name, number)
                    "linksPublished" -> repository.getProfileLinksPublished(name, number)
                    "linksUp" -> repository.getProfileLinksUp(name, number)
                    "linksDown" -> repository.getProfileLinksDown(name, number)
                    "linksCommented" -> repository.getProfileLinksCommented(name, number)
                    "linksRelated" -> repository.getProfileLinksRelated(name, number)
                    else -> throw IllegalArgumentException("invalid profile section")
                }.getOrThrow()
                result.sectionPage(page, loaded, resources = result.data.map(ResourceItem::toIOSResource))
            }
        }
    }

    private fun PaginatedData<*>.sectionPage(
        request: PageRequest,
        loaded: Int,
        resources: List<IOSResource> = emptyList(),
        users: List<IOSProfileUser> = emptyList(),
        tags: List<IOSProfileTag> = emptyList(),
    ) = IOSProfileSectionPage(
        resources = resources,
        users = users,
        tags = tags,
        next = nextAfter(PaginationMode.Numbered, request, loaded),
        total = pagination?.total,
    )

    private fun String.normalizedUsername(): String = trim().removePrefix("@").also {
        require(it.isNotBlank()) { "empty username" }
    }
}

class BlacklistsService internal constructor(private val client: PodkopClient) {
    private val blacklistsRepository: BlacklistsRepository = client.koin.get()

    fun firstRequest(): IOSPageRequest = PaginationMode.Numbered.initialRequest().toIOS()

    /** Applies the shared normalization for [category]: users, tags, domains. */
    fun normalize(category: String, value: String): String = when (category) {
        "users" -> BlacklistNormalization.user(value)
        "tags" -> BlacklistNormalization.tag(value)
        "domains" -> BlacklistNormalization.domain(value)
        else -> ""
    }

    fun load(
        category: String,
        request: IOSPageRequest,
        loaded: Int,
        completion: (IOSBlacklistPage?, IOSFailure?) -> Unit,
    ): IOSOperation = client.operation(completion) {
        val page = request.toDomain()
        val number = (page as? PageRequest.Number)?.value ?: throw IllegalArgumentException("numbered page expected")
        when (category) {
            "users" -> blacklistsRepository.getBlacklistedUsers(number).getOrThrow().let { result ->
                result.blacklistPage(page, loaded) {
                    IOSBlacklistEntry(it.username, it.avatarUrl, it.color.toIOS(), it.gender.toIOS())
                }
            }
            "tags" -> blacklistsRepository.getBlacklistedTags(number).getOrThrow().let { result ->
                result.blacklistPage(page, loaded) { IOSBlacklistEntry(it.name, null, null, null) }
            }
            "domains" -> blacklistsRepository.getBlacklistedDomains(number).getOrThrow().let { result ->
                result.blacklistPage(page, loaded) { IOSBlacklistEntry(it.domain, null, null, null) }
            }
            else -> throw IllegalArgumentException("invalid blacklist category")
        }
    }

    fun add(category: String, value: String, completion: (IOSSuccess?, IOSFailure?) -> Unit): IOSOperation =
        client.operation(completion) {
            val normalized = normalize(category, value)
            require(normalized.isNotBlank()) { "empty blacklist value" }
            when (category) {
                "users" -> blacklistsRepository.addBlacklistedUser(normalized)
                "tags" -> blacklistsRepository.addBlacklistedTag(normalized)
                "domains" -> blacklistsRepository.addBlacklistedDomain(normalized)
                else -> throw IllegalArgumentException("invalid blacklist category")
            }.getOrThrow()
            IOSSuccess()
        }

    fun remove(category: String, value: String, completion: (IOSSuccess?, IOSFailure?) -> Unit): IOSOperation =
        client.operation(completion) {
            require(value.isNotBlank()) { "empty blacklist value" }
            when (category) {
                "users" -> blacklistsRepository.removeBlacklistedUser(value)
                "tags" -> blacklistsRepository.removeBlacklistedTag(value)
                "domains" -> blacklistsRepository.removeBlacklistedDomain(value)
                else -> throw IllegalArgumentException("invalid blacklist category")
            }.getOrThrow()
            IOSSuccess()
        }

    private fun <T> PaginatedData<T>.blacklistPage(
        request: PageRequest,
        loaded: Int,
        map: (T) -> IOSBlacklistEntry,
    ) = IOSBlacklistPage(
        items = data.map(map),
        next = nextAfter(PaginationMode.Numbered, request, loaded),
        // Android shows the page size when the server omits a total.
        total = pagination?.total ?: data.size,
    )
}

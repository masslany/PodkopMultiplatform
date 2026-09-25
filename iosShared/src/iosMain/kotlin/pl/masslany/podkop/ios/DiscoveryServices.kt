package pl.masslany.podkop.ios

import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.business.common.domain.models.common.Gender
import pl.masslany.podkop.business.common.domain.models.common.NameColor
import pl.masslany.podkop.business.common.domain.models.common.PaginatedData
import pl.masslany.podkop.business.common.domain.models.common.Pagination
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.favourites.domain.main.FavouritesRepository
import pl.masslany.podkop.business.favourites.domain.models.request.FavouritesResourceType
import pl.masslany.podkop.business.favourites.domain.models.request.FavouritesSortType
import pl.masslany.podkop.business.hits.domain.main.HitsRepository
import pl.masslany.podkop.business.hits.domain.models.request.HitsSortType
import pl.masslany.podkop.business.observed.domain.main.ObservedRepository
import pl.masslany.podkop.business.observed.domain.models.request.ObservedType
import pl.masslany.podkop.business.profile.domain.main.ProfileRepository
import pl.masslany.podkop.business.rank.domain.main.RankRepository
import pl.masslany.podkop.business.search.domain.main.SearchRepository
import pl.masslany.podkop.business.search.domain.models.request.SearchDatePreset
import pl.masslany.podkop.business.search.domain.models.request.SearchQueryInput
import pl.masslany.podkop.business.search.domain.models.request.SearchQueryResult
import pl.masslany.podkop.business.search.domain.models.request.SearchSort
import pl.masslany.podkop.business.search.domain.models.request.SearchStreamQuery
import pl.masslany.podkop.business.search.domain.models.request.withSearchFallbackPagination
import pl.masslany.podkop.business.tags.domain.main.TagsRepository
import pl.masslany.podkop.common.pagination.PageRequest
import pl.masslany.podkop.common.pagination.PaginationMode
import pl.masslany.podkop.common.pagination.initialRequest
import pl.masslany.podkop.common.pagination.nextRequest
import pl.masslany.podkop.features.pagination.FeaturePaginationPolicies

class IOSTagSuggestion(val name: String, val followers: Int)
class IOSUserSuggestion(val username: String, val avatarUrl: String, val color: String, val gender: String)

/** A resource page whose [next] request is already resolved; null means the list is exhausted. */
class IOSResourceListPage(val items: List<IOSResource>, val next: IOSPageRequest?, val total: Int?)
class IOSObservedItem(val resource: IOSResource, val newContentCount: Int?)
class IOSObservedPage(val items: List<IOSObservedItem>, val next: IOSPageRequest?, val total: Int?)
class IOSRankUser(
    val username: String,
    val avatarUrl: String,
    val color: String,
    val gender: String,
    val memberSince: String?,
    val position: Int,
    val trend: Int,
    val actions: Int,
    val links: Int,
    val entries: Int,
    val followers: Int,
)
class IOSRankPage(val items: List<IOSRankUser>, val next: IOSPageRequest?, val total: Int?)

/** Opaque, already validated advanced search request. The date window is fixed when built. */
class IOSSearchQuery internal constructor(internal val value: SearchStreamQuery)

/** Exactly one of [query] or [error] is set. Errors: queryRequired, invalidCustomDateFormat, invalidCustomDateRange. */
class IOSSearchQueryResult(val query: IOSSearchQuery?, val error: String?)

class SearchService internal constructor(private val client: PodkopClient) {
    private val tagsRepository: TagsRepository = client.koin.get()
    private val profileRepository: ProfileRepository = client.koin.get()
    private val authRepository: AuthRepository = client.koin.get()
    private val searchRepository: SearchRepository = client.koin.get()

    fun tags(query: String, completion: (List<IOSTagSuggestion>?, IOSFailure?) -> Unit): IOSOperation =
        client.operation(completion) {
            require(query.trim().length >= MIN_SUGGESTION_QUERY) { "query too short" }
            tagsRepository.getAutoCompleteTags(query.trim()).getOrThrow().tags.map {
                IOSTagSuggestion(it.name, it.observedQuantity)
            }
        }

    fun users(query: String, completion: (List<IOSUserSuggestion>?, IOSFailure?) -> Unit): IOSOperation =
        client.operation(completion) {
            require(authRepository.isLoggedIn()) { "account required" }
            require(query.trim().length >= MIN_SUGGESTION_QUERY) { "query too short" }
            profileRepository.getUsersAutoComplete(query.trim()).getOrThrow().users.map {
                IOSUserSuggestion(it.username, it.avatarUrl, it.color.toIOS(), it.gender.toIOS())
            }
        }

    /** Synchronous and side-effect free; call again whenever the form changes. */
    fun build(
        query: String,
        sort: String,
        minimumVotes: Int,
        datePreset: String,
        customDateFrom: String,
        customDateTo: String,
        tags: String,
        users: String,
        domains: String,
        category: String,
    ): IOSSearchQueryResult {
        val input = SearchQueryInput(
            query = query,
            sort = SearchSort.entries.firstOrNull { it.value == sort }
                ?: return IOSSearchQueryResult(null, "invalidSort"),
            minimumVotes = minimumVotes.takeIf { it > 0 },
            datePreset = SearchDatePreset.entries.firstOrNull { it.name.equals(datePreset, ignoreCase = true) }
                ?: return IOSSearchQueryResult(null, "invalidDatePreset"),
            customDateFrom = customDateFrom,
            customDateTo = customDateTo,
            tags = tags,
            users = users,
            domains = domains,
            category = category,
        )
        return when (val result = input.build()) {
            is SearchQueryResult.Success -> IOSSearchQueryResult(IOSSearchQuery(result.query), null)
            is SearchQueryResult.Invalid -> IOSSearchQueryResult(null, result.error.name.replaceFirstChar(Char::lowercase))
        }
    }

    fun firstStreamRequest(): IOSPageRequest = PaginationMode.Numbered.initialRequest().toIOS()

    fun stream(
        query: IOSSearchQuery,
        request: IOSPageRequest,
        loaded: Int,
        completion: (IOSResourceListPage?, IOSFailure?) -> Unit,
    ): IOSOperation = client.operation(completion) {
        val page = request.toDomain()
        val number = (page as? PageRequest.Number)?.value ?: throw IllegalArgumentException("numbered page expected")
        searchRepository.getSearchStream(number, null, query.value).getOrThrow()
            .withSearchFallbackPagination(currentItemCount = loaded, currentPage = number)
            .toResourceListPage(PaginationMode.Numbered, page, loaded)
    }

    private companion object {
        const val MIN_SUGGESTION_QUERY = 3
    }
}

class HitsService internal constructor(private val client: PodkopClient) {
    private val hitsRepository: HitsRepository = client.koin.get()

    fun firstRequest(): IOSPageRequest = PaginationMode.Numbered.initialRequest().toIOS()

    /** [year] and [month] select an archive month and require sort `all`; pass 0 for neither. */
    fun load(
        sort: String,
        year: Int,
        month: Int,
        request: IOSPageRequest,
        loaded: Int,
        completion: (IOSResourceListPage?, IOSFailure?) -> Unit,
    ): IOSOperation = client.operation(completion) {
        val sortType = when (sort) {
            "all" -> HitsSortType.All
            "day" -> HitsSortType.Day
            "week" -> HitsSortType.Week
            "month" -> HitsSortType.Month
            "year" -> HitsSortType.Year
            else -> throw IllegalArgumentException("invalid hits sort")
        }
        val archive = year > 0 || month > 0
        require(!archive || (sortType == HitsSortType.All && month in 1..12)) { "invalid archive" }
        val page = request.toDomain()
        val number = (page as? PageRequest.Number)?.value ?: throw IllegalArgumentException("numbered page expected")
        hitsRepository.getLinkHits(
            page = number,
            hitsSortType = sortType,
            year = year.takeIf { archive },
            month = month.takeIf { archive },
        ).getOrThrow().toResourceListPage(PaginationMode.Numbered, page, loaded)
    }
}

class RankService internal constructor(private val client: PodkopClient) {
    private val rankRepository: RankRepository = client.koin.get()

    fun firstRequest(): IOSPageRequest = PaginationMode.Numbered.initialRequest().toIOS()

    fun load(
        request: IOSPageRequest,
        loaded: Int,
        completion: (IOSRankPage?, IOSFailure?) -> Unit,
    ): IOSOperation = client.operation(completion) {
        val page = request.toDomain()
        val number = (page as? PageRequest.Number)?.value ?: throw IllegalArgumentException("numbered page expected")
        val result = rankRepository.getRank(number).getOrThrow()
        IOSRankPage(
            items = result.data.map {
                IOSRankUser(
                    username = it.username,
                    avatarUrl = it.avatarUrl,
                    color = it.color.toIOS(),
                    gender = it.gender.toIOS(),
                    memberSince = it.memberSince?.toString(),
                    position = it.rank.position,
                    trend = it.rank.trend,
                    actions = it.summary.actions,
                    links = it.summary.links,
                    entries = it.summary.entries,
                    followers = it.summary.followers,
                )
            },
            next = result.nextAfter(PaginationMode.Numbered, page, loaded),
            total = result.pagination?.total,
        )
    }
}

class FavouritesService internal constructor(private val client: PodkopClient) {
    private val favouritesRepository: FavouritesRepository = client.koin.get()
    private val authRepository: AuthRepository = client.koin.get()

    fun firstRequest(isLoggedIn: Boolean): IOSPageRequest =
        FeaturePaginationPolicies.favourites(isLoggedIn).initialRequest().toIOS()

    /** [type]: all, link, entry, linkComment, entryComment. [sort]: newest, oldest. */
    fun load(
        sort: String,
        type: String,
        request: IOSPageRequest,
        loaded: Int,
        completion: (IOSResourceListPage?, IOSFailure?) -> Unit,
    ): IOSOperation = client.operation(completion) {
        val sortType = FavouritesSortType.entries.firstOrNull { it.value == sort }
            ?: throw IllegalArgumentException("invalid favourites sort")
        val resourceType = when (type) {
            "all" -> FavouritesResourceType.All
            "link" -> FavouritesResourceType.Link
            "entry" -> FavouritesResourceType.Entry
            "linkComment" -> FavouritesResourceType.LinkComment
            "entryComment" -> FavouritesResourceType.EntryComment
            else -> throw IllegalArgumentException("invalid favourites type")
        }
        val mode = FeaturePaginationPolicies.favourites(authRepository.isLoggedIn())
        val page = request.toDomain()
        favouritesRepository.getFavourites(page, sortType, resourceType).getOrThrow()
            .toResourceListPage(mode, page, loaded)
    }
}

class ObservedService internal constructor(private val client: PodkopClient) {
    private val observedRepository: ObservedRepository = client.koin.get()

    fun firstRequest(): IOSPageRequest = PaginationMode.CursorInPage.initialRequest().toIOS()

    /** [type]: all, profiles, discussions, tags. */
    fun load(
        type: String,
        request: IOSPageRequest,
        loaded: Int,
        completion: (IOSObservedPage?, IOSFailure?) -> Unit,
    ): IOSOperation = client.operation(completion) {
        val observedType = when (type) {
            "all" -> ObservedType.All
            "profiles" -> ObservedType.Profiles
            "discussions" -> ObservedType.Discussions
            "tags" -> ObservedType.Tags
            else -> throw IllegalArgumentException("invalid observed type")
        }
        val page = request.toDomain()
        val result = observedRepository.getObserved(page, observedType).getOrThrow()
        IOSObservedPage(
            items = result.data.map { IOSObservedItem(it.item.toIOSResource(), it.newContentCount) },
            next = result.nextAfter(PaginationMode.CursorInPage, page, loaded),
            total = result.pagination?.total,
        )
    }
}

private fun PaginatedData<ResourceItem>.toResourceListPage(
    mode: PaginationMode,
    request: PageRequest,
    loaded: Int,
) = IOSResourceListPage(
    items = data.map(ResourceItem::toIOSResource),
    next = nextAfter(mode, request, loaded),
    total = pagination?.total,
)

internal fun PaginatedData<*>.nextAfter(mode: PaginationMode, request: PageRequest, loaded: Int): IOSPageRequest? =
    nextPageRequest(mode, request, pagination, received = data.size, loaded = loaded)?.toIOS()

/**
 * Mirrors the Android `Paginator` stop rules so native lists page identically: a cursor that did
 * not advance, an empty page, a reached total, or a short numbered page ends the list.
 */
internal fun nextPageRequest(
    mode: PaginationMode,
    request: PageRequest,
    pagination: Pagination?,
    received: Int,
    loaded: Int,
): PageRequest? {
    val requestedCursor = when (request) {
        is PageRequest.PageCursor -> request.value
        is PageRequest.KeyCursor -> request.value
        PageRequest.Initial, is PageRequest.Number -> null
    }
    if (requestedCursor != null && requestedCursor.isNotBlank() && pagination?.next == requestedCursor) return null

    val emitted = loaded + received
    val hasNextCursor = pagination != null && pagination.next.isNotBlank()
    val total = pagination?.total?.takeIf { it > 0 }
    val reachedEnd = when (mode) {
        PaginationMode.CursorInPage, PaginationMode.CursorInKey ->
            !hasNextCursor || received == 0 || (total != null && emitted >= total)
        PaginationMode.Numbered -> when {
            received == 0 -> true
            total != null && emitted >= total -> true
            hasNextCursor -> false
            else -> {
                val perPage = pagination?.perPage?.takeIf { it > 0 }
                when {
                    perPage != null -> received < perPage
                    pagination != null -> true
                    else -> false
                }
            }
        }
    }
    if (reachedEnd) return null
    val nextNumber = (request as? PageRequest.Number)?.value?.plus(1) ?: 2
    return mode.nextRequest(pagination?.next, nextNumber)
}

internal fun Gender.toIOS(): String = when (this) {
    Gender.Male -> "male"
    Gender.Female -> "female"
    Gender.Unspecified -> "unspecified"
}

internal fun NameColor.toIOS(): String = when (this) {
    NameColor.Orange -> "orange"
    NameColor.Burgundy -> "burgundy"
    NameColor.Green -> "green"
    NameColor.Black -> "black"
}

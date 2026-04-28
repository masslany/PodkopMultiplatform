package pl.masslany.podkop.business.common.data.network.client

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import pl.masslany.podkop.business.blacklists.data.network.client.BlacklistsApiClient
import pl.masslany.podkop.business.entries.data.network.client.EntriesApiClient
import pl.masslany.podkop.business.favourites.data.network.client.FavouritesApiClient
import pl.masslany.podkop.business.links.data.network.client.LinksApiClient
import pl.masslany.podkop.business.notifications.data.network.client.NotificationsApiClient
import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.business.observed.data.network.client.ObservedApiClient
import pl.masslany.podkop.business.observed.domain.models.request.ObservedType
import pl.masslany.podkop.business.profile.data.network.client.ProfileApiClient
import pl.masslany.podkop.business.privatemessages.data.network.client.PrivateMessagesApiClient
import pl.masslany.podkop.business.rank.data.network.client.RankApiClient
import pl.masslany.podkop.business.search.data.network.client.SearchApiClient
import pl.masslany.podkop.business.search.domain.models.request.SearchStreamQuery
import pl.masslany.podkop.business.tags.data.network.client.TagsApiClient
import pl.masslany.podkop.common.network.api.ApiClient
import pl.masslany.podkop.common.network.models.request.Request
import pl.masslany.podkop.common.network.models.response.ApiResponse
import pl.masslany.podkop.common.network.models.response.ResponseTypeInfo
import pl.masslany.podkop.common.pagination.PageRequest

class PaginationApiClientRequestTest {

    @Test
    fun `tag logged in cursor uses page query parameter`() = runBlocking {
        val apiClient = RecordingApiClient()

        TagsApiClient(apiClient).getTagStream(
            tagName = "gry",
            page = PageRequest.PageCursor("tag-cursor"),
            limit = null,
            sort = "all",
            type = "all",
        )

        apiClient.lastRequest.assertUsesOnlyPagination(name = "page", value = "tag-cursor")
    }

    @Test
    fun `notifications cursor groups use key query parameter`() = runBlocking {
        listOf(
            NotificationGroup.Entries,
            NotificationGroup.Tags,
            NotificationGroup.ObservedDiscussions,
        ).forEach { group ->
            val apiClient = RecordingApiClient()

            NotificationsApiClient(apiClient).getNotifications(
                group = group,
                page = PageRequest.KeyCursor("notification-key"),
            )

            apiClient.lastRequest.assertUsesOnlyPagination(name = "key", value = "notification-key")
        }
    }

    @Test
    fun `observed and logged in feeds use page cursor query parameter`() = runBlocking<Unit> {
        RecordingApiClient().also { apiClient ->
            ObservedApiClient(apiClient).getObserved(
                page = PageRequest.PageCursor("observed-cursor"),
                type = ObservedType.All,
            )

            apiClient.lastRequest.assertUsesOnlyPagination(name = "page", value = "observed-cursor")
        }

        RecordingApiClient().also { apiClient ->
            EntriesApiClient(apiClient).getEntries(
                page = PageRequest.PageCursor("entry-cursor"),
                limit = null,
                sort = "newest",
                hotSort = 2,
                category = null,
                bucket = null,
            )

            apiClient.lastRequest.assertUsesOnlyPagination(name = "page", value = "entry-cursor")
        }

        RecordingApiClient().also { apiClient ->
            LinksApiClient(apiClient).getLinks(
                page = PageRequest.PageCursor("link-cursor"),
                limit = null,
                sort = "newest",
                type = "homepage",
                category = null,
                bucket = null,
            )

            apiClient.lastRequest.assertUsesOnlyPagination(name = "page", value = "link-cursor")
        }

        RecordingApiClient().also { apiClient ->
            FavouritesApiClient(apiClient).getFavourites(
                page = PageRequest.PageCursor("favourite-cursor"),
                sort = "newest",
                resource = null,
            )

            apiClient.lastRequest.assertUsesOnlyPagination(name = "page", value = "favourite-cursor")
        }
    }

    @Test
    fun `logged in favourites initial request omits pagination query parameter`() = runBlocking {
        val apiClient = RecordingApiClient()

        FavouritesApiClient(apiClient).getFavourites(
            page = PageRequest.Initial,
            sort = "newest",
            resource = null,
        )

        apiClient.lastRequest.assertNoPagination()
    }

    @Test
    fun `numbered endpoints use numeric page query parameter`() = runBlocking<Unit> {
        RecordingApiClient().also { apiClient ->
            FavouritesApiClient(apiClient).getFavourites(
                page = PageRequest.Number(3),
                sort = "newest",
                resource = null,
            )
            apiClient.lastRequest.assertUsesOnlyPagination(name = "page", value = "3")
        }

        RecordingApiClient().also { apiClient ->
            SearchApiClient(apiClient).getSearchStream(
                page = 4,
                limit = null,
                query = SearchStreamQuery(query = "test"),
            )
            apiClient.lastRequest.assertUsesOnlyPagination(name = "page", value = "4")
        }

        RecordingApiClient().also { apiClient ->
            RankApiClient(apiClient).getRank(page = 5)
            apiClient.lastRequest.assertUsesOnlyPagination(name = "page", value = "5")
        }

        RecordingApiClient().also { apiClient ->
            BlacklistsApiClient(apiClient).getBlacklistedUsers(page = 6)
            apiClient.lastRequest.assertUsesOnlyPagination(name = "page", value = "6")
        }

        RecordingApiClient().also { apiClient ->
            ProfileApiClient(apiClient).getProfileActions(username = "john", page = 7)
            apiClient.lastRequest.assertUsesOnlyPagination(name = "page", value = "7")
        }

        RecordingApiClient().also { apiClient ->
            PrivateMessagesApiClient(apiClient).getConversations(page = 8)
            apiClient.lastRequest.assertUsesOnlyPagination(name = "page", value = "8")
        }
    }

    @Test
    fun `details comments and votes use numeric page query parameter`() = runBlocking<Unit> {
        RecordingApiClient().also { apiClient ->
            EntriesApiClient(apiClient).getEntryComments(entryId = 1, page = 2)
            apiClient.lastRequest.assertUsesOnlyPagination(name = "page", value = "2")
        }

        RecordingApiClient().also { apiClient ->
            EntriesApiClient(apiClient).getEntryVotes(entryId = 1, page = 3)
            apiClient.lastRequest.assertUsesOnlyPagination(name = "page", value = "3")
        }

        RecordingApiClient().also { apiClient ->
            LinksApiClient(apiClient).getComments(
                id = 1,
                page = 4,
                limit = null,
                sort = "best",
                ama = null,
            )
            apiClient.lastRequest.assertUsesOnlyPagination(name = "page", value = "4")
        }

        RecordingApiClient().also { apiClient ->
            LinksApiClient(apiClient).getLinkUpvotes(linkId = 1, type = "up", page = 5)
            apiClient.lastRequest.assertUsesOnlyPagination(name = "page", value = "5")
        }
    }
}

private fun Request<*>.assertUsesOnlyPagination(
    name: String,
    value: String,
) {
    val query = queryParameters.orEmpty()

    assertEquals(value, query[name])
    if (name == "page") {
        assertFalse("key" in query)
    } else {
        assertFalse("page" in query)
    }
    assertTrue(query.any { (key, _) -> key == name })
}

private fun Request<*>.assertNoPagination() {
    val query = queryParameters.orEmpty()

    assertFalse("page" in query)
    assertFalse("key" in query)
}

private class RecordingApiClient : ApiClient() {
    lateinit var lastRequest: Request<*>

    override suspend fun <T> executeRequest(
        request: Request<T>,
        responseType: ResponseTypeInfo,
    ): Result<ApiResponse<T>> {
        lastRequest = request
        return Result.failure(RecordedRequestException)
    }
}

private object RecordedRequestException : RuntimeException("Request recorded")

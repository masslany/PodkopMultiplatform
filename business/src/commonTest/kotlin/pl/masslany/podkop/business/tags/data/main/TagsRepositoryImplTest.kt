package pl.masslany.podkop.business.tags.data.main

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import pl.masslany.podkop.business.tags.domain.models.request.TagsSort
import pl.masslany.podkop.business.tags.domain.models.request.TagsType
import pl.masslany.podkop.business.testsupport.fakes.FakeDispatcherProvider
import pl.masslany.podkop.business.testsupport.fakes.FakeTagsDataSource
import pl.masslany.podkop.business.testsupport.fixtures.BusinessFixtures as Fixtures

class TagsRepositoryImplTest {

    @Test
    fun `get tag details maps single resource response`() = runBlocking {
        val tagsDataSource = FakeTagsDataSource().apply {
            getTagDetailsResult = Result.success(
                Fixtures.tagDetailsResponseDto(
                    data = Fixtures.tagDetailsDto(
                        blacklist = true,
                        name = "compose",
                        description = "Compose tag",
                        followers = 44,
                        media = Fixtures.mediaDto(photo = Fixtures.photoDto(url = "https://example.com/banner.jpg")),
                        follow = true,
                        notifications = true,
                        actions = Fixtures.tagDetailsActionsDto(blacklist = false),
                    ),
                ),
            )
        }
        val sut = TagsRepositoryImpl(
            tagsDataSource = tagsDataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )

        val actual = sut.getTagDetails("kotlin")

        assertEquals(listOf("kotlin"), tagsDataSource.getTagDetailsCalls)
        assertEquals(
            Fixtures.tagDetails(
                name = "compose",
                description = "Compose tag",
                followers = 44,
                media = Fixtures.media(photo = Fixtures.photo(url = "https://example.com/banner.jpg")),
                isObserved = true,
                areNotificationsEnabled = true,
                isBlacklisted = true,
            ),
            actual.getOrThrow(),
        )
    }

    @Test
    fun `get tag stream maps resources and passes enum values`() = runBlocking {
        val tagsDataSource = FakeTagsDataSource().apply {
            getTagStreamResult = Result.success(Fixtures.resourceResponseDto())
        }
        val sut = TagsRepositoryImpl(
            tagsDataSource = tagsDataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )

        val actual = sut.getTagStream(
            tagName = "compose",
            page = "cursor",
            limit = 25,
            sort = TagsSort.Best,
            type = TagsType.Links,
        )

        assertEquals(
            listOf(
                FakeTagsDataSource.GetTagStreamCall(
                    tagName = "compose",
                    page = "cursor",
                    limit = 25,
                    sort = "best",
                    type = "link",
                ),
            ),
            tagsDataSource.getTagStreamCalls,
        )
        assertEquals(Fixtures.resources(), actual.getOrThrow())
    }

    @Test
    fun `observe tag forwards tag name`() = runBlocking {
        val tagsDataSource = FakeTagsDataSource().apply {
            observeTagResult = Result.success(Unit)
        }
        val sut = TagsRepositoryImpl(
            tagsDataSource = tagsDataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )

        val actual = sut.observeTag("compose")

        assertEquals(Unit, actual.getOrThrow())
        assertEquals(listOf("compose"), tagsDataSource.observeTagCalls)
    }

    @Test
    fun `unobserve tag forwards tag name`() = runBlocking {
        val tagsDataSource = FakeTagsDataSource().apply {
            unobserveTagResult = Result.success(Unit)
        }
        val sut = TagsRepositoryImpl(
            tagsDataSource = tagsDataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )

        val actual = sut.unobserveTag("compose")

        assertEquals(Unit, actual.getOrThrow())
        assertEquals(listOf("compose"), tagsDataSource.unobserveTagCalls)
    }

    @Test
    fun `enable tag notifications forwards tag name`() = runBlocking {
        val tagsDataSource = FakeTagsDataSource().apply {
            enableTagNotificationsResult = Result.success(Unit)
        }
        val sut = TagsRepositoryImpl(
            tagsDataSource = tagsDataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )

        val actual = sut.enableTagNotifications("compose")

        assertEquals(Unit, actual.getOrThrow())
        assertEquals(listOf("compose"), tagsDataSource.enableTagNotificationsCalls)
    }

    @Test
    fun `disable tag notifications forwards tag name`() = runBlocking {
        val tagsDataSource = FakeTagsDataSource().apply {
            disableTagNotificationsResult = Result.success(Unit)
        }
        val sut = TagsRepositoryImpl(
            tagsDataSource = tagsDataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )

        val actual = sut.disableTagNotifications("compose")

        assertEquals(Unit, actual.getOrThrow())
        assertEquals(listOf("compose"), tagsDataSource.disableTagNotificationsCalls)
    }

    @Test
    fun `get tags sorts returns supported values in order`() {
        val sut = TagsRepositoryImpl(
            tagsDataSource = FakeTagsDataSource(),
            dispatcherProvider = FakeDispatcherProvider(),
        )

        assertEquals(listOf(TagsSort.All, TagsSort.Best), sut.getTagsSorts())
    }

    @Test
    fun `get tags types returns supported values in order`() {
        val sut = TagsRepositoryImpl(
            tagsDataSource = FakeTagsDataSource(),
            dispatcherProvider = FakeDispatcherProvider(),
        )

        assertEquals(listOf(TagsType.All, TagsType.Entries, TagsType.Links), sut.getTagsTypes())
    }

    @Test
    fun `get auto complete tags maps response and forwards query`() = runBlocking {
        val tagsDataSource = FakeTagsDataSource().apply {
            getTagsAutoCompleteResult = Result.success(
                Fixtures.tagsAutoCompleteResponseDto(
                    data = listOf(Fixtures.tagsAutoCompleteDataDto(name = "#compose", observedQuantity = 44)),
                ),
            )
        }
        val sut = TagsRepositoryImpl(
            tagsDataSource = tagsDataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )

        val actual = sut.getAutoCompleteTags("com")

        assertEquals(listOf("com"), tagsDataSource.getTagsAutoCompleteCalls)
        assertEquals(
            Fixtures.tagsAutoComplete(tags = listOf(Fixtures.tagsAutoCompleteItem(name = "#compose", observedQuantity = 44))),
            actual.getOrThrow(),
        )
    }

    @Test
    fun `get tag stream propagates failure`() = runBlocking {
        val expected = IllegalArgumentException("bad request")
        val tagsDataSource = FakeTagsDataSource().apply {
            getTagStreamResult = Result.failure(expected)
        }
        val sut = TagsRepositoryImpl(
            tagsDataSource = tagsDataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )

        val actual = sut.getTagStream("tag", null, null, TagsSort.All, TagsType.All)

        assertTrue(actual.isFailure)
        assertSame(expected, actual.exceptionOrNull())
    }
}

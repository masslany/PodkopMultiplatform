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
                Fixtures.singleResourceResponseDto(
                    data = Fixtures.resourceItemDto(id = 321, resource = "entry", voted = -1),
                ),
            )
        }
        val sut = TagsRepositoryImpl(
            tagsDataSource = tagsDataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )

        val actual = sut.getTagDetails("kotlin")

        assertEquals(listOf("kotlin"), tagsDataSource.getTagDetailsCalls)
        assertEquals(Fixtures.resourceItem(id = 321, resource = pl.masslany.podkop.business.common.domain.models.common.Resource.Entry, voted = pl.masslany.podkop.business.common.domain.models.common.Voted.Negative), actual.getOrThrow())
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

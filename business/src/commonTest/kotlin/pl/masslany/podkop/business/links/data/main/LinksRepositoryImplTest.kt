package pl.masslany.podkop.business.links.data.main

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import pl.masslany.podkop.business.common.data.network.models.common.MediaDto
import pl.masslany.podkop.business.common.data.network.models.common.PhotoDto
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.VoteReason
import pl.masslany.podkop.business.common.domain.models.common.Voted
import pl.masslany.podkop.business.links.data.network.models.LinkDraftCheckDataDto
import pl.masslany.podkop.business.links.data.network.models.LinkDraftCheckResponseDto
import pl.masslany.podkop.business.links.data.network.models.LinkDraftDto
import pl.masslany.podkop.business.links.data.network.models.LinkDraftImageDto
import pl.masslany.podkop.business.links.data.network.models.LinkDraftResponseDto
import pl.masslany.podkop.business.links.data.network.models.LinkDraftsResponseDto
import pl.masslany.podkop.business.links.domain.models.request.CommentsSortType
import pl.masslany.podkop.business.links.domain.models.request.LinksSortType
import pl.masslany.podkop.business.links.domain.models.request.LinksType
import pl.masslany.podkop.business.links.domain.models.request.PublishLinkDraft
import pl.masslany.podkop.business.links.domain.models.request.UpdateLinkDraft
import pl.masslany.podkop.business.testsupport.fakes.FakeDispatcherProvider
import pl.masslany.podkop.business.testsupport.fakes.FakeLinksDataSource
import pl.masslany.podkop.business.testsupport.fixtures.BusinessFixtures as Fixtures

class LinksRepositoryImplTest {

    @Test
    fun `get links maps resources and forwards enum values`() = runBlocking {
        val linksDataSource = FakeLinksDataSource().apply {
            getLinksResult = Result.success(
                Fixtures.resourceResponseDto(
                    data = listOf(Fixtures.resourceItemDto(id = 1, resource = "link")),
                ),
            )
        }
        val sut = createSut(linksDataSource)

        val actual = sut.getLinks(
            page = "cursor",
            limit = 20,
            linksSortType = LinksSortType.Commented,
            linksType = LinksType.UPCOMING,
            category = "science",
            bucket = "top",
        )

        assertEquals(
            listOf(
                FakeLinksDataSource.GetLinksCall(
                    page = "cursor",
                    limit = 20,
                    sort = "commented",
                    type = "upcoming",
                    category = "science",
                    bucket = "top",
                ),
            ),
            linksDataSource.getLinksCalls,
        )
        assertEquals(Fixtures.resources(data = listOf(Fixtures.resourceItem(id = 1))), actual.getOrThrow())
    }

    @Test
    fun `get links sort types returns homepage subset`() {
        val sut = createSut()

        assertEquals(
            listOf(LinksSortType.Newest, LinksSortType.Active),
            sut.getLinksSortTypes(isUpcoming = false),
        )
    }

    @Test
    fun `get links sort types returns upcoming extended list`() {
        val sut = createSut()

        assertEquals(
            listOf(
                LinksSortType.Newest,
                LinksSortType.Active,
                LinksSortType.Commented,
                LinksSortType.Digged,
            ),
            sut.getLinksSortTypes(isUpcoming = true),
        )
    }

    @Test
    fun `get comments sort types returns supported values in order`() {
        val sut = createSut()

        assertEquals(
            listOf(CommentsSortType.Best, CommentsSortType.Newest, CommentsSortType.Oldest),
            sut.getCommentsSortTypes(),
        )
    }

    @Test
    fun `get link maps single resource response to link`() = runBlocking {
        val linksDataSource = FakeLinksDataSource().apply {
            getLinkResult = Result.success(
                Fixtures.singleResourceResponseDto(
                    data = Fixtures.resourceItemDto(id = 55, resource = "link", voted = -1),
                ),
            )
        }
        val sut = createSut(linksDataSource)

        val actual = sut.getLink(55)

        assertEquals(listOf(55), linksDataSource.getLinkCalls)
        assertEquals(
            Fixtures.link(
                data = Fixtures.resourceItem(id = 55, resource = Resource.Link, voted = Voted.Negative),
            ),
            actual.getOrThrow(),
        )
    }

    @Test
    fun `get comments maps resources and forwards comment sort`() = runBlocking {
        val linksDataSource = FakeLinksDataSource().apply {
            getCommentsResult = Result.success(Fixtures.resourceResponseDto())
        }
        val sut = createSut(linksDataSource)

        val actual = sut.getComments(
            id = 10,
            page = 2,
            limit = 15,
            commentSortType = CommentsSortType.Oldest,
            ama = true,
        )

        assertEquals(
            listOf(
                FakeLinksDataSource.GetCommentsCall(
                    id = 10,
                    page = 2,
                    limit = 15,
                    sort = "oldest",
                    ama = true,
                ),
            ),
            linksDataSource.getCommentsCalls,
        )
        assertEquals(Fixtures.resources(), actual.getOrThrow())
    }

    @Test
    fun `get sub comments maps resources and forwards ids`() = runBlocking {
        val linksDataSource = FakeLinksDataSource().apply {
            getSubCommentsResult = Result.success(Fixtures.resourceResponseDto())
        }
        val sut = createSut(linksDataSource)

        val actual = sut.getSubComments(linkId = 1, commentId = 2, page = 3)

        assertEquals(
            listOf(FakeLinksDataSource.GetSubCommentsCall(linkId = 1, commentId = 2, page = 3)),
            linksDataSource.getSubCommentsCalls,
        )
        assertEquals(Fixtures.resources(), actual.getOrThrow())
    }

    @Test
    fun `get related links maps resources and forwards link id`() = runBlocking {
        val linksDataSource = FakeLinksDataSource().apply {
            getRelatedLinksResult = Result.success(
                Fixtures.resourceResponseDto(
                    data = listOf(
                        Fixtures.resourceItemDto(
                            id = 999,
                            resource = null,
                            actions = Fixtures.commonActionsDto(voteUp = true, voteDown = true),
                        ),
                    ),
                ),
            )
        }
        val sut = createSut(linksDataSource)

        val actual = sut.getRelatedLinks(linkId = 999)

        assertEquals(listOf(999), linksDataSource.getRelatedLinksCalls)
        assertEquals(
            Fixtures.resources(
                data = listOf(
                    Fixtures.resourceItem(
                        id = 999,
                        resource = Resource.Link,
                        actions = Fixtures.actions(voteUp = true, voteDown = true),
                    ),
                ),
            ),
            actual.getOrThrow(),
        )
    }

    @Test
    fun `create link draft forwards url and maps response`() = runBlocking {
        val linksDataSource = FakeLinksDataSource().apply {
            createLinkDraftResult = Result.success(
                LinkDraftCheckResponseDto(
                    data = LinkDraftCheckDataDto(
                        key = "draft-key-123",
                        similar = listOf(
                            Fixtures.resourceItemDto(
                                id = 91,
                                resource = "link",
                                title = "Existing similar link",
                            ),
                        ),
                        duplicate = true,
                    ),
                ),
            )
        }
        val sut = createSut(linksDataSource)

        val actual = sut.createLinkDraft(url = "https://example.com/article")

        assertEquals(
            listOf(FakeLinksDataSource.CreateLinkDraftCall(url = "https://example.com/article")),
            linksDataSource.createLinkDraftCalls,
        )
        assertEquals("draft-key-123", actual.getOrThrow().key)
        assertEquals(true, actual.getOrThrow().duplicate)
        assertEquals(
            listOf(
                Fixtures.resourceItem(
                    id = 91,
                    resource = Resource.Link,
                    title = "Existing similar link",
                ),
            ),
            actual.getOrThrow().similar,
        )
    }

    @Test
    fun `publish link draft forwards request`() = runBlocking {
        val linksDataSource = FakeLinksDataSource().apply {
            publishLinkDraftResult = Result.success(Unit)
        }
        val sut = createSut(linksDataSource)
        val request = PublishLinkDraft(
            title = "Fresh link",
            description = "Optional body",
            tags = listOf("news", "tech"),
            photoKey = "photo-key-77",
            adult = false,
            selectedImageIndex = 2,
        )

        val actual = sut.publishLinkDraft(
            key = "draft-key-123",
            request = request,
        )

        assertTrue(actual.isSuccess)
        assertEquals(
            listOf(
                FakeLinksDataSource.PublishLinkDraftCall(
                    key = "draft-key-123",
                    request = request,
                ),
            ),
            linksDataSource.publishLinkDraftCalls,
        )
    }

    @Test
    fun `get link draft forwards key and maps prefilled fields`() = runBlocking {
        val linksDataSource = FakeLinksDataSource().apply {
            getLinkDraftResult = Result.success(
                LinkDraftResponseDto(
                    data = LinkDraftDto(
                        key = "draft-key-123",
                        url = "https://example.com/article",
                        title = "Prefilled website title",
                        description = "Prefilled website description",
                        tags = listOf("news", "tech"),
                        adult = true,
                        images = listOf(
                            LinkDraftImageDto(
                                url = "https://example.com/server-image-1.jpg",
                                selected = false,
                            ),
                            LinkDraftImageDto(
                                url = "https://example.com/server-image-2.jpg",
                                selected = true,
                            ),
                        ),
                        media = MediaDto(
                            embed = null,
                            photo = PhotoDto(
                                key = "photo-key-1",
                                label = "draft-photo",
                                mimeType = "image/jpeg",
                                url = "https://example.com/photo.jpg",
                                size = 1024,
                                width = 640,
                                height = 480,
                            ),
                            survey = null,
                        ),
                    ),
                ),
            )
        }
        val sut = createSut(linksDataSource)

        val actual = sut.getLinkDraft(key = "draft-key-123")

        assertEquals(
            listOf(FakeLinksDataSource.GetLinkDraftCall(key = "draft-key-123")),
            linksDataSource.getLinkDraftCalls,
        )
        assertEquals("draft-key-123", actual.getOrThrow().key)
        assertEquals("https://example.com/article", actual.getOrThrow().url)
        assertEquals("Prefilled website title", actual.getOrThrow().title)
        assertEquals("Prefilled website description", actual.getOrThrow().description)
        assertEquals(listOf("news", "tech"), actual.getOrThrow().tags)
        assertEquals(true, actual.getOrThrow().adult)
        assertEquals("photo-key-1", actual.getOrThrow().photoKey)
        assertEquals("https://example.com/photo.jpg", actual.getOrThrow().photoUrl)
        assertEquals(
            listOf(
                "https://example.com/server-image-1.jpg",
                "https://example.com/server-image-2.jpg",
            ),
            actual.getOrThrow().suggestedImages,
        )
        assertEquals(1, actual.getOrThrow().selectedImageIndex)
    }

    @Test
    fun `get link drafts maps list response`() = runBlocking {
        val linksDataSource = FakeLinksDataSource().apply {
            getLinkDraftsResult = Result.success(
                LinkDraftsResponseDto(
                    data = listOf(
                        LinkDraftDto(
                            key = "draft-key-123",
                            url = "https://example.com/article",
                            title = "Draft title",
                            description = "Draft description",
                            tags = listOf("a", "b"),
                            adult = false,
                            images = listOf(
                                LinkDraftImageDto(
                                    url = "https://example.com/server-image-3.png",
                                    selected = true,
                                ),
                            ),
                            media = MediaDto(
                                embed = null,
                                photo = PhotoDto(
                                    key = "photo-key-2",
                                    label = "draft-list-photo",
                                    mimeType = "image/png",
                                    url = "https://example.com/photo-2.png",
                                    size = 2048,
                                    width = 800,
                                    height = 600,
                                ),
                                survey = null,
                            ),
                        ),
                    ),
                ),
            )
        }
        val sut = createSut(linksDataSource)

        val actual = sut.getLinkDrafts()

        assertEquals(listOf(Unit), linksDataSource.getLinkDraftsCalls)
        assertEquals(1, actual.getOrThrow().size)
        assertEquals("draft-key-123", actual.getOrThrow().single().key)
        assertEquals("Draft title", actual.getOrThrow().single().title)
        assertEquals(listOf("a", "b"), actual.getOrThrow().single().tags)
        assertEquals("photo-key-2", actual.getOrThrow().single().photoKey)
        assertEquals(listOf("https://example.com/server-image-3.png"), actual.getOrThrow().single().suggestedImages)
        assertEquals(0, actual.getOrThrow().single().selectedImageIndex)
    }

    @Test
    fun `update link draft forwards request`() = runBlocking {
        val linksDataSource = FakeLinksDataSource().apply {
            updateLinkDraftResult = Result.success(Unit)
        }
        val sut = createSut(linksDataSource)
        val request = UpdateLinkDraft(
            title = "Fresh link",
            description = "Optional body",
            tags = listOf("news", "tech"),
            photoKey = "photo-key-77",
            adult = false,
            selectedImageIndex = 2,
        )

        val actual = sut.updateLinkDraft(
            key = "draft-key-123",
            request = request,
        )

        assertTrue(actual.isSuccess)
        assertEquals(
            listOf(
                FakeLinksDataSource.UpdateLinkDraftCall(
                    key = "draft-key-123",
                    request = request,
                ),
            ),
            linksDataSource.updateLinkDraftCalls,
        )
    }

    @Test
    fun `delete link draft forwards key`() = runBlocking {
        val linksDataSource = FakeLinksDataSource().apply {
            deleteLinkDraftResult = Result.success(Unit)
        }
        val sut = createSut(linksDataSource)

        val actual = sut.deleteLinkDraft(key = "draft-key-123")

        assertTrue(actual.isSuccess)
        assertEquals(
            listOf(FakeLinksDataSource.DeleteLinkDraftCall(key = "draft-key-123")),
            linksDataSource.deleteLinkDraftCalls,
        )
    }

    @Test
    fun `create link comment forwards payload and maps single resource item`() = runBlocking {
        val linksDataSource = FakeLinksDataSource().apply {
            createLinkCommentResult = Result.success(
                Fixtures.singleResourceResponseDto(
                    data = Fixtures.resourceItemDto(
                        id = 808,
                        resource = "link_comment",
                        parentId = 404,
                    ),
                ),
            )
        }
        val sut = createSut(linksDataSource)

        val actual = sut.createLinkComment(
            linkId = 404,
            content = "@user: hello",
            adult = false,
            photoKey = "photo-key-3",
        )

        assertEquals(
            listOf(
                FakeLinksDataSource.CreateLinkCommentCall(
                    linkId = 404,
                    content = "@user: hello",
                    adult = false,
                    photoKey = "photo-key-3",
                ),
            ),
            linksDataSource.createLinkCommentCalls,
        )
        assertEquals(
            Fixtures.resourceItem(
                id = 808,
                resource = Resource.LinkComment,
                parentId = 404,
            ),
            actual.getOrThrow(),
        )
    }

    @Test
    fun `create link comment reply forwards payload and maps single resource item`() = runBlocking {
        val linksDataSource = FakeLinksDataSource().apply {
            createLinkCommentReplyResult = Result.success(
                Fixtures.singleResourceResponseDto(
                    data = Fixtures.resourceItemDto(
                        id = 909,
                        resource = "link_comment",
                        parentId = 505,
                    ),
                ),
            )
        }
        val sut = createSut(linksDataSource)

        val actual = sut.createLinkCommentReply(
            linkId = 404,
            commentId = 505,
            content = "@author: reply",
            adult = false,
            photoKey = "photo-key-4",
        )

        assertEquals(
            listOf(
                FakeLinksDataSource.CreateLinkCommentReplyCall(
                    linkId = 404,
                    commentId = 505,
                    content = "@author: reply",
                    adult = false,
                    photoKey = "photo-key-4",
                ),
            ),
            linksDataSource.createLinkCommentReplyCalls,
        )
        assertEquals(
            Fixtures.resourceItem(
                id = 909,
                resource = Resource.LinkComment,
                parentId = 505,
            ),
            actual.getOrThrow(),
        )
    }

    @Test
    fun `update link comment forwards payload and maps single resource item`() = runBlocking {
        val linksDataSource = FakeLinksDataSource().apply {
            updateLinkCommentResult = Result.success(
                Fixtures.singleResourceResponseDto(
                    data = Fixtures.resourceItemDto(
                        id = 919,
                        resource = "link_comment",
                        parentId = 505,
                    ),
                ),
            )
        }
        val sut = createSut(linksDataSource)

        val actual = sut.updateLinkComment(
            linkId = 404,
            commentId = 505,
            content = "updated comment payload",
            adult = true,
            photoKey = "photo-key-update-5",
        )

        assertEquals(
            listOf(
                FakeLinksDataSource.UpdateLinkCommentCall(
                    linkId = 404,
                    commentId = 505,
                    content = "updated comment payload",
                    adult = true,
                    photoKey = "photo-key-update-5",
                ),
            ),
            linksDataSource.updateLinkCommentCalls,
        )
        assertEquals(
            Fixtures.resourceItem(
                id = 919,
                resource = Resource.LinkComment,
                parentId = 505,
            ),
            actual.getOrThrow(),
        )
    }

    @Test
    fun `vote operations delegate to data source`() = runBlocking {
        val linksDataSource = FakeLinksDataSource().apply {
            voteOnLinkResult = Result.success(Unit)
            voteDownOnLinkResult = Result.success(Unit)
            removeVoteOnLinkResult = Result.success(Unit)
            voteUpOnRelatedLinkResult = Result.success(Unit)
            voteDownOnRelatedLinkResult = Result.success(Unit)
            removeVoteOnRelatedLinkResult = Result.success(Unit)
            voteOnLinkCommentResult = Result.success(Unit)
            voteDownOnLinkCommentResult = Result.success(Unit)
            removeVoteOnLinkCommentResult = Result.success(Unit)
        }
        val sut = createSut(linksDataSource)

        val voteLink = sut.voteOnLink(1)
        val voteDownLink = sut.voteDownOnLink(2, VoteReason.Spam)
        val removeVoteLink = sut.removeVoteOnLink(3)
        val voteUpRelated = sut.voteUpOnRelatedLink(linkId = 10, relatedId = 11)
        val voteDownRelated = sut.voteDownOnRelatedLink(linkId = 12, relatedId = 13)
        val removeVoteRelated = sut.removeVoteOnRelatedLink(linkId = 14, relatedId = 15)
        val voteComment = sut.voteOnLinkComment(linkId = 4, commentId = 5)
        val voteDownComment = sut.voteDownOnLinkComment(linkId = 6, commentId = 7)
        val removeVoteComment = sut.removeVoteOnLinkComment(linkId = 8, commentId = 9)

        assertTrue(voteLink.isSuccess)
        assertTrue(voteDownLink.isSuccess)
        assertTrue(removeVoteLink.isSuccess)
        assertTrue(voteUpRelated.isSuccess)
        assertTrue(voteDownRelated.isSuccess)
        assertTrue(removeVoteRelated.isSuccess)
        assertTrue(voteComment.isSuccess)
        assertTrue(voteDownComment.isSuccess)
        assertTrue(removeVoteComment.isSuccess)
        assertEquals(listOf(1), linksDataSource.voteOnLinkCalls)
        assertEquals(
            listOf(FakeLinksDataSource.LinkVoteDownCall(linkId = 2, reason = VoteReason.Spam)),
            linksDataSource.voteDownOnLinkCalls,
        )
        assertEquals(listOf(3), linksDataSource.removeVoteOnLinkCalls)
        assertEquals(
            listOf(FakeLinksDataSource.RelatedLinkVoteCall(linkId = 10, relatedId = 11)),
            linksDataSource.voteUpOnRelatedLinkCalls,
        )
        assertEquals(
            listOf(FakeLinksDataSource.RelatedLinkVoteCall(linkId = 12, relatedId = 13)),
            linksDataSource.voteDownOnRelatedLinkCalls,
        )
        assertEquals(
            listOf(FakeLinksDataSource.RelatedLinkVoteCall(linkId = 14, relatedId = 15)),
            linksDataSource.removeVoteOnRelatedLinkCalls,
        )
        assertEquals(
            listOf(FakeLinksDataSource.LinkCommentVoteCall(linkId = 4, commentId = 5)),
            linksDataSource.voteOnLinkCommentCalls,
        )
        assertEquals(
            listOf(FakeLinksDataSource.LinkCommentVoteCall(linkId = 6, commentId = 7)),
            linksDataSource.voteDownOnLinkCommentCalls,
        )
        assertEquals(
            listOf(FakeLinksDataSource.LinkCommentVoteCall(linkId = 8, commentId = 9)),
            linksDataSource.removeVoteOnLinkCommentCalls,
        )
    }

    @Test
    fun `get links propagates failure`() = runBlocking {
        val expected = IllegalStateException("io")
        val linksDataSource = FakeLinksDataSource().apply {
            getLinksResult = Result.failure(expected)
        }
        val sut = createSut(linksDataSource)

        val actual = sut.getLinks(
            page = null,
            limit = null,
            linksSortType = LinksSortType.Active,
            linksType = LinksType.HOMEPAGE,
            category = null,
            bucket = null,
        )

        assertTrue(actual.isFailure)
        assertSame(expected, actual.exceptionOrNull())
    }

    @Test
    fun `create link comment propagates failure`() = runBlocking {
        val expected = IllegalStateException("create failed")
        val linksDataSource = FakeLinksDataSource().apply {
            createLinkCommentResult = Result.failure(expected)
        }
        val sut = createSut(linksDataSource)

        val actual = sut.createLinkComment(
            linkId = 12,
            content = "payload",
            adult = false,
            photoKey = null,
        )

        assertTrue(actual.isFailure)
        assertSame(expected, actual.exceptionOrNull())
    }

    private fun createSut(
        linksDataSource: FakeLinksDataSource = FakeLinksDataSource(),
    ): LinksRepositoryImpl {
        return LinksRepositoryImpl(
            linksDataSource = linksDataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )
    }
}

package pl.masslany.podkop.business.common.data.main.mapper.common

import kotlin.test.Test
import kotlin.test.assertEquals
import pl.masslany.podkop.business.common.domain.models.common.Deleted
import pl.masslany.podkop.business.common.domain.models.common.Gender
import pl.masslany.podkop.business.common.domain.models.common.NameColor
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.Voted
import pl.masslany.podkop.business.testsupport.fixtures.BusinessFixtures as Fixtures

class CommonCompositeMappersTest {

    @Test
    fun `author mapper maps nested rank and enum conversions`() {
        val dto = Fixtures.authorDto(
            color = "black",
            gender = "f",
            rank = Fixtures.commonRankDto(position = null, trend = -3),
        )

        assertEquals(
            Fixtures.author(
                avatar = dto.avatar,
                blacklist = dto.blacklist,
                color = NameColor.Black,
                company = dto.company,
                follow = dto.follow,
                gender = Gender.Female,
                note = dto.note,
                online = dto.online,
                rank = Fixtures.rank(position = 0, trend = -3),
                status = dto.status,
                username = dto.username,
                verified = dto.verified,
            ),
            dto.toAuthor(),
        )
    }

    @Test
    fun `media mapper maps nested components`() {
        val actionsDto = Fixtures.commonActionsDto(voteUp = null, voteDown = true)
        val answerDto = Fixtures.answerDto(count = 9, id = 77, text = "A", voted = 0)
        val surveyDto = Fixtures.surveyDto(actions = actionsDto, answers = listOf(answerDto))
        val embedDto = Fixtures.embedDto(thumbnail = null)
        val photoDto = Fixtures.photoDto(width = 999)
        val dto = Fixtures.mediaDto(
            embed = embedDto,
            photo = photoDto,
            survey = surveyDto,
        )

        assertEquals(
            Fixtures.media(
                embed = Fixtures.embed(
                    key = embedDto.key,
                    thumbnail = "",
                    type = embedDto.type,
                    url = embedDto.url,
                ),
                photo = Fixtures.photo(
                    height = photoDto.height,
                    key = photoDto.key,
                    label = photoDto.label,
                    mimeType = photoDto.mimeType,
                    size = photoDto.size,
                    url = photoDto.url,
                    width = 999,
                ),
                survey = Fixtures.survey(
                    actions = Fixtures.actions(
                        create = true,
                        createFavourite = false,
                        delete = true,
                        deleteFavourite = false,
                        finishAma = true,
                        report = false,
                        startAma = true,
                        undoVote = false,
                        update = true,
                        voteDown = true,
                        voteUp = false,
                    ),
                    answers = listOf(Fixtures.answer(count = 9, id = 77, text = "A", voted = 0)),
                    count = surveyDto.count,
                    deletable = surveyDto.deletable,
                    editable = surveyDto.editable,
                    key = surveyDto.key,
                    question = surveyDto.question,
                    voted = surveyDto.voted,
                ),
            ),
            dto.toMedia(),
        )
    }

    @Test
    fun `parent mapper maps link id from nested link`() {
        val dto = Fixtures.parentDto(
            id = 55,
            link = Fixtures.resourceItemDto(id = 1234),
        )

        assertEquals(Fixtures.parent(id = 55, linkId = 1234), dto.toParent())
    }

    @Test
    fun `survey mapper maps fields and nested collections`() {
        val dto = Fixtures.surveyDto(
            actions = Fixtures.commonActionsDto(create = null, voteDown = true),
            answers = listOf(
                Fixtures.answerDto(id = 1, text = "One", voted = -1),
                Fixtures.answerDto(id = 2, text = "Two", voted = 1),
            ),
            count = 88,
            voted = -1,
        )

        assertEquals(
            Fixtures.survey(
                actions = Fixtures.actions(
                    create = false,
                    createFavourite = false,
                    delete = true,
                    deleteFavourite = false,
                    finishAma = true,
                    report = false,
                    startAma = true,
                    undoVote = false,
                    update = true,
                    voteDown = true,
                    voteUp = true,
                ),
                answers = listOf(
                    Fixtures.answer(count = 3, id = 1, text = "One", voted = -1),
                    Fixtures.answer(count = 3, id = 2, text = "Two", voted = 1),
                ),
                count = 88,
                deletable = dto.deletable,
                editable = dto.editable,
                key = dto.key,
                question = dto.question,
                voted = -1,
            ),
            dto.toSurvey(),
        )
    }

    @Test
    fun `comment item mapper maps list and defaults missing parent id`() {
        val nestedComments = Fixtures.commentsDto(count = 4, hot = null, items = null)
        val embedDto = Fixtures.embedDto(thumbnail = null)
        val dto = Fixtures.commentItemDto(
            comments = nestedComments,
            parentId = null,
            resource = "entry_comment",
            voted = 1,
            deleted = null,
            media = Fixtures.mediaDto(embed = embedDto),
        )

        val actual = listOf(dto).toCommentList()

        assertEquals(
            listOf(
                Fixtures.comment(
                    actions = Fixtures.actions(),
                    adult = dto.adult,
                    archive = dto.archive,
                    author = Fixtures.author(),
                    blacklist = dto.blacklist,
                    comments = Fixtures.comments(count = 4, hot = false, items = emptyList()),
                    content = dto.content,
                    createdAt = dto.createdAt,
                    deletable = dto.deletable,
                    deleted = Deleted.None,
                    device = dto.device,
                    editable = dto.editable,
                    favourite = dto.favourite,
                    id = dto.id,
                    media = Fixtures.media(
                        embed = Fixtures.embed(
                            key = embedDto.key,
                            thumbnail = "",
                            type = embedDto.type,
                            url = embedDto.url,
                        ),
                        photo = null,
                        survey = null,
                    ),
                    parentId = -1,
                    resource = Resource.EntryComment,
                    slug = dto.slug,
                    tags = dto.tags,
                    voted = Voted.Positive,
                    votes = Fixtures.votes(),
                ),
            ),
            actual,
        )
    }

    @Test
    fun `comments mapper defaults hot and items when missing`() {
        val dto = Fixtures.commentsDto(count = 9, hot = null, items = null)

        assertEquals(
            Fixtures.comments(count = 9, hot = false, items = emptyList()),
            dto.toComments(),
        )
    }

    @Test
    fun `resource item mapper maps nested values and applies defaults`() {
        val parentLink = Fixtures.resourceItemDto(id = 808)
        val embedDto = Fixtures.embedDto(thumbnail = null)
        val dto = Fixtures.resourceItemDto(
            actions = null,
            adult = null,
            archive = null,
            author = null,
            comments = Fixtures.commentsDto(count = 7, hot = null, items = null),
            content = null,
            deletable = null,
            deleted = null,
            description = null,
            editable = null,
            hot = null,
            id = null,
            media = Fixtures.mediaDto(embed = embedDto),
            name = null,
            parent = Fixtures.parentDto(id = 42, link = parentLink),
            recommended = null,
            resource = null,
            slug = null,
            source = Fixtures.sourceDto(type = null, typeId = null),
            tags = null,
            title = null,
            voted = null,
            votes = null,
        )

        val actual = listOf(dto).toResourceItemList().single()

        assertEquals(
            Fixtures.resourceItem(
                actions = null,
                adult = false,
                archive = false,
                author = null,
                comments = Fixtures.comments(count = 7, hot = false, items = emptyList()),
                content = "",
                createdAt = dto.createdAt,
                deleted = Deleted.None,
                deletable = false,
                description = "",
                editable = false,
                hot = false,
                id = -1,
                media = Fixtures.media(
                    embed = Fixtures.embed(
                        key = embedDto.key,
                        thumbnail = "",
                        type = embedDto.type,
                        url = embedDto.url,
                    ),
                    photo = null,
                    survey = null,
                ),
                name = "",
                parent = Fixtures.parent(id = 42, linkId = 808),
                parentId = dto.parentId,
                publishedAt = dto.publishedAt,
                recommended = false,
                resource = Resource.Unknown,
                slug = "",
                source = Fixtures.source(label = dto.source!!.label, type = "", typeId = -1, url = dto.source.url),
                tags = emptyList(),
                title = "",
                voted = Voted.None,
                votes = null,
            ),
            actual,
        )
    }

    @Test
    fun `resources mapper maps data and pagination`() {
        val itemDto = Fixtures.resourceItemDto(
            id = 99,
            resource = "entry",
            voted = -1,
            deleted = "moderator",
            parent = null,
            media = null,
            comments = null,
            source = null,
        )
        val dto = Fixtures.resourceResponseDto(
            data = listOf(itemDto),
            pagination = Fixtures.paginationDto(perPage = 50, total = 123, next = null, prev = "/prev"),
        )

        assertEquals(
            Fixtures.resources(
                data = listOf(
                    Fixtures.resourceItem(
                        id = 99,
                        resource = Resource.Entry,
                        voted = Voted.Negative,
                        deleted = Deleted.Moderator,
                        parent = null,
                        media = null,
                        comments = null,
                        source = null,
                    ),
                ),
                page = Fixtures.pagination(perPage = 50, total = 123, next = "", prev = "/prev"),
            ),
            dto.toResources(),
        )
    }
}

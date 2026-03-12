package pl.masslany.podkop.business.common.data.main.mapper.common

import kotlin.test.Test
import kotlin.test.assertEquals
import pl.masslany.podkop.business.common.domain.models.common.Deleted
import pl.masslany.podkop.business.common.domain.models.common.Voted
import pl.masslany.podkop.business.testsupport.fixtures.BusinessFixtures as Fixtures

class CommonValueMappersTest {

    @Test
    fun `actions mapper maps explicit values`() {
        val dto = Fixtures.commonActionsDto(
            create = true,
            createFavourite = true,
            delete = false,
            deleteFavourite = true,
            finishAma = false,
            report = true,
            startAma = false,
            undoVote = true,
            update = false,
            voteDown = true,
            voteUp = false,
        )

        assertEquals(
            Fixtures.actions(
                create = true,
                createFavourite = true,
                delete = false,
                deleteFavourite = true,
                finishAma = false,
                report = true,
                startAma = false,
                undoVote = true,
                update = false,
                voteDown = true,
                voteUp = false,
            ),
            dto.toActions(),
        )
    }

    @Test
    fun `actions mapper defaults nulls to false`() {
        val dto = Fixtures.commonActionsDto(
            create = null,
            createFavourite = null,
            delete = null,
            deleteFavourite = null,
            finishAma = null,
            report = null,
            startAma = null,
            undoVote = null,
            update = null,
            voteDown = null,
            voteUp = null,
        )

        assertEquals(
            Fixtures.actions(
                create = false,
                createFavourite = false,
                delete = false,
                deleteFavourite = false,
                finishAma = false,
                report = false,
                startAma = false,
                undoVote = false,
                update = false,
                voteDown = false,
                voteUp = false,
            ),
            dto.toActions(),
        )
    }

    @Test
    fun `answer mapper maps fields`() {
        val dto = Fixtures.answerDto(count = 7, id = 9, text = "yes", voted = -1)

        assertEquals(Fixtures.answer(count = 7, id = 9, text = "yes", voted = -1), dto.toAnswer())
    }

    @Test
    fun `deleted mapper maps known and fallback values`() {
        assertEquals(Deleted.Moderator, "moderator".toDeleted())
        assertEquals(Deleted.Author, "author".toDeleted())
        assertEquals(Deleted.Host, "host".toDeleted())
        assertEquals(Deleted.None, "unknown".toDeleted())
        assertEquals(Deleted.None, null.toDeleted())
    }

    @Test
    fun `embed mapper defaults missing thumbnail to empty string`() {
        val dto = Fixtures.embedDto(thumbnail = null)

        assertEquals(
            Fixtures.embed(
                key = dto.key,
                thumbnail = "",
                type = dto.type,
                url = dto.url,
            ),
            dto.toEmbed(),
        )
    }

    @Test
    fun `pagination mapper defaults null values`() {
        val dto = Fixtures.paginationDto(
            perPage = null,
            total = null,
            totalItems = null,
            next = null,
            prev = null,
        )

        assertEquals(
            Fixtures.pagination(perPage = 0, total = 0, next = "", prev = ""),
            dto.toPagination(),
        )
    }

    @Test
    fun `pagination mapper falls back to total items when total is missing`() {
        val dto = Fixtures.paginationDto(
            total = null,
            totalItems = 123,
        )

        assertEquals(
            Fixtures.pagination(total = 123),
            dto.toPagination(),
        )
    }

    @Test
    fun `photo mapper maps fields`() {
        val dto = Fixtures.photoDto(
            height = 1,
            key = "k",
            label = "l",
            mimeType = "m",
            size = 2,
            url = "u",
            width = 3,
        )

        assertEquals(
            Fixtures.photo(
                height = 1,
                key = "k",
                label = "l",
                mimeType = "m",
                size = 2,
                url = "u",
                width = 3,
            ),
            dto.toPhoto(),
        )
    }

    @Test
    fun `rank mapper defaults null position to zero`() {
        val dto = Fixtures.commonRankDto(position = null, trend = -4)

        assertEquals(Fixtures.rank(position = 0, trend = -4), dto.toRank())
    }

    @Test
    fun `source mapper defaults null type and type id`() {
        val dto = Fixtures.sourceDto(type = null, typeId = null)

        assertEquals(
            Fixtures.source(
                label = dto.label,
                type = "",
                typeId = -1,
                url = dto.url,
            ),
            dto.toSource(),
        )
    }

    @Test
    fun `voted mapper maps negative positive and fallback`() {
        assertEquals(Voted.Negative, (-1).toVoted())
        assertEquals(Voted.Positive, 1.toVoted())
        assertEquals(Voted.None, 0.toVoted())
        assertEquals(Voted.None, null.toVoted())
    }

    @Test
    fun `votes mapper defaults null count to zero`() {
        val dto = Fixtures.votesDto(count = null, down = 2, up = 9)

        assertEquals(Fixtures.votes(count = 0, down = 2, up = 9), dto.toVotes())
    }
}
